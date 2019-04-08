package se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq;

import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.http.HeaderEnum;

public class BunqConstants {
    public static class Url {
        public static final String INSTALLATION = "/v1/installation";
        public static final String REGISTER_DEVICE = "/v1/device-server";
        public static final String CREATE_SESSION = "/v1/session-server";
        public static final String MONETARY_ACCOUNTS = "/v1/user/{userId}/monetary-account";
        public static final String MONETARY_ACCOUNTS_TRANSACTIONS =
                "/v1/user/{userId}/monetary-account/{accountId}/payment";
    }

    public enum Headers implements HeaderEnum {
        USER_AGENT("User-Agent", "Tink (+https://www.tink.se/; noc@tink.se)"),
        LANGUAGE("X-Bunq-Language", "en_US"),
        REGION("X-Bunq-Region", "nl_NL"),
        REQUEST_ID("X-Bunq-Client-Request-Id", null),
        GEOLOCATION("X-Bunq-Geolocation", "0 0 0 0 000"),
        CACHE_CONTROL("Cache-Control", "no-cache"),
        CLIENT_AUTH("X-Bunq-Client-Authentication", null),
        CLIENT_SIGNATURE("X-Bunq-Client-Signature", null);

        private final String key;
        private final String value;

        Headers(String key, String value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getValue() {
            return value;
        }
    }

    public static class Pagination {
        public static final String TRANSACTIONS_PER_PAGE_KEY = "count";
        public static final String TRANSACTIONS_PER_PAGE_VALUE = "30";
    }

    public static class UrlParameterKeys {
        public static final String USER_ID = "userId";
        public static final String ACCOUNT_ID = "accountId";
    }

    public static class PredicatesKeys {
        public static final String IBAN_ALIAS_TYPE = "iban";
    }

    public static class StorageKeys {
        public static final String DEVICE_RSA_SIGNING_KEY_PAIR = "deviceRsaSigningKeyPair";
        public static final String BUNQ_PUBLIC_KEY = "bunqPublicKey";
        public static final String DEVICE_SERVER_ID = "deviceServerId";
        public static final String CLIENT_AUTH_TOKEN = "clientAuthToken";
        public static final String USER_ID = "userId";
    }

    public static class LogTags {
        public static final LogTag AUTO_AUTHENTICATION_FAILED =
                LogTag.from("Auto authentication failed");
    }

    public static final String DEVICE_NAME = "Tink";
}
