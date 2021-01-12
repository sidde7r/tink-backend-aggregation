package se.tink.backend.aggregation.agents.nxgen.se.banks.collector;

import java.util.regex.Pattern;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class CollectorConstants {

    public static class Urls {

        private static final String HOST_AUTHENTICATION = "https://mobiltbankid.collectorbank.se";
        private static final String HOST_USER_DATA = "https://api-bankapp-prod.azurewebsites.net";
        private static final String HOST_TRANSACTIONAL_ACCOUNTS =
                "https://api-olivia-prod.azurewebsites.net";

        public static final URL INIT_BANKID = new URL(HOST_AUTHENTICATION + Endpoints.AUTH_BANKID);
        public static final URL POLL_BANKID = new URL(HOST_AUTHENTICATION + Endpoints.POLL_BANKID);
        public static final URL TOKEN_EXCHANGE = new URL(HOST_USER_DATA + Endpoints.TOKEN_EXCHANGE);

        public static final URL USER = new URL(HOST_USER_DATA + Endpoints.USER);
        public static final URL ACCOUNTS = new URL(HOST_USER_DATA + Endpoints.ACCOUNTS);
        public static final URL PROFILE = new URL(HOST_USER_DATA + Endpoints.PROFILE);
        public static final URL SAVINGS = new URL(HOST_TRANSACTIONAL_ACCOUNTS + Endpoints.SAVINGS);
    }

    public static class Endpoints {
        private static final String AUTH_BANKID = "/api/v3/auth/";
        private static final String POLL_BANKID = "/api/v3/auth/{sessionId}";
        private static final String TOKEN_EXCHANGE = "/mollyToCurityTokenExchange";
        private static final String USER = "/api/User/exists";
        private static final String ACCOUNTS = "/views/v2/cards";
        private static final String PROFILE = "/views/v2/Profile";
        private static final String SAVINGS = "/api/v1/savings/details/{accountId}";
    }

    public static class Headers {
        public static final String AUTH_USERNAME = "bankapp";
        public static final String AUTH_PASSWORD = "aad59bfa-0090-48dc-b33a-304ca072297b";
        public static final String AUTHORIZATION = "Authorization";
    }

    public static class Storage {
        public static final String BEARER_TOKEN = "bearer_token";
        public static final String TRANSACTIONS = "transactions";
    }

    public static class BankIdStatus {
        public static final String OUTSTANDING_TRANSACTION = "outstanding_transaction";
        public static final String USER_SIGN = "user_sign";
        public static final String COMPLETE = "complete";
        public static final String NO_CLIENT = "no_client";
        public static final String CANCELLED = "user_cancel";
    }

    public static class IdTags {
        public static final String ACCOUNT_ID = "accountId";
        public static final String SESSION_ID = "sessionId";
    }

    public static class Currency {
        public static final String SEK = "sek";
    }

    public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder().put(AccountTypes.SAVINGS, "savings").build();

    public static final Pattern UUID_PATTERN =
            Pattern.compile(
                    "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");
}
