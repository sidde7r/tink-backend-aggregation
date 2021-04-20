package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis;

public class UkOpenBankingPaymentConstants {

    public static final String CONSENT_ID_KEY = "consentId";
    public static final String PAYMENT_ID_KEY = "paymentId";

    public static class ErrorMessage {
        public static final String CREDITOR_VALIDATION_FAILURE =
                "Creditor account failed to pass validation checks";
        public static final String DEBTOR_VALIDATION_FAILURE =
                "Debtor account failed to pass validation checks";
        public static final String EXCEED_DAILY_LIMIT_FAILURE =
                "Forbidden: This payment exceeds the daily payment limit";
        public static final String INVALID_CLAIM_FAILURE = "Invalid Claims";
        public static final String NO_DESCRIPTION = "No Description";
    }
}
