package se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio;

import se.tink.backend.aggregation.nxgen.http.url.URL;

public class MontepioConstants {

    public static class URLs {
        public static final URL LOGIN = new URL(Endpoints.LOGIN);
        public static final URL FINALIZE_LOGIN = new URL(Endpoints.FINALIZE_LOGIN);
        public static final URL FETCH_ACCOUNTS = new URL(Endpoints.FETCH_ACCOUNTS);
        public static final URL FETCH_TRANSACTIONS = new URL(Endpoints.FETCH_TRANSACTIONS);
    }

    public static class Endpoints {

        public static final String BASE = "https://mobweb.montepio.pt/";
        public static final String LOGIN = BASE + "publicMG/LoginTransactionStep0";
        public static final String FINALIZE_LOGIN = BASE + "publicMG/LoginTransactionStep1";
        public static final String FETCH_ACCOUNTS =
                BASE + "privateMG/currentAccount/CurrentAccountsTransaction";
        public static final String FETCH_TRANSACTIONS =
                BASE + "privateMG/currentAccount/CurrentAccountTransactionsTransaction";
    }

    public static class Crypto {

        public static final String SALT_PATTERN = "IPHONE%s";
        public static final String PASSWORD_ENCRYPTION_KEY =
                "ZWJhbmtJVCB8IE9tbmljaGFubmVsIElubm92YXRpb24=";
    }

    public static class HeaderKeys {
        public static final String APP_VERSION = "ITSAPP-VER";
        public static final String APP_ID = "MGAppId";
        public static final String DEVICE = "ITSAPP-DEVICE";
        public static final String LANG = "ITSAPP-LANG";
        public static final String IOS_VERSION = "ITSAPP-SO";
        public static final String MGM_VERSION = "MGMdwVersion";
        public static final String PSU_IP = "MGIP";
        public static final String SCREEN_NAME = "MGScreen";
    }

    public static class HeaderValues {
        public static final String ACCEPT_ENCODING = "br, gzip, deflate";
        public static final String APP_VERSION = "2.38";
        public static final String APP_ID = "iOS-Mobile";
        public static final String DEVICE = "IPHONE";
        public static final String LANG = "pt-PT";
        public static final String IOS_VERSION = "12.4";
        public static final String MGM_VERSION = "5";
        public static final String ACCEPT = "*/*";
        public static final String ACCEPT_LANGUAGE = "en;q=1";
        public static final String PSU_IP = "0.0.0.0";
        public static final String ACCOUNTS_SCREEN_NAME = "AccountsMovementsViewController_P";
        public static final String TRANSACTIONS_SCREEN_NAME = "AccountsDocumentsViewController_P";
    }

    public static class ErrorMessages {
        public static final String INVALID_LOGIN = "MG:7";
        public static final String INVALID_PASSWORD = "MG:4";
    }

    public class FieldValues {
        public static final String DEVICE_MODEL = "Tink";
        public static final String PSU_IP = "0.0.0.0";
        public static final int CREDENTIAL_TYPE = 0;
        public static final String CLIENT_TYPE = "0";
        public static final String LATITUDE = "0.000000";
        public static final String LONGTITUDE = "0.000000";
    }
}
