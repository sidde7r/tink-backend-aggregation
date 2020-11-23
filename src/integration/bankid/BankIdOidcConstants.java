package src.integration.bankid;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BankIdOidcConstants {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Urls {
        public static final String AGENT_TEST_SERVER_THIRD_PARTY_CALLBACK =
                "https://127.0.0.1:7357/api/v1/thirdparty/callback";

        private static final String AGENT_TEST_SERVER_BASE = "https://127.0.0.1:7357/";
        private static final String TINK_PRODUCTION_CDN_BASE = "https://cdn.tink.se/";

        private static final String BANK_ID_IFRAME_PAGE_PATH = "bankid/bankid-iframe.html";

        public static String getBankIdIframePage(boolean isInTestContext) {
            return getTinkCdnBase(isInTestContext) + BANK_ID_IFRAME_PAGE_PATH;
        }

        private static String getTinkCdnBase(boolean isInTestContext) {
            return isInTestContext ? AGENT_TEST_SERVER_BASE : TINK_PRODUCTION_CDN_BASE;
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class QueryParamKeys {
        public static final String STATE = "state";
        public static final String IFRAME_URL = "iframeUrl";
        public static final String CALLBACK_URL = "callbackUrl";
    }
}
