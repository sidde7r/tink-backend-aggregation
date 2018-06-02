package se.tink.backend.main.validators;

import se.tink.backend.main.controllers.exceptions.PeriodInvalidException;
import se.tink.backend.main.providers.calendar.PeriodListProvider;

public class PeriodListValidator {
    public static void validateStartYear(Integer year) {
        if (!PeriodListProvider.isValidYear(year)) {
            throw new PeriodInvalidException(String.format("Year not valid [%d]", year));
        }
    }

    public static void validateStartMonth(Integer month) {
        if (!PeriodListProvider.isValidMonth(month)) {
            throw new PeriodInvalidException(String.format("Month not valid [%d]", month));
        }
    }

    public static void validateNumberOfMonths(Integer months) throws IllegalArgumentException {
        if (!PeriodListProvider.isValidNumberOfMonthsGreaterThanZero(months)) {
            throw new PeriodInvalidException(String.format("Non-positive number of months given [%d]", months));
        }
        if (!PeriodListProvider.isValidNumberOfMonthsLessThanTwoYears(months)) {
            throw new PeriodInvalidException(String.format("More than 24 months given [%d]", months));
        }
    }
}
