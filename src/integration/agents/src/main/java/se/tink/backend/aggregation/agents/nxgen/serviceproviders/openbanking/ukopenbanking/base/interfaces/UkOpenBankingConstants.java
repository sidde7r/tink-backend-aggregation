package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces;

import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.OpenbankConstants;

public abstract class UkOpenBankingConstants extends OpenbankConstants {
    public static final String INTEGRATION_NAME = "ukOpenBankingJson";

    public static class HttpHeaders {
        public static final String X_IDEMPOTENCY_KEY = "x-idempotency-key";
    }

    public static class ApiServices {
        public static final String ACCOUNT_BULK_REQUEST = "/accounts";
        public static final String ACCOUNT_BALANCE_REQUEST = "/accounts/%s/balances";
        public static final String ACCOUNT_TRANSACTIONS_REQUEST = "/accounts/%s/transactions";
        public static final String ACCOUNT_UPCOMING_TRANSACTIONS_REQUEST =
                "/accounts/%s/scheduled-payments";
        public static final String ACCOUNT_REQUESTS = "/account-requests";
        public static final String PAYMENTS = "/payments";
        public static final String PAYMENT_SUBMISSIONS = "/payment-submissions";

        public static class Domestic {
            public static final String PAYMENT_CONSENT = "/domestic-payment-consents";
            public static final String PAYMENT_CONSENT_STATUS =
                    "/domestic-payment-consents/{consentId}";
            public static final String PAYMENT_FUNDS_CONFIRMATION =
                    "/domestic-payment-consents/{consentId}/funds-confirmation";
            public static final String PAYMENT = "/domestic-payments";
            public static final String PAYMENT_STATUS = "/domestic-payments/{paymentId}";
        }

        public static class International {
            public static final String PAYMENT_CONSENT = "/international-payment-consents";
            public static final String PAYMENT_CONSENT_STATUS =
                    "/international-payment-consents/{consentId}";
            public static final String PAYMENT_FUNDS_CONFIRMATION =
                    "/international-payment-consents/{consentId}/funds-confirmation";
            public static final String PAYMENT = "/international-payments";
            public static final String PAYMENT_STATUS = "/international-payments/{paymentId}";
        }

        public static class UrlParameterKeys {
            public static final String consentId = "consentId";
            public static final String paymentId = "paymentId";
        }
    }
}
