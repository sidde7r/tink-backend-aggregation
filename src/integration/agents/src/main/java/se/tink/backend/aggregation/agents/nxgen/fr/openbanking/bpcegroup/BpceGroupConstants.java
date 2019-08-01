package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.http.URL;

public final class BpceGroupConstants {

    public static final String INTEGRATION_NAME = "bpcegroup";

    private BpceGroupConstants() {
        throw new AssertionError();
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String MISSING_TOKEN = "Cannot find token.";
        public static final String TOKENT_NOT_FOUND = "Token not found.";
        public static final String BALANCE_NOT_FOUND = "No balance found";
    }

    public static class Urls {

        public static final String BASE_AUTH_URL =
                "https://www.as-sandbox.api.89c3.com/api/oauth/token";
        public static final String BASE_API_URL =
                "https://www.rs-sandbox.api.89c3.com/stet/psd2/v1";

        public static final URL ACCOUNTS = new URL(BASE_API_URL + Endpoints.ACCOUNTS);
        public static final URL TRANSACTIONS = new URL(BASE_API_URL + Endpoints.TRANSACTIONS);
    }

    public static class Endpoints {

        public static final String ACCOUNTS = "/accounts";
        public static final String BALANCES = "/accounts/{account-id}/balances";
        public static final String TRANSACTIONS = "/accounts/{account-id}/transactions";
    }

    public static class UrlParameters {

        public static final String ACCOUNT_ID = "account-id";
    }

    public static class StorageKeys {

        public static final String OAUTH_TOKEN = OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN;
        public static final String ACCESS_TOKEN = "ACCESS_TOKEN";
    }

    public static class QueryKeys {

        public static final String SBX_ROID = "sbx_roid";
        public static final String SBX_CDETAB = "sbx_cdetab";
        public static final String SBX_TPPID = "sbx_tppid";
    }

    public static class QueryValues {

        public static final String SBX_ROID = "D6061564I0";
        public static final String SBX_CDETAB = "16807";
        public static final String SBX_TPPID = "1234";
    }

    public static class HeaderKeys {

        public static final String AUTHORIZATION = "Authorization";
        public static final String SIGNATURE = "Signature";
        public static final String X_REQUEST_ID = "X-Request-ID";
    }

    public static class HeaderValues {

        public static final String EMPTY = "empty";
        public static final String TOKEN_PREFIX = "Bearer ";
    }

    public static class FormKeys {

        public static final String CLIENT_ID = "client_id";
        public static final String CLIENT_SECRET = "client_secret";
        public static final String GRANT_TYPE = "grant_type";
        public static final String CDETAB = "cdetab";
        public static final String SCOPE = "scope";
    }

    public static class FormValues {

        public static final String SCOPE = "aisp";
        public static final String GRANT_TYPE = "client_credentials";
        public static final String CDETAB = "16807";
    }

    public static class Balance {

        public static final String ACCOUNT_BALANCE = "CLBD";
    }

    public static class Account {

        public static final String TRANSACTIONAL_ACCOUNT_TYPE = "CACC";
    }

    public static class Transaction {

        public static final String PENDING = "PDNG";
    }
}
