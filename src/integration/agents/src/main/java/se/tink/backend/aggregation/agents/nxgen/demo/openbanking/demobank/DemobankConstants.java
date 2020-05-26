package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank;

public class DemobankConstants {

    public static class Urls {
        public static final String BASE_URL = "https://demobank.production.global.tink.se";
        public static final String OAUTH_TOKEN = "/oauth/token";
        public static final String OAUTH_AUTHORIZE = "/oauth/authorize";
        public static final String ACCOUNTS = "/api/accounts";
        public static final String TRANSACTIONS = "/api/account/{accountId}/transactions";
    }

    public static class QueryParams {
        public static final String RESPONSE_TYPE = "response_type";
        public static final String CLIENT_ID = "client_id";
        public static final String STATE = "state";
        public static final String ACCOUNT_ID = "accountId";
        public static final String DATE_FROM = "from";
        public static final String DATE_TO = "to";
        public static final String REDIRECT_URI = "redirect_uri";
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

    public static class ProviderNameRegex {
        public static final String PASSWORD_PROVIDER = "^.*-demobank-password";
    }

    public static class ClusterIds {
        public static final String OXFORD_PREPROD = "oxford-preprod";
        public static final String OXFORD_STAGING = "oxford-staging";
    }

    public static class AccountTypes {
        public static final String CHECKING = "CHECKING";
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
