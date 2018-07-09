package se.tink.backend.aggregation.agents.nxgen.uk.banks.revolut;

import se.tink.backend.aggregation.nxgen.http.URL;

public class RevolutConstants {

    public static final class Urls {
        public static final String HOST = "https://api.revolut.com";

        public static final URL USER_EXIST = new URL(HOST + "/user/exist");
        public static final URL SIGN_IN = new URL(HOST + "/signin");
        public static final URL RESEND_CODE_VIA_CALL = new URL(HOST + "/verification-code/call");
        public static final URL CONFIRM_SIGN_IN = new URL(HOST + "/signin/confirm");
        public static final URL FEATURES = new URL(HOST + "/features");
        public static final URL USER_CURRENT = new URL(HOST + "/user/current");
        public static final URL WALLET = new URL(HOST + "/user/current/wallet");
        public static final URL TOPUP_ACCOUNTS = new URL(HOST + "/topup/accounts");
        public static final URL TRANSACTIONS = new URL(HOST + "/user/current/transactions/last");
    }

    public static final class Storage {
        public static final String USER_ID = "userId";
        public static final String ACCESS_TOKEN = "accessToken";
        public static final String DEVICE_ID = "deviceId";
    }

    public static final class Tags {
        public static final String AUTHORIZATION_ERROR = "uk_revolut_authorization_error";
    }

    public static final class Headers {
        public static final String AUTHORIZATION_HEADER = "Authorization";
        public static final String DEVICE_ID_HEADER = "X-Device-Id";
        public static final String BASIC = "Basic ";
    }

    public static final class Params {
        public static final String PHONES = "phones";
        public static final String COUNT = "count";
        public static final String TO = "to";
    }

    public enum AppAuthenticationValues {
        API_VERSION("X-Api-Version", "1"),
        APP_VERSION("X-Client-Version", "5.1"),
        MODEL("X-Device-Model", "iPhone8,1"),
        APP_AUTHORIZATION("App", "S9WUnSFBy67gWan7"),
        USER_AGENT("User-Agent", "Revolut/5.1 (iPhone; iOS 10.2; Scale/2.00;Tink (+https://www.tink.se/; noc@tink.se))");

        private final String key;
        private final String value;

        AppAuthenticationValues(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }

    public static final class Accounts {
        public static final String ACTIVE_STATE = "ACTIVE";
        public static final String REQUIRED_REFERENCE = "requiredReference";
    }

    public static final class Pockets {
        public static final String CURRENT_ACCOUNT = "CURRENT";
        public static final String SAVINGS_ACCOUNT = "SAVINGS";
    }

    public static class Pagination {
        public static final int COUNT = 120;
    }
}
