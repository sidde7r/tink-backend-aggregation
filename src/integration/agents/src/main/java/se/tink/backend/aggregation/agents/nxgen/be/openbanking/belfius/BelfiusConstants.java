package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius;

import com.google.common.collect.ImmutableList;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;

public final class BelfiusConstants {

    public static final String INTEGRATION_NAME = "belfius";
    public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder()
                    .put(AccountTypes.CHECKING, "some_string1_the_integratee_uses")
                    .put(AccountTypes.SAVINGS, "some_string2_the_integratee_uses")
                    .put(AccountTypes.CREDIT_CARD, "some_string3_the_integratee_uses")
                    .ignoreKeys("some_string4_the_integratee_uses")
                    .build();

    private BelfiusConstants() {
        throw new AssertionError();
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String MISSING_TOKEN = "Cannot find token.";
        public static final ImmutableList<Integer> TRANSACTION_ERROR_CODES =
                ImmutableList.of(403, 404, 429);
    }

    public static class Urls {
        public static final String FETCH_ACCOUNT_PATH = "/accounts/";
        public static final String FETCH_TRANSACTIONS_PATH = "/accounts/{logical_id}/transactions";
        public static final String CONSENT_PATH = "/consent-uris";
        public static final String TOKEN_PATH = "/token";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN;
        public static final String CODE = "code";
        public static final String ID_TOKEN = "id_token";
        public static final String LOGICAL_ID = "logical_id";
    }

    public static class HeaderKeys {
        public static final String REQUEST_ID = "Request-ID";
        public static final String ACCEPT = "Accept";
        public static final String CLIENT_ID = "Client-ID";
        public static final String AUTHORIZATION = "Authorization";
        public static final String REDIRECT_URI = "redirect-uri";
        public static final String CODE_CHALLENGE = "Code-Challenge";
        public static final String CODE_CHALLENGE_METHOD = "Code-Challenge-Method";
        public static final String X_TINK_DEBUG = "X-Tink-Debug";
    }

    public static class FormKeys {
        public static final String GRANT_TYPE = "grant_type";
        public static final String CODE = "code";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String CODE_VERIFIER = "code_verifier";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public static class FormValues {
        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public static class HeaderValues {
        public static final String ACCEPT = "application/vnd.belfius.api+json; version=1";
        public static final String AUTHORIZATION = "Bearer ";
        public static final String ACCEPT_LANGUAGE = "fr";
        public static final String CODE_CHALLENGE_TYPE = "S256";
        public static final String FORCE = "force";
    }

    public static class CredentialKeys {
        public static final String IBAN = "iban";
    }

    public static class QueryKeys {
        public static final String IBAN = "iban";
        public static final String STATE = "state";
        public static final String FROM_DATE = "date_from";
        public static final String TO_DATE = "date_to";
    }
}
