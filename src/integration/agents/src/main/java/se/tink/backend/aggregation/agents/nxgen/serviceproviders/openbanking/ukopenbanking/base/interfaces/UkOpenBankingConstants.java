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
    }
}
