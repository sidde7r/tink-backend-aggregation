package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.http.URL;

public final class FinecoBankConstants {

    public static final String INTEGRATION_NAME = "finecobank";

    private FinecoBankConstants() {
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

        public static final String BASE_URL = "https://api.finecobank.com/v1";
        public static final URL CONSENTS = new URL(BASE_URL + Endpoints.CONSENTS);
        public static final URL ACCOUNTS = new URL(BASE_URL + Endpoints.ACCOUNTS);
        public static final URL TRANSACTIONS = new URL(BASE_URL + Endpoints.TRANSACTIONS);
    }

    public static class Endpoints {
        public static final String CONSENTS = "/consents";
        public static final String ACCOUNTS = "/accounts";
        public static final String TRANSACTIONS = "/accounts/{account-id}/transactions";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN;
    }

    public static class QueryKeys {

        public static final String WITH_BALANCE = "withBalance";
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
    }

    public static class QueryValues {

        public static final String BOOKED = "booked";
    }

    public static class HeaderKeys {

        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String TPP_REDIRECT_URI = "TPP-Redirect-URI";
        public static final String CONSENT_ID = "Consent-ID";
    }

    public static class BalanceTypes {

        public static final String FORWARD_AVAILABLE = "forwardAvailable";
        public static final String INTERIM_AVAILABLE = "interimAvailable";
    }

    public static class Formats {

        public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
        public static final String CURRENCY = "EUR";
    }

    public static class ParameterKeys {

        public static final String ACCOUNT_ID = "account-id";
    }

    public static class HeaderValues {
        public static final String X_REQUEST_ID_ACCOUNTS = "123e4567-e89b-42d3-a456-556642440048";
        public static final String X_REQUEST_ID_TRANSACTIONS =
                "123e4567-e89b-42d3-a456-556642440071";
        public static final String CONSENT_ID = "76b908cc-4605-4d06-8869-4387f5daa318";
    }
}
