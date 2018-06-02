package se.tink.backend.common.statistics;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Doubles;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.core.Category;
import se.tink.backend.core.CategoryTypes;
import se.tink.libraries.date.Period;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.UserProfile;
import se.tink.libraries.date.DateUtils;

public class PeriodCalculator {

    private static final boolean DETECT_SALARY = false;
    private final static int MONTHLY_EXPENSE_COUNT_THRESHOLD = 5;

    private static final Ordering<Transaction> TRANSACTION_SORTING_BY_DATE = new Ordering<Transaction>() {
        @Override
        public int compare(Transaction t1, Transaction t2) {
            return t1.getOriginalDate().compareTo(t2.getOriginalDate());
        }
    };

    private final ImmutableMap<String, Category> categoriesByCode;
    private final CategoryConfiguration categoryConfiguration;

    public PeriodCalculator(ImmutableMap<String, Category> categoriesByCode,
            CategoryConfiguration categoryConfiguration) {
        this.categoriesByCode = categoriesByCode;
        this.categoryConfiguration = categoryConfiguration;
    }

    public List<Period> calculatePeriods(UserProfile userProfile, Iterable<Transaction> transactions) {

        if (Iterables.isEmpty(transactions)) {
            return Lists.newArrayList();
        }

        final Date earliestTransactionDate = TRANSACTION_SORTING_BY_DATE.min(transactions).getDate();
        final Date today = new Date();

        // Figure out the earliest transaction date and create a periods list with 2 buffer months in each
        // direction from that date to today. Need future buffer to find period break date for last period.
        List<String> periodNames = getPeriodNamesForDateSpanWithPadding(earliestTransactionDate, today, 2);

        // Period break dates
        Map<String, Date> periodBreakDates = Maps.newHashMap();

        if (DETECT_SALARY && userProfile.getPeriodMode() == ResolutionTypes.MONTHLY_ADJUSTED) {
            periodBreakDates = detectPeriodBreakDates(transactions, periodNames);
        }

        // Get the start date of the first (oldest) valid period
        Date cleanPeriodBreakDate = getCleanDataPeriodBreakDate(transactions, userProfile, periodBreakDates);

        ResolutionTypes resolution = userProfile.getPeriodMode();
        int periodBreakDate = userProfile.getPeriodAdjustedDay();

        return generatePeriodList(periodNames, resolution, periodBreakDate, periodBreakDates,
                earliestTransactionDate, today, cleanPeriodBreakDate);

    }

    /**
     * Calculate the break date (beginning of period) from when periods are considered to be clean.
     *
     * @param transactions
     * @param userProfile
     * @param periodBreakDates NB! Not yet taken into consideration!
     * @return
     */
    private Date getCleanDataPeriodBreakDate(Iterable<Transaction> transactions, UserProfile userProfile,
            Map<String, Date> periodBreakDates) {

        // FIXME Make it to take `periodBreakDates` into consideration

        Date cleanPeriodBreakDate = new Date();

        // Map expenses by period name
        ImmutableListMultimap<String, Transaction> expensesByPeriodName = getExpensesByPeriodName(transactions,
                userProfile, periodBreakDates);

        for (Map.Entry<String, Collection<Transaction>> entry : expensesByPeriodName.asMap().entrySet()) {
            Date date = DateUtils.getFirstDateFromPeriod(entry.getKey(), userProfile.getPeriodMode(),
                    userProfile.getPeriodAdjustedDay());
            int transactionCount = entry.getValue().size();
            if (transactionCount >= MONTHLY_EXPENSE_COUNT_THRESHOLD && date.before(cleanPeriodBreakDate)) {
                cleanPeriodBreakDate = date;
            }
        }

        return cleanPeriodBreakDate;
    }

    /**
     * Generates a list of period names (e.g. "2014-09") for a padded date span.
     *
     * @param start   Start date
     * @param end     End date
     * @param padding Padding (in months)
     * @return
     */
    private List<String> getPeriodNamesForDateSpanWithPadding(Date start, Date end, int padding) {

        Calendar calendar = DateUtils.getCalendar();

        calendar.setTime(start);
        calendar.add(Calendar.MONTH, -padding);
        Date startWithMargin = calendar.getTime();

        calendar.setTime(end);
        calendar.add(Calendar.MONTH, padding);
        Date endWithMargin = calendar.getTime();

        return DateUtils.createPeriodList(startWithMargin, endWithMargin, ResolutionTypes.MONTHLY, -1);
    }

    /**
     * Generate a list of periods, based on supplied settings and date span.
     *
     * @param periodNames
     * @param resolution
     * @param periodBreakDate
     * @param periodBreakDates
     * @param start
     * @param end
     * @param cleanPeriodBreakDate
     * @return
     */
    private List<Period> generatePeriodList(List<String> periodNames, ResolutionTypes resolution, int periodBreakDate,
            Map<String, Date> periodBreakDates, final Date start, final Date end, final Date cleanPeriodBreakDate) {

        List<Period> periods = Lists.newArrayList();
        Calendar calendar = DateUtils.getCalendar();

        for (int i = 0; i < periodNames.size(); i++) {

            Period period = new Period();
            period.setName(periodNames.get(i));
            period.setResolution(resolution);

            Date periodStartDate = periodBreakDates.get(periodNames.get(i));

            // If we don't have a detected period start date, just use the default.

            if (periodStartDate == null) {
                periodStartDate = DateUtils.getFirstDateFromPeriod(periodNames.get(i), resolution, periodBreakDate);
            }

            period.setStartDate(periodStartDate);

            if (i > 0) {
                // If this is not the first period, use the start date from the current period as the end date of the
                // previous period.

                calendar.setTime(periodStartDate);
                calendar.add(Calendar.DAY_OF_YEAR, -1);
                DateUtils.setInclusiveEndTime(calendar);

                Period previousPeriod = periods.get(i - 1);
                previousPeriod.setEndDate(calendar.getTime());
            }

            if ((i + 1) == periodNames.size()) {
                // If this is the last period, use the default period end date.
                period.setEndDate(DateUtils.getLastDateFromPeriod(periodNames.get(i), resolution, periodBreakDate));
            }

            periods.add(period);
        }

        for (Period p : periods) {
            p.setClean(cleanPeriodBreakDate.before(p.getEndDate()));
        }

        return Lists.newArrayList(Iterables.filter(periods,
                p -> (p.getEndDate() != null && p.getStartDate() != null && p.getEndDate().after(start) && p
                        .getStartDate().before(end))));
    }

    /**
     * *** NB! Currently not used! *** Detect period break dates based on income transactions.
     *
     * @param transactions
     * @param periodNames
     * @return
     */
    private Map<String, Date> detectPeriodBreakDates(Iterable<Transaction> transactions, List<String> periodNames) {

        Map<String, Date> periodDateBreaks = Maps.newHashMap();
        Calendar calendar = DateUtils.getCalendar();

        final Category salaryCategory = categoriesByCode.get(categoryConfiguration.getSalaryCode());

        Iterable<Transaction> incomeTransactions = Iterables.filter(transactions,
                t -> t.getCategoryId().equals(salaryCategory.getId()));

        if (Iterables.size(incomeTransactions) > 0) {
            // Find mean value for salary

            DescriptiveStatistics statistics = new DescriptiveStatistics();

            for (Transaction t : incomeTransactions) {
                statistics.addValue(t.getAmount());
            }

            final double meanValue = statistics.getMean();

            // Loop through the periods and find the most suitable transaction as period break

            for (String period : periodNames) {
                final Date salaryStartDate = DateUtils.getFirstPosibleSalaryDateForPeriod(period);
                final Date salaryEndDate = DateUtils.getLastPosibleSalaryDateForPeriod(period);

                // Find transactions within these dates

                List<Transaction> possibleTransactions = Lists.newArrayList(Iterables.filter(incomeTransactions,
                        t -> t.getDate().after(salaryStartDate) && t.getDate().before(salaryEndDate)));

                // Did not find any salary for this period

                if (possibleTransactions.size() == 0) {
                    continue;
                }

                // Take the transaction closest to the mean salary

                Collections.sort(possibleTransactions, (t1, t2) -> Doubles.compare(Math.abs(t1.getAmount() - meanValue),
                        Math.abs(t2.getAmount() - meanValue)));

                calendar.setTime(possibleTransactions.get(0).getDate());
                DateUtils.setInclusiveStartTime(calendar);
                periodDateBreaks.put(period, calendar.getTime());
            }
        }

        return periodDateBreaks;
    }

    /**
     * Filter transactions and map the expenses based on period.
     *
     * @param transactions
     * @param userProfile
     * @param periodBreakDates NB! Not yet taken into consideration!
     * @return
     */
    private ImmutableListMultimap<String, Transaction> getExpensesByPeriodName(Iterable<Transaction> transactions,
            UserProfile userProfile, Map<String, Date> periodBreakDates) {

        final ResolutionTypes resolution = userProfile.getPeriodMode();
        final int periodBreakDate = userProfile.getPeriodAdjustedDay();

        return Multimaps
                .index(Iterables.filter(transactions, t -> t.getCategoryType() == CategoryTypes.EXPENSES), t -> {
                    // FIXME Make it to take `periodBreakDates` into consideration
                    return DateUtils.getMonthPeriod(t.getDate(), resolution, periodBreakDate);
        });
    }
}
