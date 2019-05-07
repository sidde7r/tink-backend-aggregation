package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.http.URL;

public final class SkandiaConstants {

    public static final String INTEGRATION_NAME = "skandia";
    public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder().put(AccountTypes.CHECKING, "CACC").build();

    private SkandiaConstants() {
        throw new AssertionError();
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String MISSING_TOKEN = "Cannot find token.";
        public static final String INVALID_ACCOUNT_TYPE = "Account type is not valid!";
    }

    public static class Urls {
        public static final String BASE_URL = "https://api.test.skandia.se/open-banking/sandbox";

        public static final URL GET_ACCOUNTS = new URL(BASE_URL + ApiServices.GET_ACCOUNTS);
        public static final URL GET_BALANCES = new URL(BASE_URL + ApiServices.GET_BALANCES);
        public static final URL GET_TRANSACTIONS = new URL(BASE_URL + ApiServices.GET_TRANSACTIONS);
    }

    public static class ApiServices {
        public static final String GET_ACCOUNTS = "/v1/accounts";
        public static final String GET_BALANCES = "/v1/accounts/{accountId}/balances";
        public static final String GET_TRANSACTIONS = "/v1/accounts/{accountId}/transactions";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN;
    }

    public static class QueryKeys {
        public static final String BOOKING_STATUS = "bookingStatus";
    }

    public static class QueryValues {
        public static final String PENDING = "pending";
        public static final String BOOKED = "booked";
    }

    public static class HeaderKeys {
        public static final String CONSENT_ID = "Consent-ID";
        public static final String X_REQUEST_ID = "X-Request-ID";
    }

    public static class IdTags {
        public static final String ACCOUNT_ID = "accountId";
    }
}
