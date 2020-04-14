package se.tink.backend.aggregation.agents.nxgen.se.business.nordea;

import com.google.common.collect.ImmutableMap;

public class NordeaSEConstants {
    public static class Urls {
        public static final String BASE_URL = "https://se.smemobilebank.prod.nordea.com";

        public static final String INIT_BANKID = BASE_URL + Endpoints.INIT_BANKID;
        public static final String POLL_BANKID = BASE_URL + Endpoints.POLL_BANKID;
        public static final String FETCH_TOKEN = BASE_URL + Endpoints.FETCH_TOKEN;
    }

    public static class Endpoints {
        public static final String INIT_BANKID =
                "/SE/MobileBankIdServiceV1.1/MobileBankIdInitialAuthentication";
        public static final String POLL_BANKID =
                "/SE/MobileBankIdServiceV1.1/MobileBankIdAuthenticationResult/";
        public static final String FETCH_TOKEN = "/SE/AuthenticationServiceV1.1/SecurityToken";
    }

    public static class Headers {
        public static final String REQUEST_ID = "x-Request-Id";
        public static final String SECURITY_TOKEN = "x-Security-Token";
    }

    public static final ImmutableMap<String, Object> NORDEA_CUSTOM_HEADERS =
            ImmutableMap.<String, Object>builder()
                    .put("x-App-Country", "SE")
                    .put("x-App-Language", "en")
                    .put("x-App-Name", "SME")
                    .put("x-App-Version", "1.3.5-18")
                    .put("x-Device-Make", "Apple")
                    .put("x-Device-Model", "iPhone9,4")
                    .put("x-Platform-Type", "iOS")
                    .put("x-Platform-Version", "13.3.1")
                    .put("User-Agent", "SMEMobileBankSE/18 CFNetwork/1121.2.2 Darwin/19.3.0")
                    .build();

    public static class StorageKeys {
        public static final String SECURITY_TOKEN = "security_token";
    }

    public static class BankIdStatus {
        public static final String COMPLETE = "COMPLETE";
        public static final String WAITING = "OUTSTANDING_TRANSACTION";
        public static final String USER_SIGNING = "USER_SIGN";
    }

    public static class ErrorCodes {
        public static final String NO_CLIENT = "MBS8636"; // BankId no client
    }
}
