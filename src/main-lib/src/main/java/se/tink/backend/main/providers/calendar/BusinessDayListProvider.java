package se.tink.backend.main.providers.calendar;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import org.joda.time.LocalDate;
import se.tink.libraries.date.DateUtils;

public class BusinessDayListProvider {
    /**
     * Generate map of years, months and their respective lists of business days
     */
    public Map<String, Map<String, List<Integer>>> listBusinessDays(LocalDate startDate, int numberOfMonths) {
        LocalDate endDate = new LocalDate(startDate)
                .plusMonths(numberOfMonths - 1)
                .dayOfMonth().withMaximumValue();

        return createBusinessDays(startDate, endDate);
    }

    /**
     * Iterate from the startDate to the endDate and fill a map with years, months and their business days
     */
    private Map<String, Map<String, List<Integer>>> createBusinessDays(LocalDate startDate, LocalDate endDate) {
        LocalDate currentDate = startDate;

        Map<String, Map<String, List<Integer>>> businessDays = Maps.newHashMap();
        // Go through each year
        while (!currentDate.isAfter(endDate)) {
            int year = currentDate.getYear();
            String yearString = currentDate.year().getAsString();

            Map<String, List<Integer>> yearBusinessDays = Maps.newHashMap();
            // Go through each month of a year, and switch year when we come to a new one
            while (currentDate.getYear() == year && !currentDate.isAfter(endDate)) {
                int currentYear = currentDate.getYear();
                int currentMonthOfYear = currentDate.getMonthOfYear();

                List<Integer> monthBusinessDays = createMonthBusinessDays(currentYear, currentMonthOfYear);

                yearBusinessDays.put(currentDate.monthOfYear().getAsString(), monthBusinessDays);
                currentDate = currentDate.plusMonths(1);
            }

            businessDays.put(yearString, yearBusinessDays);
        }

        return businessDays;
    }

    /**
     * Generate business day list for a single month of a year
     */
    private static List<Integer> createMonthBusinessDays(Integer year, Integer month) {
        LocalDate startDate = new LocalDate(year, month, 1);
        LocalDate endDate = new LocalDate(startDate).dayOfMonth().withMaximumValue();

        List<Integer> businessDays = Lists.newArrayList();
        while (!startDate.isAfter(endDate)) {
            if (DateUtils.isBusinessDay(startDate)) {
                businessDays.add(startDate.getDayOfMonth());
            }
            startDate = startDate.plusDays(1);
        }

        return businessDays;
    }
}
