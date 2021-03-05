package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia;

import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.utils.deviceprofile.DeviceProfile;
import se.tink.backend.aggregation.utils.deviceprofile.DeviceProfileConfiguration;

public class AktiaConstants {
    public static final DeviceProfile DEVICE_PROFILE = DeviceProfileConfiguration.IOS_STABLE;

    public static class HttpHeaders {
        public static final String OTP_INDEX = "otpIndex";
        public static final String OTP_CARD = "otpCard";
        public static final String LOGIN_STATUS = "Login-Status";

        public static final String BASIC_AUTH_USERNAME = "vPzvzS9loHWg";
        public static final String BASIC_AUTH_PASSWORD = "zvn5vHAXlL2W99epdEejBVypJJujBVVgRBSU5hb4";

        public static final String USER_AGENT =
                "MobileBank/2.10.0 (com.aktia.mobilebank; build 267; iOS 12.4.0)";
    }

    public static class Oauth2Scopes {
        public static final String REGISTRATION_INIT = "AVAIN_REGISTRATION";
        public static final String AUTHENTICATION_INIT = "AVAIN";
        public static final String AUTHENTICATION_COMPLETE = "AVAIN_MGW";
    }

    public static class HttpParameters {
        public static final String OAUTH2_USERNAME = "anonymous";
        public static final String OAUTH2_GRANT_TYPE = "password";

        public static final String ACCOUNT_ID = "accountId";
        public static final String PAGE_KEY = "continuationKey";
    }

    public static class ErrorCodes {
        public static final String INVALID_CREDENTIALS = "INVALID_LOGIN";
        public static final String INVALID_GRANT = "invalid_grant";
    }

    public static class Url {
        private static final String OAUTH2_BASE =
                "https://mobile-auth.aktia.fi/mobileauth/oauth2/access_token?realm=/mobileauth&scope=";

        public static final URL OAUTH2_REGISTRATION_INIT =
                new URL(OAUTH2_BASE + Oauth2Scopes.REGISTRATION_INIT);
        public static final URL OAUTH2_AUTHENTICATION_INIT =
                new URL(OAUTH2_BASE + Oauth2Scopes.AUTHENTICATION_INIT);
        public static final URL OAUTH2_AUTHENTICATION_COMPLETE =
                new URL(OAUTH2_BASE + Oauth2Scopes.AUTHENTICATION_COMPLETE);

        private static final String GATEWAY_BASE = "https://mobile-gateway.aktia.fi/api";

        // Note: it's meant to be double slashes for these urls.
        public static final URL REGISTRATION_INIT =
                new URL(GATEWAY_BASE + "//avainregistration/initiate");
        public static final URL REGISTRATION_COMPLETE =
                new URL(GATEWAY_BASE + "//avainregistration/complete");
        public static final URL AVAIN_INFO = new URL(GATEWAY_BASE + "//avain/avain-info");
        public static final URL AUTHENTICATION_INIT =
                new URL(GATEWAY_BASE + "//avain/login/mobile/initiate");
        public static final URL GET_PHONE_NUMBER =
                new URL(GATEWAY_BASE + "//avain/login/getPhoneNumber");
        public static final URL INITIATE_CHALLENGE =
                new URL(GATEWAY_BASE + "//avain/login/initiateChallenge");
        public static final URL VERIFY_CHALLENGE =
                new URL(GATEWAY_BASE + "//avain/login/verifyChallenge");

        private static final String API_BASE = GATEWAY_BASE + "/mgw";

        public static final URL LOGIN_DETAILS = new URL(API_BASE + "/login/details");

        // current_accounts, savings_accounts
        public static final URL ACCOUNT_LIST_0 = new URL(API_BASE + "/summary");

        public static final URL ACCOUNT_TRANSACTIONS =
                new URL(
                        API_BASE
                                + String.format(
                                        "/account/{%s}/transactionsAndLockedEvents",
                                        HttpParameters.ACCOUNT_ID));
    }

    public static class Avain {
        public static final String SOFTWARE_NAME = "Avain";
        public static final String SOFTWARE_VERSION = "2.10.0";
        public static final String PUSH_NOTIFICATION_TOKEN = "dummy-token";
        public static final String CONSENT = "AVAIN_TERMS";
        public static final String AUTHENTICATION_ID_TYPE = "com.evry.sam.appcontext";
        public static final String CHALLENGE_TYPE = "SMS_ONBOARDING";
    }

    public static final TypeMapper<TransactionalAccountType> TRANSACTIONAL_ACCOUNTS_TYPE_MAPPER =
            TypeMapper.<TransactionalAccountType>builder()
                    .put(TransactionalAccountType.CHECKING, "CURRENT_ACCOUNT")
                    .put(TransactionalAccountType.SAVINGS, "SAVINGS_ACCOUNT", "ASP_ACCOUNT")
                    .build();

    public class InstanceStorage {
        public static final String USER_ACCOUNT_INFO = "userAccountInfo";
    }

    public static class Payload {
        public static final String TOKEN = "TOKEN";
    }
}
