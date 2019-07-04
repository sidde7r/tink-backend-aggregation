package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.http.URL;

public final class SBABConstants {

    public static final String INTEGRATION_NAME = "sbab";
    public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder()
                    .put(AccountTypes.SAVINGS, "savings", "minor_savings_account")
                    .build();

    private SBABConstants() {
        throw new AssertionError();
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String MISSING_TOKEN = "Cannot find token.";
        public static final String UNKNOWN_ACCOUNT_TYPE = "Unknown account type.";
    }

    public static class Urls {
        public static final URL ACCOUNTS = new URL(Endpoints.BASE_URL + Endpoints.ACCOUNTS);
        public static final URL AUTHORIZATION = new URL(Endpoints.BASE_URL + Endpoints.OAUTH);
        public static final URL TRANSACTIONS = new URL(Endpoints.BASE_URL + Endpoints.TRANSFERS);
        public static final URL CUSTOMERS = new URL(Endpoints.BASE_URL + Endpoints.CUSTOMERS);
        public static final URL TOKEN = new URL(Endpoints.BASE_URL + Endpoints.TOKEN);
        public static final URL INITIATE_PAYMENT =
                new URL(Endpoints.BASE_URL + Endpoints.INITIATE_PAYMENT);
        public static final URL GET_PAYMENT = new URL(Endpoints.BASE_URL + Endpoints.GET_PAYMENT);
    }

    public static class Endpoints {
        public static final String BASE_URL = "https://developer.sbab.se";
        public static final String OAUTH = "/sandbox/psd2/auth/1.0/authenticate";
        public static final String TRANSFERS =
                "/sandbox/psd2/savings/2.0/accounts/{accountNumber}/transfers";
        public static final String CUSTOMERS = "/sandbox/psd2/customer/1.0/customers";
        public static final String ACCOUNTS = "/sandbox/psd2/savings/2.0/accounts";
        public static final String TOKEN = "/sandbox/psd2/auth/1.0/token";
        public static final String INITIATE_PAYMENT =
                "/sandbox/psd2/savings/2.0/accounts/{accountNumber}/transfers";
        public static final String GET_PAYMENT =
                "/sandbox/psd2/savings/2.0/accounts/{accountNumber}/transfers/status/{referenceId}";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN;
        public static final String ACCOUNT_NUMBER = "accountNumber";
        public static final String TOKEN = "TOKEN";
    }

    public static class QueryKeys {
        public static final String CLIENT_ID = "client_id";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String SCOPE = "scope";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String GRANT_TYPE = "grant_type";
        public static final String CODE = "pending_code";
        public static final String START_DATE = "startDate";
        public static final String END_DATE = "endDate";
        public static final String USER_ID = "user_id";
        public static final String REFRESH_TOKEN = "refreshToken";
    }

    public static class QueryValues {
        public static final String RESPONSE_TYPE = "pending_code";
        public static final String SCOPE = "AIS,PIS";
        public static final String GRANT_TYPE = "pending_authorization_code";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String TEST_USER = "testUser";
    }

    public static class HeaderKeys {
        public static final String AUTHORIZATION = "Authorization";
        public static final String CLIENT_CERTIFICATE = "X-PSD2-CLIENT-TEST-CERT";
        public static final String PSU_IP_ADDRESS = "PSU-IP-Address";
    }

    public static class Format {
        public static final String TIMEZONE = "UTC";
        public static final String TIMESTAMP = "yyyy-MM-dd";
    }

    public static class IdTags {
        public static final String ACCOUNT_NUMBER = "accountNumber";
        public static final String PAYMENT_ID = "referenceId";
    }

    public static class CredentialKeys {
        public static final String USERNAME = "USERNAME";
        public static final String PASSWORD = "PASSWORD";
    }

    public class FormKeys {
        public static final String GRANT_TYPE = "grant_type";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String CODE = "pending_code";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public static class HeaderValues {
        public static final String PSU_IP_ADDRESS = "192.160.0.1";
    }
}
