package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis;

public class UkOpenBankingPaymentConstants {

    public static final String CONSENT_ID_KEY = "consentId";
    public static final String PAYMENT_ID_KEY = "paymentId";

    public static class ErrorMessage {
        public static final String CREDITOR_VALIDATION_FAILURE =
                "Creditor account failed to pass validation checks";
        public static final String CREDITOR_SAME_USER_AS_DEBTOR_FAILURE =
                "Sender and recipient can not be the same user";
        public static final String DEBTOR_VALIDATION_FAILURE =
                "Debtor account failed to pass validation checks";
        public static final String EXCEED_DAILY_LIMIT_FAILURE =
                "Forbidden: This payment exceeds the daily payment limit";
        public static final String INVALID_CLAIM_FAILURE = "Invalid Claims";
        public static final String NO_DESCRIPTION = "No Description";
        public static final String PAYMENT_RE_AUTHENTICATION_REQUIRED =
                "Not having required scope or permission to perform this action. Please contact support for further details.";
        public static final String PROFILE_IS_RESTRICTED = "9038";
        public static final String SUSPICIOUS_TRANSACTION =
                "Our systems have identified your transaction as highly suspicious.";
        public static final String SAME_SENDER_AND_RECIPIENT =
                "Sender and recipient can not be the same user.";
        public static final String PAYMENTS_IN_EUR_ARE_NOT_AVAILABLE =
                "Domestic payments in EUR are not available";
    }
}
