package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
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
                "MobileBank/2.0.1 (com.aktia.mobilebank; build:174; iOS 10.3.1) Alamofire/4.5.1";
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
        public static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
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
        public static final URL AUTHENTICATION_INIT =
                new URL(GATEWAY_BASE + "//avain/login/mobile/initiate");

        private static final String API_BASE = GATEWAY_BASE + "/mgw";

        public static final URL LOGIN_DETAILS = new URL(API_BASE + "/login/details");

        // current_accounts, savings_accounts
        public static final URL ACCOUNT_LIST_0 = new URL(API_BASE + "/summary");
        // cards, investments, loans
        public static final URL ACCOUNT_LIST_1 = new URL(API_BASE + "/summary2");

        public static final URL ACCOUNT_DETAILS = new URL(API_BASE + "/account/{accountId}");

        public static final URL ACCOUNT_TRANSACTIONS =
                new URL(
                        API_BASE
                                + String.format(
                                        "/account/{%s}/transactionsAndLockedEvents",
                                        HttpParameters.ACCOUNT_ID));

        // Example: `/card/transactions/68A3DD.../2?cardId=FFF9DE9...`
        public static final URL CARD_TRANSACTIONS =
                new URL(API_BASE + "/card/transactions/{evryCardId}/{evryCardVersion}");
    }

    public static class Avain {
        public static final String SOFTWARE_NAME = "Avain";
        public static final String SOFTWARE_VERSION = "2.0.1";
        public static final String PUSH_NOTIFICATION_TOKEN = "dummy-token";
        public static final String CONSENT = "AVAIN_TERMS";
        public static final String AUTHENTICATION_ID_TYPE = "com.evry.sam.appcontext";
    }

    public static final TypeMapper<AccountTypes> TRANSACTIONAL_ACCOUNTS_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder()
                    .put(AccountTypes.CHECKING, "CURRENT_ACCOUNT")
                    .put(AccountTypes.SAVINGS, "SAVINGS_ACCOUNT", "ASP_ACCOUNT")
                    .build();

    public class InstanceStorage {
        public static final String USER_ACCOUNT_INFO = "userAccountInfo";
    }

    public static class Payload {
        public static final String TOKEN = "TOKEN";
    }
}
