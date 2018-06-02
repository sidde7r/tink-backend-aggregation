package se.tink.backend.main.rpc.calendar;

import se.tink.backend.main.validators.PeriodListValidator;

import java.util.Optional;

public class GetBusinessDaysCommand {
        private final Integer startYear;
        private final Integer startMonth;
        private final Integer months;

        public GetBusinessDaysCommand(Integer startYear, Integer startMonth, Integer months) {
            months = Optional.ofNullable(months).orElse(1);
            validate(startYear, startMonth, months);
            this.startYear = startYear;
            this.startMonth = startMonth;
            this.months = months;
        }

        public Integer getStartYear() {
            return startYear;
        }

        public Integer getStartMonth() {
            return startMonth;
        }

        public Integer getMonths() {
            return months;
        }

        public void validate(Integer startYear, Integer startMonth, Integer months) {
            PeriodListValidator.validateStartYear(startYear);
            PeriodListValidator.validateStartMonth(startMonth);
            PeriodListValidator.validateNumberOfMonths(months);
        }

}
