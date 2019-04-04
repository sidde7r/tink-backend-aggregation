package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken;

public abstract class IcaBankenConstants {

    public static class Urls {

        private static String BASE_URL = "accounts/1.0.0";
        public static final String ACCOUNTS_PATH = BASE_URL + "/Accounts";
        public static final String TRANSACTIONS_PATH =
                BASE_URL + "/Accounts/{accountId}/transactions";
    }

    public static class StorageKeys {

        public static final String BASE_URL = "BASE_URL";
        public static final String TOKEN = "TOKEN";
    }

    public static class QueryKeys {

        public static final String WITH_BALANCE = "withBalance";
        public static final String DATE_FROM = "from";
        public static final String DATE_TO = "to";
        public static final String STATUS = "status";
    }

    public static class QueryValues {

        public static final String STATUS = "both";
    }

    public static class HeaderKeys {

        public static final String REQUEST_ID = "X-Request-ID";
    }

    public static class FormKeys {}

    public static class FormValues {}

    public static class LogTags {}

    public class Market {

        public static final String INTEGRATION_NAME = "icabanken";
        public static final String CLIENT_NAME = "tink";
    }

    public class Account {

        public static final String INTERIM_AVAILABLE_BALANCE = "interimAvailable";
        public static final String ACCOUNT_ID = "accountId";
    }
}
