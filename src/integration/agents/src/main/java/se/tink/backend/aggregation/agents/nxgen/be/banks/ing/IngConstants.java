package se.tink.backend.aggregation.agents.nxgen.be.banks.ing;

import java.util.regex.Pattern;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public final class IngConstants {

    public static class Urls {
        private static final String BASE_URL = "https://api.mobile.ing.be";
        private static final String SECURITY_MEANS = "/security/means";
        private static final String SRP_LOGIN = SECURITY_MEANS + "/secure-remote-password/login";

        public static final URL KEY_AGREEMENT =
                URL.of(BASE_URL + SECURITY_MEANS + "/bootstrap/key-agreement");

        public static final URL PROXY = URL.of(BASE_URL + "/proxy");

        public static final URL DEVICE_PROFILE_MEANS =
                URL.of(BASE_URL + SRP_LOGIN + "/variables/profile/{id}/means/DEVICE");

        public static final URL MPIN_PROFILE_MEANS =
                URL.of(BASE_URL + SRP_LOGIN + "/variables/profile/{id}/means/MPIN");

        public static final URL EVIDENCE_SESSION =
                URL.of(BASE_URL + SRP_LOGIN + "/evidence/session/{id}");
    }

    public static class Headers {
        public static final String X_ING_BOOTSTRAP_VERSION = "X-ING-Bootstrap-Version";
        public static final String X_ING_BOOTSTRAP_VERSION_VALUE = "1";

        public static final String USER_AGENT_VALUE =
                "BEOneApp/20200908.130418 CFNetwork/978.0.7 Darwin/18.7.0";

        public static final String ACCEPT_LANGUAGE_VALUE = "en-GB";
        public static final String APP_IDENTIFIER_VALUE = "be.ING.OneApp";
        public static final String APP_VERSION_VALUE = "1.18.0";
        public static final String OS_VERSION_VALUE = "13.2.2";
        public static final String DEVICE_PLATFORM_VALUE = "IOS";
        public static final String DEVICE_MODEL_VALUE = "iPhone9,3";
    }

    public static class Storage {
        public static final String CLIENT_PRIVATE_KEY = "CLIENT_PRIVATE_KEY";
        public static final String CLIENT_PUBLIC_KEY = "CLIENT_PUBLIC_KEY";
        public static final String SERVER_PUBLIC_KEY = "SERVER_PUBLIC_KEY";
        public static final String ENCRYPTION_KEY = "ENCRYPTION_KEY";
        public static final String SIGNING_KEY = "SIGNING_KEY";
        public static final String ACCESS_TOKEN = "ACCESS_TOKEN";
        public static final String REFRESH_TOKEN = "REFRESH_TOKEN";
        public static final String ENROLL_DEVICE_PINNING_PRIVKEY = "ENROLL_DEVICE_PINNING_PRIVKEY";
        public static final String ENROLL_DEVICE_PINNING_PUBKEY = "ENROLL_DEVICE_PINNING_PUBKEY";
        public static final String ENROLL_SIGNING_PRIVKEY = "ENROLL_SIGNING_PRIVKEY";
        public static final String ENROLL_SIGNING_PUBKEY = "ENROLL_SIGNING_PUBKEY";
        public static final String MOBILE_APP_ID = "MOBILE_APP_ID";
        public static final String BASKET_ID = "BASKET_ID";
        public static final String CHALLENGE = "CHALLENGE";
        public static final String MPIN_SALT = "MPIN_SALT";
        public static final String DEVICE_SALT = "DEVICE_SALT";
        public static final String SRP6_PASSWORD = "SRP6_PASSWORD";
        public static final String OTP = "OTP";
        public static final String TRANSACTIONS_HREF = "TRANSACTIONS_HREF";
    }

    public static class Types {
        public static final String CURRENT = "CURRENT";
        public static final String SAVINGS = "SAVINGS";
        public static final String CARD = "CARD";
        public static final String LOAN = "LOAN";
        public static final String INVESTMENT = "INVESTMENT";
        public static final String INVESTMENT_MONEY_ACCOUNT = "INVESTMENT_MONEY_ACCOUNT";
    }

    public static final Pattern TRANSACTION_PREFIXES =
            Pattern.compile(
                    "^((naar:|zu:|á:|to:|van:|von:|de:|from:|vers:)|(\\d{2}/\\d{2}\\s-\\s\\d{1,2}h\\d{2}\\s+-\\s+)).*",
                    Pattern.CASE_INSENSITIVE);

    public static final String SERVER_SIGNING_KEY_ID = "20180822";
    public static final String CLIENT_ID = "706711ed-8111-4d65-a035-7441083e2079";
    public static final byte[] SAFE_PRIME =
            EncodingUtils.decodeHexString(
                    "EEAF0AB9ADB38DD69C33F80AFA8FC5E86072618775FF3C0B9EA2314C9C256576"
                            + "D674DF7496EA81D3383B4813D692C6E0E0D5D8E250B98BE48E495C1D6089DAD1"
                            + "5DC7D7B46154D6B6CE8EF4AD69B15D4982559B297BCF1885C529F566660E57EC"
                            + "68EDBC3C05726CC02FD4CBF4976EAA9AFD5138FE8376435B9FC61D2FC0EB06E3");
    public static final byte[] GROUP_PARAM = EncodingUtils.decodeHexString("02");

    public static final int MAX_RETRIES = 2;
    public static final int THROTTLING_DELAY = 2000;
}
