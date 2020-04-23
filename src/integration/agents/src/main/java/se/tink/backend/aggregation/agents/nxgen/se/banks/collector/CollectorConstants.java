package se.tink.backend.aggregation.agents.nxgen.se.banks.collector;

import se.tink.backend.aggregation.nxgen.http.url.URL;

public class CollectorConstants {

    public static class Urls {
        private static final String HOST_AUTHENTICATION = "https://mobiltbankid.collectorbank.se";
        private static final String HOST_USER_DATA = "https://api-bankapp-prod.azurewebsites.net";

        public static final URL INIT_BANKID = new URL(HOST_AUTHENTICATION + Endpoints.AUTH_BANKID);
        public static final URL POLL_BANKID = new URL(HOST_AUTHENTICATION + Endpoints.POLL_BANKID);
        public static final URL TOKEN_EXCHANGE = new URL(HOST_USER_DATA + Endpoints.TOKEN_EXCHANGE);
    }

    public static class Endpoints {
        private static final String AUTH_BANKID = "/api/v3/auth/";
        private static final String POLL_BANKID = "/api/v3/auth/{sessionId}";
        private static final String TOKEN_EXCHANGE = "/mollyToCurityTokenExchange";
    }

    public static class Headers {
        public static final String AUTH_USERNAME = "bankapp";
        public static final String AUTH_PASSWORD = "aad59bfa-0090-48dc-b33a-304ca072297b";
        public static final String AUTHORIZATION = "Authorization";
    }

    public static class Storage {
        public static final String BEARER_TOKEN = "bearer_token";
    }

    public static class BankIdStatus {
        public static final String OUTSTANDING_TRANSACTION = "outstanding_transaction";
        public static final String USER_SIGN = "user_sign";
        public static final String COMPLETE = "complete";
        public static final String NO_CLIENT = "no_client";
    }

    public static class IdTags {
        public static final String SESSION_ID = "sessionId";
    }
}
