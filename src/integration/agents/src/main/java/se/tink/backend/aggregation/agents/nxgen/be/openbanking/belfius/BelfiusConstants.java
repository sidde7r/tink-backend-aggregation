package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius;

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
    }

    public static class Urls {
        public static final String FETCH_ACCOUNT_PATH = "/sandbox/psd2/accounts/";
        public static final String FETCH_TRANSACTIONS_PATH =
                "/sandbox/psd2/accounts/%s/transactions";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN;
    }

    public static class HeaderKeys {
        public static final String REQUEST_ID = "Request-ID";
        public static final String ACCEPT = "Accept";
        public static final String CLIENT_ID = "Client-ID";
        public static final String REDIRECT_URI = "Redirect-URI";
        public static final String AUTHORIZATION = "Authorization";
    }

    public static class HeaderValues {
        public static final String ACCEPT = "application/vnd.belfius.api+json; version=1";
        public static final String AUTHORIZATION = "Bearer 1";
        public static final String ACCEPT_LANGUAGE = "en";
    }
}
