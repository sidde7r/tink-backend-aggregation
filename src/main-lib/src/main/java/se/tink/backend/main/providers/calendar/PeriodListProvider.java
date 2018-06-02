package se.tink.backend.main.providers.calendar;

import com.google.api.client.util.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import se.tink.libraries.date.Period;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.libraries.date.DateUtils;

public class PeriodListProvider {

    public static boolean isValidYear(Integer year) {
        if (year == null || year < 0) {
            return false;
        }

        return true;
    }

    public static boolean isValidMonth(Integer month) {
        if (month == null || month < 1 || month > 12) {
            return false;
        }

        return true;
    }

    public static boolean isValidNumberOfMonthsGreaterThanZero(Integer months) throws IllegalArgumentException {
        if (months == null) {
            return false;
        }

        if (months <= 0) {
            return false;
        }

        return true;
    }

    public static boolean isValidNumberOfMonthsLessThanTwoYears(Integer months) throws IllegalArgumentException {
        if (months == null) {
            return false;
        }

        if (months > 24) {
            return false;
        }

        return true;
    }

    public List<Period> buildListOfPeriods(
            List<Period> userStatePeriods,
            List<String> monthsToGetPeriodFor,
            ResolutionTypes resolutionTypes,
            int peroidBreakDay) {

        Map<String, Period> periodsMap = Maps.newHashMap();
        List<Period> listPeriods = Lists.newArrayList();

        if (userStatePeriods != null) {

            for (Period period : userStatePeriods) {

                periodsMap.put(period.getName(), period);
            }
        }

        for (String period : monthsToGetPeriodFor) {

            if (!periodsMap.containsKey(period)) {
                listPeriods.add(DateUtils.buildMonthlyPeriod(period, resolutionTypes, peroidBreakDay));
            } else {
                listPeriods.add(periodsMap.get(period));
            }
        }

        return listPeriods;
    }

}
