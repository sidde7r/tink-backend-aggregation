package se.tink.backend.common.utils;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.joda.time.DateTime;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.core.Category;
import se.tink.backend.core.Transaction;
import se.tink.libraries.date.DateUtils;
import se.tink.backend.utils.guavaimpl.Orderings;

public class PaydayCalculator {

    private final ImmutableList<Transaction> salaryTransactions;

    public PaydayCalculator(CategoryConfiguration categoryConfiguration,
            ImmutableMap<String, Category> categoriesByCode,
            Iterable<Transaction> transactions) {

        this.salaryTransactions = getSalaryTransactions(categoriesByCode, transactions, categoryConfiguration);
    }

    public Integer detectPayday() {
        return detectPayday(CalculationMode.SIMPLE);
    }

    public Integer detectPayday(final CalculationMode calculationMode) {
        if (calculationMode == CalculationMode.SIMPLE) {
            return detectPaydaySimple();
        } else {
            return detectPaydayAdvanced();
        }
    }

    /**
     * Detect payday
     *
     * @return Day of month (1-31), if able to identify one (and only one) payday. `NULL` if inconclusive.
     */
    private Integer detectPaydaySimple() {

        if (salaryTransactions.isEmpty()) {
            return null;
        }

        final Calendar calendar = DateUtils.getCalendar();

        // 1-indexed (1 to 31)
        ImmutableList<Integer> paydays = ImmutableList.copyOf(Iterables.transform(salaryTransactions,
                t -> {

                    calendar.setTime(t.getDate());

                    return calendar.get(Calendar.DAY_OF_MONTH);
                }));

        int[] countByDay = new int[31]; // 0..30 => 31 days

        for (int i : paydays) {
            countByDay[i - 1]++;
        }

        // The initial max count value is the threshold
        int maxCount = 2; // there'll need to be at least three salaries the same day

        Integer payday = null;

        for (int i = 0; i < countByDay.length; i++) {
            if (countByDay[i] == maxCount) {
                // There can be only one...
                payday = null;
            } else if (countByDay[i] > maxCount) {
                payday = i;
                maxCount = countByDay[i];
            }
        }

        // Make 1-indexed
        if (payday != null) {
            payday++;
        }

        return payday;
    }

    /**
     * Detect payday
     *
     * @return Day of month (1-31), if able to identify one (and only one) payday. `NULL` if inconclusive.
     */
    public Integer detectPaydayAdvanced() {

        if (salaryTransactions.isEmpty()) {
            return null;
        }

        double[] countByDay = new double[32];

        for (Transaction transaction : salaryTransactions) {

            DateTime date = new DateTime(transaction.getDate());

            List<Integer> days = Lists.newArrayList();

            // Check if upcoming days are Saturday, Sunday or red day
            do {
                days.add(date.getDayOfMonth());
                date = date.plusDays(1);
            } while (!DateUtils.isBusinessDay(date));

            // Weight the value
            for (Integer i : days) {
                countByDay[i] += 1.0 / days.size();
            }

        }

        // The initial max count value is the threshold
        double maxCount = 2.0;

        Integer payday = null;

        for (int i = 1; i < countByDay.length; i++) {
            if (countByDay[i] == maxCount) {
                // There can be only one...
                payday = null;
            } else if (countByDay[i] > maxCount) {
                payday = i;
                maxCount = countByDay[i];
            }
        }

        return payday;
    }

    /**
     * Find the date of the last salary.
     *
     * @return
     */
    public Date detectLastSalaryDate() {

        if (salaryTransactions.isEmpty()) {
            return null;
        }

        return salaryTransactions.stream().max(Orderings.TRANSACTION_ORIGINAL_DATE_ORDERING).get().getDate();
    }

    private static ImmutableList<Transaction> getSalaryTransactions(ImmutableMap<String, Category> categoriesByCode,
            Iterable<Transaction> transactions, CategoryConfiguration categoryConfiguration) {

        if (transactions == null) {
            return ImmutableList.of();
        }

        final Category salaryCategory = categoriesByCode.get(categoryConfiguration.getSalaryCode());

        return ImmutableList.copyOf(Iterables.filter(transactions,
                t -> Objects.equal(t.getCategoryId(), salaryCategory.getId())));
    }

    public enum CalculationMode {
        SIMPLE,
        ADVANCED
    }

}
