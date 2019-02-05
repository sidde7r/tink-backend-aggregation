package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia;

import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.http.URL;

import java.util.Base64;

public class AktiaConstants {
    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();

    public static class Url {
        private static final String BASE_API = "https://mobile-gateway.aktia.fi";
        private static final String BASE_AUTH = "https://mobile-auth.aktia.fi";

        // == START Login ==
        public static final URL AUTHENTICATE = new URL(BASE_AUTH + "/mobileauth/oauth2/access_token?realm=/mobileauth");
        public static final URL LOGIN_CHALLENGE = new URL(BASE_API + "/api/login/details");
        public static final URL FINALIZE_CHALLENGE = new URL(BASE_API + "/api/login/otp/authenticate");
        public static final URL DEVICE_REGISTRATION = new URL(BASE_API + "/api/device/registrations");
        // == END Login

        // == START Accounts ==
        public static final URL SUMMARY = new URL(BASE_API + "/api/summary");
        public static final URL SUMMARY_2 = new URL(BASE_API + "/api/summary2");
        public static final URL ACCOUNT_DETAILS = new URL(BASE_API + "/api/account/{accountId}");
        public static final URL TRANSACTIONS = new URL(BASE_API + "/api/account/{accountId}/transactionsAndLockedEvents");
        // == END Accounts ==

        // == START Investments ==
        public static final URL INVESTMENTS = new URL(BASE_API + "/api/savings/investments");
        // == END Investments ==
    }

    public static class Session {
        public static final String DEVICE_NAME = "Tink";
        private static final String AUTHORIZATION_HEADER_PART_0 = "Basic";
        // These should be base64 encoded before usage
        private static final String AUTHORIZATION_HEADER_PART_1 = "vPzvzS9loHWg";
        private static final String AUTHORIZATION_HEADER_PART_2 = "zvn5vHAXlL2W99epdEejBVypJJujBVVgRBSU5hb4";
        public static final String AUTHORIZATION_HEADER =
                AUTHORIZATION_HEADER_PART_0 + " " + BASE64_ENCODER.encodeToString(
                        (AUTHORIZATION_HEADER_PART_1 + ":" + AUTHORIZATION_HEADER_PART_2).getBytes());

        public static final String NEXT_OTP_CHALLENGE_INDEX_KEY = "NextOtpChallengeIndex";
    }

    public static class LogTags {
        public static final LogTag INVESTMENTS = LogTag.from("Aktia bank - Investments");
        public static final LogTag PRODUCTS_SUMMARY = LogTag.from("Aktia bank - Products summary");
        public static final LogTag TRANSACTIONS_RESPONSE = LogTag.from("Aktia bank - Transactions response");
    }

    public static class Authentication {
        public static final String LOGIN_FAILED_REASON_KEY = "LOGIN_FAILED_REASON";
        public static final String INVALID_USERNAME_OR_PASSWORD_1 = "sts001";
        public static final String INVALID_USERNAME_OR_PASSWORD_2= "sts002";
        public static final String INVALID_USERNAME_OR_PASSWORD_3 = "sts003";
        public static final String ACCOUNT_LOCKED = "sts004";
        public static final String UNKNOWN = "unknown";
        public static final String ENCAP_AUTHORIZATION = "bf-esb-internet";
    }

    public static class Accounts {
        public static final String CHECKING_ACCOUNT = "current_account";
        public static final String SAVINGS_ACCOUNT = "savings_account";
    }
}
