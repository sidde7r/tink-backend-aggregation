package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.errorhandling;

import lombok.experimental.UtilityClass;

@UtilityClass
class PolishApiErrors {
    static final String DAILY_REQUEST_LIMIT_REACHED = "TPP daily requests limit reached";
    static final String NOT_IMPLEMENTED = "not implemented";
    static final String DAYS_EN_90 = "90 days";
    static final String DAYS_PL_90 = "90 dni";
    static final String SCA_NEEDED = "SCA needed";
    static final String INVALID_DATE_PERIOD = "Invalid date period";
    static final String INVALID_GRANT = "invalid_grant";

    /** WARNING - that might not handle all the cases. Please verify when creating your agent. */
    public static boolean isTooBigTransactionHistoryWindow(String message) {
        return message.contains(DAYS_EN_90)
                || message.contains(DAYS_PL_90)
                || message.contains(SCA_NEEDED)
                || message.contains(INVALID_DATE_PERIOD);
    }
}
