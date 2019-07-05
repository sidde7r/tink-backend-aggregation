package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sdc;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.http.URL;

public final class SdcConstants {

    public static final String INTEGRATION_NAME = "sdc";

    private SdcConstants() {
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

    public static class Urls {

        public static final String BASE_AUTH_URL = "https://azure-auth-t2.test.sdc.dk";
        public static final String BASE_API_URL = "https://api-proxy.test.sdc.dk/api/psd2";

        public static final URL AUTHORIZATION = new URL(BASE_AUTH_URL + Endpoints.AUTHORIZATION);
        public static final URL TOKEN = new URL(BASE_AUTH_URL + Endpoints.TOKEN);
        public static final URL ACCOUNTS = new URL(BASE_API_URL + Endpoints.ACCOUNTS);
        public static final URL BALANCES = new URL(BASE_API_URL + Endpoints.BALANCES);
        public static final URL TRANSACTIONS = new URL(BASE_API_URL + Endpoints.TRANSACTIONS);
    }

    private static class Endpoints {
        public static final String AUTHORIZATION = "/Account/Login";
        public static final String TOKEN = "/Token";
        public static final String ACCOUNTS = "/v1/accounts";
        public static final String BALANCES = "/v1/accounts/{account-id}/balances";
        public static final String TRANSACTIONS = "/v1/accounts/{account-id}/transactions";
    }

    public static class PathParameters {
        public static final String ACCOUNT_ID = "account-id";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN;
    }

    public static class QueryKeys {

        public static final String SCOPE = "scope";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String CLIENT_ID = "client_id";
        public static final String STATE = "state";
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
    }

    public static class QueryValues {

        public static final String CODE = "code";
        public static final String BOOKED = "booked";
    }

    public static class HeaderKeys {

        public static final String X_REQUEST_ID = "x_request_id";
        public static final String OCP_APIM_SUBSCRIPTION_KEY = "Ocp-Apim-Subscription-Key";
    }

    public static class FormKeys {

        public static final String GRANT_TYPE = "grant_type";
        public static final String CODE = "code";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String CLIENT_ID = "client_id";
        public static final String CLIENT_SECRET = "client_secret";
        public static final String SCOPE = "scope";
        public static final String SSN = "ssn";
        public static final String IPIDFR = "ipidfr";
    }

    public static class FormValues {

        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String SCOPE_AIS = "psd2.aisp";
    }

    public static class HeaderValues {

        public static final String SCOPE_AIS = "psd2.aisp";
    }

    public static class Account {

        public static final String AVAILABLE = "available";
    }

    public static class Transactions {

        public static final int MAX_TRANSACTIONS_PER_RESPONSE = 400;
    }
}
