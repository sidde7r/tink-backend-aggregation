package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.loan.rpc;

import com.google.common.collect.ImmutableList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class NumberOfMonthsBoundCalculator {

    private static final String MONTHS_IN_DANISH = "måneder";
    private static final String MONTHS_IN_ENGLISH = "months";

    private static final String YEARS_IN_DANISH = "år";
    private static final String YEARS_IN_ENGLISH = "years";

    private static final List<ToMonthCalculator> toMonthCalculators =
            ImmutableList.of(new MonthToMonthCalculator(), new YearToMonthCalculator());

    interface ToMonthCalculator {
        boolean canHandle(final String s);

        int extractValue(final String s);
    }

    static class MonthToMonthCalculator implements ToMonthCalculator {
        @Override
        public boolean canHandle(final String s) {
            return (s.contains(MONTHS_IN_DANISH) || s.contains(MONTHS_IN_ENGLISH))
                    && !(s.contains(YEARS_IN_DANISH) || s.contains(YEARS_IN_ENGLISH));
        }

        @Override
        public int extractValue(final String s) {
            return Integer.parseInt(
                    s.replace(MONTHS_IN_DANISH, "").replace(MONTHS_IN_ENGLISH, "").trim());
        }
    }

    static class YearToMonthCalculator implements ToMonthCalculator {
        @Override
        public boolean canHandle(final String s) {
            return (s.contains(YEARS_IN_DANISH) || s.contains(YEARS_IN_ENGLISH))
                    && !(s.contains(MONTHS_IN_DANISH) || s.contains(MONTHS_IN_ENGLISH));
        }

        @Override
        public int extractValue(final String s) {
            return 12
                    * Integer.parseInt(
                            s.replace(YEARS_IN_DANISH, "").replace(YEARS_IN_ENGLISH, "").trim());
        }
    }

    public int calculate(String s) {
        String[] parts = s.split(",");

        int monthSum = 0;

        for (String part : parts) {
            monthSum += calculateForPart(part);
        }

        return monthSum;
    }

    private int calculateForPart(final String part) {
        for (ToMonthCalculator calculator : toMonthCalculators) {
            if (calculator.canHandle(part)) {
                return calculator.extractValue(part);
            }
        }
        log.error("Found unknown part of maturity: {}", part);
        return 0;
    }
}
