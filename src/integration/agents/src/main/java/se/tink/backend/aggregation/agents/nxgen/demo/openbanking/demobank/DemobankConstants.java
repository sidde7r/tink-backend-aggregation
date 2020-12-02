package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank;

import java.time.temporal.ChronoUnit;
import se.tink.backend.aggregation.nxgen.core.account.AccountHolderType;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;

public class DemobankConstants {
    public static final TypeMapper<AccountHolderType> HOLDER_TYPE_TYPE_MAPPER =
            TypeMapper.<AccountHolderType>builder()
                    .put(AccountHolderType.PERSONAL, "PERSONAL")
                    .put(AccountHolderType.BUSINESS, "BUSINESS")
                    .put(AccountHolderType.CORPORATE, "CORPORATE")
                    .build();

    public static final ChronoUnit DEFAULT_OB_TOKEN_LIFETIME_UNIT = ChronoUnit.DAYS;
    public static final int DEFAULT_OB_TOKEN_LIFETIME = 90;

    public static class Urls {
        public static final String BASE_URL = "https://demobank.production.global.tink.se";
        public static final String OAUTH_TOKEN = "/oauth/token";
        public static final String OAUTH_AUTHORIZE = "/oauth/authorize";
        public static final String ACCOUNTS = "/api/accounts";
        public static final String TRANSACTIONS = "/api/account/{accountId}/transactions";
        public static final String HOLDERS = "/api/account/{accountId}/holders";
        public static final String USER = "/api/user";

        // App to app URLs
        public static final String A2A_INIT_URL = "/api/auth/ticket";
        public static final String A2A_INIT_DECOUPLED_URL = "/api/auth/ticket/decoupled";
        public static final String A2A_COLLECT_URL = "/api/auth/ticket/{ticketId}/collect";

        // Norwegian BankID mocks
        public static final String NO_BANKID_INIT = "/auth/no/init";
        public static final String NO_BANKID_COLLECT = "/auth/no/collect";

        // Denmark NemID mocks
        public static final String DK_NEMID_GET_CHALLENGE =
                "/auth/dk/mobilbank/nemid/get_challange";
        public static final String DK_NEMID_GENERATE_CODE = "/auth/dk/mobilbank/nemid/generatecode";
        public static final String DK_NEMID_ENROLL = "/auth/dk/mobilbank/nemid/inroll";
        public static final String DK_NEMID_LOGIN =
                "/auth/dk/mobilbank/nemid/login_with_installid_prop";

        // Embedded OTP
        public static final String EMBEDDED_OTP_COMMENCE = "/api/auth/otplogin/commence";
        public static final String EMBEDDED_OTP_COMPLETE = "/api/auth/otplogin/complete";
    }

    public static class QueryParams {
        public static final String RESPONSE_TYPE = "response_type";
        public static final String CLIENT_ID = "client_id";
        public static final String STATE = "state";
        public static final String DATE_FROM = "from";
        public static final String DATE_TO = "to";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String SCOPE = "scope";

        public static final String ACCOUNT_ID = "accountId";
        public static final String TICKET_ID = "ticketId";
    }

    public static class OAuth2Params {
        public static final String CLIENT_ID = "aggregation";
        public static final String CLIENT_SECRET = "password";
    }

    public static class QueryParamsValues {
        public static final String RESPONSE_TYPE = "code";
        public static final String CLIENT_ID = OAuth2Params.CLIENT_ID;
    }

    public static class StorageKeys {
        public static final String OAUTH2_TOKEN = "oAuth2Token";
    }

    public static class ClusterIds {
        public static final String OXFORD_PREPROD = "oxford-preprod";
        public static final String OXFORD_STAGING = "oxford-staging";
    }

    public static class AccountTypes {
        public static final String CHECKING = "CHECKING";
        public static final String CREDIT_CARD = "CREDIT_CARD";
    }

    public static class ClusterSpecificCallbacks {

        public static final String OXFORD_STAGING_CALLBACK =
                "https://main.staging.oxford.tink.se/api/v1/credentials/third-party/callback";
        public static final String OXFORD_PREPROD_CALLBACK =
                "https://api.preprod.oxford.tink.com/api/v1/credentials/third-party/callback";
        public static final String OXFORD_PROD_CALLBACK =
                "https://api.tink.com/api/v1/credentials/third-party/callback";
    }
}
