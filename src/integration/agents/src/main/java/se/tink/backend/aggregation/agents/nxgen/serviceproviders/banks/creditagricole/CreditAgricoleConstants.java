package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class CreditAgricoleConstants {

    public static final String DATE_FORMAT = "yyyyMMdd";

    public static final class Url {
        private static final String BASE_URL =
                "https://ibudget.iphone.credit-agricole.fr/budget5/iphoneservice";

        static final String ACCESSIBILITY_GRID = BASE_URL + "/authentication/accessibility/grid";
        static final String AUTHENTICATE = BASE_URL + "/authentication/{regionId}/authenticate";
        static final String OTP_REQUEST = BASE_URL + "/otp/{regionId}/request";
        static final String FIND_PROFILES = BASE_URL + "/user/{regionId}/find";
        static final String CREATE_PROFILE = BASE_URL + "/user/{regionId}/create";

        static final URL RESTORE_PROFILE =
                new URL(
                        BASE_URL
                                + String.format(
                                        "/user/{%s}/{%s}/{%s}/restore",
                                        StorageKey.USER_ID,
                                        StorageKey.REGION_ID,
                                        StorageKey.PARTNER_ID));
        static final URL VALIDATE_IBAN =
                new URL(
                        BASE_URL
                                + String.format(
                                        "/portfolio/{%s}/{%s}/{%s}/codeBic",
                                        StorageKey.USER_ID,
                                        StorageKey.REGION_ID,
                                        StorageKey.PARTNER_ID));
        static final URL ADD_BENEFICIARY =
                new URL(
                        BASE_URL
                                + String.format(
                                        "/portfolio/{%s}/{%s}/{%s}/externalAccount",
                                        StorageKey.USER_ID,
                                        StorageKey.REGION_ID,
                                        StorageKey.PARTNER_ID));
        static final URL CONTRACTS =
                new URL(
                        BASE_URL
                                + String.format(
                                        "/portfolio/{%s}/{%s}/{%s}/contracts",
                                        StorageKey.USER_ID,
                                        StorageKey.REGION_ID,
                                        StorageKey.PARTNER_ID));
        static final URL OPERATIONS =
                new URL(
                        BASE_URL
                                + String.format(
                                        "/portfolio/{%s}/{%s}/{%s}/accounts/{%s}/operations",
                                        StorageKey.USER_ID,
                                        StorageKey.REGION_ID,
                                        StorageKey.PARTNER_ID,
                                        StorageKey.ACCOUNT_NUMBER)); // takes query param
    }

    public static class Authorization {
        static String HEADER = "Authorization";
        static String BASIC_PREFIX = "Basic ";
    }

    // This data should only be used internally by us, and changing it
    // should thus not affect communication with the bank
    public static class StorageKey {
        public static final String REGION_ID = "regionId";
        public static final String USER_ACCOUNT_NUMBER = "userAccountNumber";
        public static final String USER_ACCOUNT_CODE = "userAccountCode";
        public static final String PROFILE_PIN = "profilePin";
        public static final String PARTNER_ID = "partnerId";
        public static final String USER_ID = "userId";
        public static final String EMAIL = "email";
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
        public static final String ACCOUNT_CODE = "accountCode";
        public static final String ACCOUNT_NUMBER = "accountNumber";
        public static final String PASSWORD = "password";
        public static final String EMAIL = "email";
        public static final String LL_TOKEN = "llToken";
        public static final String ADD_EXTERNAL_IBAN = "add_external_iban";
        public static final String OTP_GRANT_TYPE = "otp_sms";
        public static final String LOGIN_REQUEST_MARKER = "\"scope\":\"login\",\"llToken\"";
    }

    public static class AccountType {
        public static final String CHECKING = "Compte courant";
        public static final String SAVINGS = "Epargne disponible";
        public static final AccountTypeMapper ACCOUNT_TYPE_MAPPER =
                AccountTypeMapper.builder()
                        .put(AccountTypes.SAVINGS, SAVINGS)
                        .put(AccountTypes.CHECKING, CHECKING)
                        .build();
    }

    public static class ErrorCode {
        public static final String INCORRECT_CREDENTIALS = "fr.mabanque.auth.wrongpassword";
        public static final String PASSWORD_AUTH_REQUIRED =
                "fr.mabanque.login.passwordauthenticationrequired";
        public static final String GENERIC = "fr.mabanque.auth.generic";
        public static final String SCA_REQUIRED = "fr.mabanque.createuser.scarequired";
        public static final String FUNCTIONAL_ERROR = "FonctionnalError";
        public static final String BAM_AUTH_REQUIRED = "BamAuthenticationRequired";
        public static final String TECHNICAL_ERROR = "TechnicalError";
        public static final String NO_SCA_METHOD = "fr.mabanque.createuser.noscamethodavailable";
        public static final String INVALID_OTP = "fr.mabanque.otp.invalid";
        public static final String ACCOUNT_BLOCKED = "fr.mabanque.auth.burned";
        public static final String POST_OTP_REQUEST = "postOtpRequest";
    }

    public static class Step {
        public static final String AUTHORIZE = "AUTHORIZE";
        public static final String ADD_BENEFICIARY = "ADD_BENEFICIARY";
    }
}
