package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.executor.payment.enums;

import java.time.Period;
import se.tink.libraries.payment.rpc.Payment;

public enum OpBankPaymentFrequency {
    DAILY("Daily") {
        public int getCountValue(Payment payment) {
            return Period.between(payment.getStartDate(), payment.getEndDate()).getDays();
        }
    },
    WEEKLY("Weekly") {
        public int getCountValue(Payment payment) {
            return Period.between(payment.getStartDate(), payment.getEndDate()).getMonths() * 4;
        }
    },
    EVERY_TWO_WEEKS("EveryTwoWeeks") {
        public int getCountValue(Payment payment) {
            return Period.between(payment.getStartDate(), payment.getEndDate()).getMonths() * 2;
        }
    },
    MONTHLY("Monthly") {
        public int getCountValue(Payment payment) {
            return Period.between(payment.getStartDate(), payment.getEndDate()).getMonths();
        }
    },
    EVERY_TWO_MONTHS("EveryTwoMonths") {
        public int getCountValue(Payment payment) {
            return Period.between(payment.getStartDate(), payment.getEndDate()).getMonths() / 2;
        }
    },
    QUARTERLY("Quarterly") {
        public int getCountValue(Payment payment) {
            return Period.between(payment.getStartDate(), payment.getEndDate()).getYears() * 4;
        }
    },
    SEMI_ANNUAL("SemiAnnual") {
        public int getCountValue(Payment payment) {
            return Period.between(payment.getStartDate(), payment.getEndDate()).getYears() * 2;
        }
    },
    ANNUAL("Annual") {
        public int getCountValue(Payment payment) {
            return Period.between(payment.getStartDate(), payment.getEndDate()).getYears();
        }
    };

    private final String frequency;

    OpBankPaymentFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getFrequency() {
        return frequency;
    }

    public abstract int getCountValue(Payment payment);
}
