package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;

public abstract class BnpParibasFortisConstants {

    public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
        TypeMapper.<AccountTypes>builder()
            .put(AccountTypes.CHECKING, "CACC")
            .put(AccountTypes.SAVINGS, "SVGS")
            .put(AccountTypes.OTHER, "OTHR")
            .build();

    public static class Urls {

        public static final String OAUTH = "/authorize";
        public static final String TOKEN = "/token";
        public static final String ACCOUNTS = "/v1/accounts";
    }

    public static class StorageKeys {

        public static final String AUTH_BASE_URL = "AUTH_BASE_URL";
        public static final String CLIENT_ID = "CLIENT_ID";
        public static final String REDIRECT_URI = "REDIRECT_URI";
        public static final String CLIENT_SECRET = "CLIENT_SECRET";
        public static final String ORGANIZATION_ID = "ORGANIZATION_ID";
        public static final String OPENBANK_STET_VERSION = "OPENBANK_STET_VERSION";
        public static final String OAUTH_TOKEN = "OAUTH_TOKEN";
        public static final String API_BASE_URL = "API_BASE_URL";
        public static final String ACCOUNT_LINKS = "ACCOUNT_LINKS";
    }

    public static class QueryKeys {

        public static final String RESPONSE_TYPE = "response_type";
        public static final String CLIENT_ID = "client_id";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String SCOPE = "scope";
        public static final String STATE = "state";
    }

    public static class QueryValues {

        public static final String RESPONSE_TYPE = "code";
        public static final String SCOPE = "aisp";
    }

    public static class Market {

        public static final String INTEGRATION_NAME = "bnpparibasfortis";
        public static final String CLIENT_NAME = "tink";
    }

    public static class HeaderKeys {

        public static final String ORGANIZATION_ID = "X-Openbank-Organization";
        public static final String OPENBANK_STET_VERSION = "X-Openbank-Stet-Version";
        public static final String REQUEST_ID = "X-Request-ID";
        public static final String SIGNATURE = "Signature";
        public static final String APPLICATION_HAL_JSON = "application/hal+json";
    }

    public static class FormValues {

        public static final String GRANT_TYPE = "authorization_code";
        public static final String SCOPE = "aisp";
    }

    public static class Accounts {

        public static final String OTHR_BALANCE_TYPE = "OTHR";
    }

    public static class Transactions {

        public static final String PENDING_STATUS = "PDNG";
    }
}
