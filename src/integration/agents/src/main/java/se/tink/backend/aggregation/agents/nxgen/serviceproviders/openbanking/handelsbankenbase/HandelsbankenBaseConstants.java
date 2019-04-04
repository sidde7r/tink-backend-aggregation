package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase;

public abstract class HandelsbankenBaseConstants {

    public static class Urls {

        public static String BASE_URL = "https://sandbox.handelsbanken.com/openbanking/psd2/v2";
        public static String ACCOUNTS = "/accounts";
        public static String ACCOUNT_DETAILS = "/accounts/%s";
        public static String ACCOUNT_TRANSACTIONS = "/accounts/%s/transactions";
    }

    public static class StorageKeys {

        public static String ACCOUNT_ID = "accountId";
        public static String CLIENT_ID = "clientId";
        public static String TPP_TRANSACTION_ID = "tppTransactionId";
        public static String TPP_REQUEST_ID = "tppRequestId";
        public static String PSU_IP_ADDRESS = "psuIpAddress";
        public static String ACCESS_TOKEN = "accessToken";
    }

    public static class QueryKeys {

        public static String DATE_FROM = "dateFrom";
        public static String DATE_TO = "dateTo";
        public static String WITH_BALANCE = "withBalance";
    }

    public static class QueryValues {}

    public static class HeaderKeys {

        public static String X_IBM_CLIENT_ID = "x-ibm-client-id";
        public static String TPP_TRANSACTION_ID = "tpp-transaction-id";
        public static String TPP_REQUEST_ID = "tpp-request-id";
        public static String PSU_IP_ADDRESS = "psu-ip-address";
        public static String AUTHORIZATION = "authorization";
    }

    public static class FormKeys {}

    public static class FormValues {}

    public static class LogTags {}

    public static class AccountBalance {

        public static String TYPE = "AVAILABLE_AMOUNT";
    }

    public static class Transactions {

        public static String PENDING_TYPE = "PENDING";
    }

    public static class ExceptionMessages {

        public static String BALANCE_NOT_FOUND = "Balance not found.";
        public static String ACCOUNT_TYPE_NOT_SUPPORTED = "Not supported account type: ";
    }

    public static class Configuration {

        public static String INTEGRATION_NAME = "handelsbankenbase";
        public static String CLIENT_NAME = "tink";
    }
}
