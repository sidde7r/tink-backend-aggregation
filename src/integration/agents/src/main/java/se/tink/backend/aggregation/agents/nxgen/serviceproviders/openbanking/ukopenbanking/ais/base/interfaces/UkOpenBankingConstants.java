package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces;

public interface UkOpenBankingConstants {

    enum PartyEndpoint {
        PARTY("/party"),
        ACCOUNT_ID_PARTY("/accounts/%s/party"),
        ACCOUNT_ID_PARTIES("/accounts/%s/parties");

        private final String value;

        PartyEndpoint(String value) {
            this.value = value;
        }

        public String getPath() {
            return this.value;
        }
    }

    class HttpHeaders {
        public static final String AUTHORIZATION = "Authorization";
        public static final String X_FAPI_FINANCIAL_ID = "x-fapi-financial-id";
        public static final String X_FAPI_INTERACTION_ID = "x-fapi-interaction-id";
    }

    class ApiServices {
        public static final String ACCOUNT_BULK_REQUEST = "/accounts";
        public static final String ACCOUNT_BALANCE_REQUEST = "/accounts/%s/balances";
        public static final String ACCOUNT_TRANSACTIONS_REQUEST = "/accounts/%s/transactions";
        public static final String ACCOUNT_UPCOMING_TRANSACTIONS_REQUEST =
                "/accounts/%s/scheduled-payments";
        public static final String ACCOUNT_BENEFICIARIES_REQUEST = "/accounts/%s/beneficiaries";
    }
}
