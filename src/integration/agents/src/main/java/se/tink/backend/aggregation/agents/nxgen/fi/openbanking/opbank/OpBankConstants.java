package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;

public final class OpBankConstants {

    public static final String INTEGRATION_NAME = "fi-opbank-openbanking";

    private OpBankConstants() {
        throw new AssertionError();
    }

    public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder()
                    .put(AccountTypes.CHECKING, "some_string1_the_integratee_uses")
                    .put(AccountTypes.SAVINGS, "some_string2_the_integratee_uses")
                    .put(AccountTypes.CREDIT_CARD, "some_string3_the_integratee_uses")
                    .ignoreKeys("some_string4_the_integratee_uses")
                    .build();

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String MISSING_TOKEN = "Cannot find token.";
    }

    private static String BASE_URL = "http://mtls-apis.psd2-sandbox.op.fi";

    public static class Urls {
        public static final String OAUTH_TOKEN = BASE_URL + "/oauth/token";
        public static final String ACCOUNTS_AUTHORIZATION = BASE_URL + "/accounts-psd2/v1/authorizations";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN;
    }

    public static class QueryKeys {}

    public static class QueryValues {}

    public static class HeaderKeys {
        public static final String X_API_KEY = "x-api-key";
        public static final String AUTHORIZATION = "Authorization";
        public static final String X_FAPI_FINANCIAL_ID = "x-fapi-financial-id";
    }

    public static class FormKeys {}

    public static class FormValues {}

    public static class LogTags {}
}
