package se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;

public abstract class BbvaConstants {

    public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder()
                    .put(AccountTypes.CHECKING, "CHECKING")
                    .put(AccountTypes.CREDIT_CARD, "CREDIT")
                    .build();

    public static class Urls {
        public static final String OAUTH = "/token/authorize";
        public static final String TOKEN = "/token";
        public static final String ACCOUNTS = "/accounts-sbx/v1/me/accounts";
        public static final String ACCOUNT = "/accounts-sbx/v1/me/accounts/%s";
        public static final String ACCOUNT_TRANSACTIONS =
                "/accounts-sbx/v1/me/accounts/%s/transactions";
    }

    public static class StorageKeys {
        public static final String BASE_AUTH_URL = "BASE_AUTH_URL";
        public static final String BASE_API_URL = "BASE_API_URL";
        public static final String CLIENT_ID = "CLIENT_ID";
        public static final String CLIENT_SECRET = "CLIENT_SECRET";
        public static final String REDIRECT_URI = "REDIRECT_URI";
        public static final String TOKEN = "TOKEN";
    }

    public static class QueryKeys {
        public static final String CLIENT_ID = "client_id";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String SCOPE = "scope";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String STATE = "state";
        public static final String GRANT_TYPE = "grant_type";
        public static final String CODE = "code";
        public static final String PAGINATION = "pageKey";
    }

    public static class QueryValues {
        public static final String GRANT_TYPE = "authorization_code";
        public static final String RESPONSE_TYPE = "code";
        public static final String SCOPE = "accounts_detail_full_sbx_1 account_transactions_sbx";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public static class HeaderKeys {
        public static final String AUTHORIZATION = "Authorization";
    }

    public static class HeaderValues {
        public static final String AUTHORIZATION_RESPONSE = "Basic %s";
        public static final String AUTHORIZATION = "jwt %s";
    }

    public static class FormKeys {}

    public static class FormValues {}

    public static class ErrorMessages {
        public static final String INVALID_CONFIG_CANNOT_BE_EMPTY_OR_NULL =
                "Invalid Config: %s cannot be empty or null";
    }

    public static class Exceptions {
        public static final String MISSING_CONFIGURATION = "BBVA configuration missing.";
        public static final String MISSING_TOKEN = "Cannot find token!";
    }

    public class Market {
        public static final String INTEGRATION_NAME = "bbva";
        public static final String CLIENT_NAME = "tink";
    }

    public class Pagination {
        public static final int START_PAGE = 1;
    }

    public class Formats {
        public static final String TRANSACTION_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    }

    public class BodyKeys {
        public static final String REFRESH_TOKEN = "refresh_token";
    }
}
