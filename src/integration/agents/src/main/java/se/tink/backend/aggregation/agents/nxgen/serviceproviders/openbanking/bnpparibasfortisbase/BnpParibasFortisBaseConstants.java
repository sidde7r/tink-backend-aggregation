package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibasfortisbase;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;

public final class BnpParibasFortisBaseConstants {

    public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder()
                    .put(AccountTypes.CHECKING, "CACC")
                    .put(AccountTypes.SAVINGS, "SVGS")
                    .build();

    public static class Endpoints {
        public static final String PSD2_BASE_PATH = "/psd2/v2";
        public static final String OAUTH = "/authorize";
        public static final String TOKEN = "/token";
        public static final String ACCOUNTS = "/accounts";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = "OAUTH_TOKEN";
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

    public static class HeaderKeys {
        public static final String REQUEST_ID = "X-Request-ID";
        public static final String SIGNATURE = "Signature";
        public static final String DIGEST = "Digest";
        public static final String USER_AGENT = "User-Agent";
    }

    public static class HeaderValues {
        public static final String APPLICATION_HAL_JSON = "application/hal+json";
        public static final String TINK = "Tink";
    }

    public static class FormValues {
        public static final String GRANT_TYPE = "authorization_code";
        public static final String SCOPE = "aisp";
    }

    public static class Accounts {
        public static final String BALANCE_TYPE_OTHER = "OTHR";
    }

    public static class Transactions {
        public static final String PENDING_STATUS = "PDNG";
    }

    public static class IdTags {
        public static final String PAYMENT_ID = "paymentId";
    }

    public static class Errors {
        public static final String NO_ELIGIBLE_ACCOUNTS = "No eligible accounts";
    }
}
