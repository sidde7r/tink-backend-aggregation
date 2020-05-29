package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole;

import se.tink.backend.aggregation.nxgen.http.url.URL;

public class CreditAgricoleConstants {

    public static final class Url {
        private static final String BASE_URL =
                "https://ibudget.iphone.credit-agricole.fr/budget5/iphoneservice";

        public static final String ACCESSIBILITY_GRID =
                BASE_URL + "/authentication/accessibility/grid";
        public static final String AUTHENTICATE =
                BASE_URL + "/authentication/{regionId}/authenticate";
        public static final String FIND_PROFILES = BASE_URL + "/user/{regionId}/find";
        public static final String CREATE_PROFILE = BASE_URL + "/user/{regionId}/create";

        public static final URL SELECT_REGION =
                new URL(
                        BASE_URL
                                + String.format(
                                        "/budget5/iphoneservice/vitrine/{%s}/isResetPwdBamActive",
                                        StorageKey.REGION_ID));
        public static final URL NUMBER_PAD =
                new URL(BASE_URL + "/budget5/iphoneservice/authentication/grid");
        public static final URL SIGN_IN =
                new URL(
                        BASE_URL
                                + String.format(
                                        "/budget5/iphoneservice/user/{%s}/search",
                                        StorageKey.REGION_ID));
        public static final URL USER_AGREEMENT =
                new URL(
                        BASE_URL
                                + String.format(
                                        "/budget5/iphoneservice/about/{%s}?lastUpdate=0",
                                        StorageKey.REGION_ID));
        public static final URL APP_CODE =
                new URL(
                        BASE_URL
                                + String.format(
                                        "/budget5/iphoneservice/user/{%s}/{%s}/{%s}/restore",
                                        StorageKey.USER_ID,
                                        StorageKey.REGION_ID,
                                        StorageKey.PARTNER_ID));
        public static final URL STRONG_AUTHENTICATION =
                new URL(
                        BASE_URL
                                + String.format(
                                        "/budget5/iphoneservice/authentication/{%s}/strong",
                                        StorageKey.REGION_ID));
        public static final URL KEEP_ALIVE =
                new URL(
                        BASE_URL
                                + String.format(
                                        "/budget5/iphoneservice/authentication/{%s}/{%s}/longSessionProfile",
                                        StorageKey.USER_ID, StorageKey.REGION_ID));
        public static final URL CONTRACTS =
                new URL(
                        BASE_URL
                                + String.format(
                                        "/budget5/iphoneservice/portfolio/{%s}/{%s}/{%s}/contracts",
                                        StorageKey.USER_ID,
                                        StorageKey.REGION_ID,
                                        StorageKey.PARTNER_ID));
        public static final URL OPERATIONS =
                new URL(
                        BASE_URL
                                + String.format(
                                        "/budget5/iphoneservice/portfolio/{%s}/{%s}/{%s}/accounts/{%s}/operations",
                                        StorageKey.USER_ID,
                                        StorageKey.REGION_ID,
                                        StorageKey.PARTNER_ID,
                                        StorageKey.ACCOUNT_NUMBER)); // takes query param
        // startDate={BASIC_ISO_DATE}
    }

    // This data is used in query params, and may not be changed
    // without affecting communication with the bank
    public static class QueryParam {
        public static final String USER_ACCOUNT_CODE = "accountCode";
        public static final String USER_ACCOUNT_NUMBER = "accountNumber";
    }

    public static class Authorization {
        public static String HEADER = "Authorization";
        public static String BASIC_PREFIX = "Basic ";
    }

    // This data should only be used internally by us, and changing it
    // should thus not affect communication with the bank
    public static class StorageKey {
        public static final String REGION_ID = "c";
        public static final String USER_ACCOUNT_NUMBER = "userAccountNumber";
        public static final String USER_ACCOUNT_CODE = "userAccountCode";
        public static final String SHUFFLED_USER_ACCOUNT_CODE = "shuffledAccountCode";
        public static final String APP_CODE = "appCode";
        public static final String PARTNER_ID = "partnerId";
        public static final String USER_ID = "userId";
        public static final String LOGIN_EMAIL = "login";
        public static final String SL_TOKEN = "slToken";
        public static final String LL_TOKEN = "llToken";
        public static final String ACCOUNT_NUMBER = "accountNumber";
        public static final String NUMPAD_SEQUENCE = "numpadSequence";
        public static final String PUBLIC_KEY = "publicKey";
        public static final String IS_DEVICE_REGISTERED = "isDeviceRegistered";
    }

    // This data is used in request body forms, and may not be changed
    // without affecting communication with the bank
    public static class Form {
        public static final String USER_ACCOUNT_CODE = "accountCode";
        public static final String USER_ACCOUNT_NUMBER = "accountNumber";
        public static final String APP_CODE = "password";
        public static final String LOGIN_EMAIL = "login";
        public static final String LL_TOKEN = "llToken";
    }

    public static final class AccountType {
        public static final String CHECKING = "Compte courant";
    }

    public static class ErrorCode {
        public static String INCORRECT_CREDENTIALS = "fr.mabanque.auth.wrongpassword";
        public static String PASSWORD_AUTH_REQUIRED =
                "fr.mabanque.login.passwordauthenticationrequired";
        public static String SCA_REQUIRED = "fr.mabanque.createuser.scarequired";
    }

    public static final String DATE_FORMAT = "yyyyMMdd";
}
