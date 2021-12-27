package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq;

import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.http.constants.CommonHeaders;
import se.tink.backend.aggregation.nxgen.http.header.HeaderEnum;

public final class BunqBaseConstants {
    public static final String DEVICE_NAME = "Tink";

    private BunqBaseConstants() {}

    public enum Headers implements HeaderEnum {
        USER_AGENT("User-Agent", CommonHeaders.DEFAULT_USER_AGENT),
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

    public static class Url {
        public static final String INSTALLATION = "/v1/installation";
        public static final String REGISTER_DEVICE = "/v1/device-server";
        public static final String CREATE_SESSION = "/v1/session-server";
        public static final String MONETARY_ACCOUNTS = "/v1/user/{userId}/monetary-account";
        public static final String MONETARY_ACCOUNTS_TRANSACTIONS =
                "/v1/user/{userId}/monetary-account/{accountId}/payment";
    }

    public static class Pagination {
        public static final String TRANSACTIONS_PER_PAGE_KEY = "count";
        public static final String TRANSACTIONS_PER_PAGE_VALUE = "30";
    }

    public static class UrlParameterKeys {
        public static final String USER_ID = "userId";
        public static final String ACCOUNT_ID = "accountId";
        public static final String PAYMENT_ID = "paymentId";
    }

    public static class PredicatesKeys {
        public static final String IBAN_ALIAS_TYPE = "iban";
    }

    public static class StorageKeys {
        public static final String USER_DEVICE_RSA_SIGNING_KEY_PAIR = "deviceRsaSigningKeyPair";
        public static final String CLIENT_AUTH_TOKEN = "clientAuthToken";
        public static final String USER_CLIENT_AUTH_TOKEN = "userClientAuthToken";
        public static final String USER_ID = "userId";
        public static final String USER_API_KEY = "user-api-key";
    }

    public static class LogTags {
        public static final LogTag AUTO_AUTHENTICATION_FAILED =
                LogTag.from("Auto authentication failed");
    }

    public static class Errors {
        public static final String INCORRECT_USER_CREDENTIALS =
                "user credentials are incorrect. incorrect api key or ip address.";
        public static final String OPERATION_NOT_COMPLETED =
                "the operation could not be completed. please try again.";
    }
}
