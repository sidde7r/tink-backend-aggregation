package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner;

import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.enums.AccountFlag;

public class NordeaPartnerConstants {

    public static final String INTEGRATION_NAME = "nordeapartner";

    public static final TransactionalAccountTypeMapper TRANSACTIONAL_ACCOUNT_TYPE_MAPPER =
            TransactionalAccountTypeMapper.builder()
                    .put(
                            TransactionalAccountType.CHECKING,
                            AccountFlag.PSD2_PAYMENT_ACCOUNT,
                            "TRANSACTION")
                    .put(TransactionalAccountType.SAVINGS, "SAVINGS")
                    .build();

    public class EndPoints {
        public static final String PARTNER_PATH = "/partners/{partner_id}";
        public static final String ACCOUNTS = PARTNER_PATH + "/accounts";
        public static final String ACCOUNT_TRANSACTIONS = ACCOUNTS + "/{account_id}/transactions";
        public static final String CARDS = PARTNER_PATH + "/cards";
        public static final String CARD_TRANSACTIONS = CARDS + "/{card_id}/transactions";
    }

    public class PathParamsKeys {
        public static final String PARTNER_ID = "partner_id";
        public static final String ACCOUNT_ID = "account_id";
        public static final String CARD_ID = "card_id";
    }

    public class QueryParamsKeys {
        public static final String CONTINUATION_KEY = "continuation_key";
        public static final String PAGE = "page";
        public static final String PAGE_SIZE = "page_size";
    }

    public class Jwt {
        public static final String ISSUER = "Tink";
        public static final String JWT_CONTENT_TYPE = "JWT";
        public static final long TOKEN_LIFETIME_SECONDS = 300;
    }

    public static class HttpFilters {
        public static final int RETRY_SLEEP_MILLISECONDS = 5000;
        public static final int MAX_NUM_RETRIES = 3;
    }

    public static class Keystore {
        public static final String KEYSTORE_PATH =
                "data/agents/serviceprovider/nordea/partner/{clusterId}.jks";
        public static final String SIGNING_KEY_ALIAS = "tinksign";
    }

    public static class CardCategory {
        public static final String COMBINED = "combined";
        public static final String CREDIT = "credit";
    }
}
