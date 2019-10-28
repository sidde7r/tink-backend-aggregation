package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;

public class NordeaPartnerConstants {

    public static final String DATE_FORMAT = "yyyy-MM-dd";

    public static final AccountTypeMapper ACCOUNT_TYPE_MAPPER =
            AccountTypeMapper.builder()
                    .put(AccountTypes.CHECKING, "TRANSACTION")
                    .put(AccountTypes.SAVINGS, "SAVINGS")
                    .build();

    public class EndPoints {
        public static final String PARTNER_PATH = "/partners/{partner_id}";
        public static final String TOKEN = PARTNER_PATH + "/user/token";
        public static final String ACCOUNTS = PARTNER_PATH + "/accounts";
        public static final String ACCOUNT_TRANSACTIONS = ACCOUNTS + "/{account_id}/transactions";
        public static final String PAYMENTS = PARTNER_PATH + "/payments";
    }

    public class PathParamsKeys {
        public static final String PARTNER_ID = "partner_id";
        public static final String ACCOUNT_ID = "account_id";
    }

    public class QueryParamsKeys {
        public static final String CONTINUATION_KEY = "continuation_key";
    }

    public class HeaderValues {
        public static final String ACCEPT_LANGUAGE = "en-SE";
    }

    public class StorageKeys {
        public static final String PARTNER_USER_ID = "partner-user-id";
    }

    public class SupplementalInfoKeys {
        public static final String TOKEN = "token";
    }

    public class Jwt {
        public static final String ISSUER = "Tink";
        public static final String JWT_CONTENT_TYPE = "JWT";
        public static final long TOKEN_LIFETIME_SECONDS = 300;
    }

    public static class SupplementalFields {
        public static final Field TOKEN =
                Field.builder()
                        .name(SupplementalInfoKeys.TOKEN)
                        .description("Nordea JWE token")
                        .build();
    }

    public static class HttpFilters {
        public static final int RETRY_SLEEP_MILLISECONDS = 5000;
        public static final int MAX_NUM_RETRIES = 3;
    }
}
