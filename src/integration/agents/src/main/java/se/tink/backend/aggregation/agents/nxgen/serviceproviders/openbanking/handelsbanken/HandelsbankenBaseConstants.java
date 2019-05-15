package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken;

public final class HandelsbankenBaseConstants {
    public static final String INTEGRATION_NAME = "handelsbanken";

    public static class Urls {
        public static final String BASE_URL =
                "https://sandbox.handelsbanken.com/openbanking/psd2/v2";
        public static final String ACCOUNTS = "/accounts";
        public static final String ACCOUNT_DETAILS = "/accounts/%s";
        public static final String ACCOUNT_TRANSACTIONS = "/accounts/%s/transactions";
    }

    public static class StorageKeys {
        public static final String ACCOUNT_ID = "accountId";
        public static final String CLIENT_ID = "clientId";
        public static final String ACCESS_TOKEN = "accessToken";
    }

    public static class QueryKeys {
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
        public static final String WITH_BALANCE = "withBalance";
    }

    public static class HeaderKeys {
        public static final String X_IBM_CLIENT_ID = "x-ibm-client-id";
        public static final String TPP_TRANSACTION_ID = "tpp-transaction-id";
        public static final String TPP_REQUEST_ID = "tpp-request-id";
        public static final String PSU_IP_ADDRESS = "psu-ip-address";
        public static final String AUTHORIZATION = "authorization";
    }

    public static class AccountBalance {
        public static final String TYPE = "AVAILABLE_AMOUNT";
    }

    public static class Transactions {
        public static final String PENDING_TYPE = "PENDING";
    }

    public static class ExceptionMessages {
        public static final String BALANCE_NOT_FOUND = "Balance not found.";
        public static final String ACCOUNT_TYPE_NOT_SUPPORTED = "Not supported account type: ";
    }
}
