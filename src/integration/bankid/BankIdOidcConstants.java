package src.integration.bankid;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BankIdOidcConstants {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Urls {

        private static final String TINK_PRODUCTION_CDN = "https://cdn.tink.se/";
        private static final String TINK_LOCAL_CDN = "https://127.0.0.1:7357/";

        private static final String BANK_ID_IFRAME_PAGE = "bankid/bankid-iframe.html";

        public static String getBankIdIframePage(boolean isInTestContext) {
            return getTinkCdnBase(isInTestContext) + BANK_ID_IFRAME_PAGE;
        }

        private static String getTinkCdnBase(boolean isInTestContext) {
            return isInTestContext ? TINK_LOCAL_CDN : TINK_PRODUCTION_CDN;
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class QueryParamKeys {
        public static final String STATE = "state";
        public static final String IFRAME_URL = "iframeUrl";
        public static final String CALLBACK_URL = "callbackUrl";
    }
}
