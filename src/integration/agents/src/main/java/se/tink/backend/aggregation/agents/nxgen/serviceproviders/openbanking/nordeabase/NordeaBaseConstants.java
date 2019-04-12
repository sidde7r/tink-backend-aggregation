package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.http.URL;

public final class NordeaBaseConstants {
    public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder()
                    .put(AccountTypes.CHECKING, "Current")
                    .put(AccountTypes.SAVINGS, "Savings")
                    .build();

    private NordeaBaseConstants() {
        throw new AssertionError();
    }

    public static class Market {
        public static String INTEGRATION_NAME = "nordea";
    }

    public static class Urls {
        public static final String BASE_URL = "https://api.nordeaopenbanking.com";
        public static final URL AUTHORIZE = new URL(BASE_URL + ApiService.AUTHORIZE);
        public static final URL GET_TOKEN = new URL(BASE_URL + ApiService.GET_TOKEN);
        public static final URL GET_ACCOUNTS = new URL(BASE_URL + ApiService.GET_ACCOUNTS);
        public static final URL GET_TRANSACTIONS = new URL(BASE_URL + ApiService.GET_TRANSACTIONS);
        public static final URL INITIATE_DOMESTIC_PAYMENT =
                new URL(BASE_URL + ApiService.INITIATE_DOMESTIC_PAYMENT);
        public static final URL CONFIRM_DOMESTIC_PAYMENT = new URL(BASE_URL + ApiService.CONFIRM_DOMESTIC_PAYMENT);
        public static final URL CONFIRM_SEPA_PAYMENT = new URL(BASE_URL + ApiService.CONFIRM_SEPA_PAYMENT);
    }

    public static class ApiService {
        public static final String AUTHORIZE = "/v3/authorize";
        public static final String GET_TOKEN = "/v3/authorize/token";
        public static final String GET_ACCOUNTS = "/v3/accounts";
        public static final String GET_TRANSACTIONS = "/v3/accounts/{accountId}/transactions";
        public static final String INITIATE_DOMESTIC_PAYMENT = "/v3/payments/domestic";
        public static final String CONFIRM_DOMESTIC_PAYMENT = "/v3/payments/domestic/{paymentId}/confirm";
        public static final String CONFIRM_SEPA_PAYMENT = "/v3/payments/sepa/{paymentId}/confirm";
    }

    public static class StorageKeys {
        public static final String ACCOUNT_ID = "account_id";
        public static final String ACCESS_TOKEN = "accessToken";
    }

    public static class QueryKeys {
        public static final String CLIENT_ID = "client_id";
        public static final String X_CLIENT_ID = "X-IBM-Client-Id";
        public static final String X_CLIENT_SECRET = "X-IBM-Client-Secret";
        public static final String STATE = "state";
        public static final String DURATION = "duration";
        public static final String COUNTRY = "country";
        public static final String SCOPE = "scope";
        public static final String REDIRECT_URI = "redirect_uri";
    }

    public static class QueryValues {
        public static final String DURATION = "12000";
        public static final String SCOPE =
                "ACCOUNTS_BALANCES,ACCOUNTS_BASIC,"
                        + "ACCOUNTS_DETAILS,ACCOUNTS_TRANSACTIONS,PAYMENTS_MULTIPLE";
    }

    public static class HeaderKeys {
        public static final String AUTHORIZATION = "Authorization";
    }

    public static class FormKeys {
        public static final String GRANT_TYPE = "grant_type";
        public static final String CODE = "code";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public static class FormValues {
        public static final String AUTHORIZATION_CODE = "authorization_code";
    }

    public static class IdTags {
        public static final String ACCOUNT_ID = "accountId";
        public static final String PAYMENT_ID = "paymentId";
    }

    public static final class ErrorMessages {
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
    }
}
