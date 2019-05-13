package se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.http.URL;

public final class RaiffeisenConstants {

    public static final String INTEGRATION_NAME = "raiffeisen-at";

    private RaiffeisenConstants() {
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

        public static final String BASE_AUTH_URL = "https://sso.raiffeisen.at/as";
        public static final String BASE_API_URL = "https://sandbox.raiffeisen.at/api";
        public static final URL AUTHENTICATE = new URL(BASE_AUTH_URL + Endpoints.AUTHENTICATE);
        public static final URL ACCOUNTS = new URL(BASE_API_URL + Endpoints.ACCOUNTS);
        public static final URL CONSENTS = new URL(BASE_API_URL + Endpoints.CONSENTS);
        public static final URL TRANSACTIONS = new URL(BASE_API_URL + Endpoints.TRANSACTIONS);
    }

    public static class Endpoints {
        public static final String AUTHENTICATE = "/token.oauth2";
        public static final String ACCOUNTS = "/psd2-xs2a/rest/v1/accounts";
        public static final String CONSENTS = "/psd2-xs2a/rest/v1/consents";
        public static final String TRANSACTIONS =
                "/psd2-xs2a/rest/v1/accounts/{account-id}/transactions";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN;
        public static final String CONSENT_ID = "CONSENT_ID";
    }

    public static class QueryKeys {

        public static final String WITH_BALANCE = "withBalance";
        public static final String BOOKING_STATUS = "bookingStatus";
    }

    public static class QueryValues {

        public static final String BOTH = "both";
    }

    public static class HeaderKeys {

        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String CONSENT_ID = "Consent-ID";
        public static final String AUTHORIZATION = "Authorization";
    }

    public static class FormKeys {

        public static final String GRANT_TYPE = "grant_type";
        public static final String SCOPE = "scope";
    }

    public static class FormValues {

        public static final String GRANT_TYPE = "client_credentials";
        public static final String SCOPE = "apic-sbx";
    }

    public static class CredentialKeys {

        public static final String IBAN = "IBAN";
    }

    public static class BalanceTypes {

        public static final String FORWARD_AVAILABLE = "forwardAvailable";
        public static final String INTERIM_AVAILABLE = "interimAvailable";
    }

    public static class ParameterKeys {

        public static final String ACCOUNT_ID = "account-id";
    }

    public static class Formats {

        public static final String TRANSACTION_DATE_FORMAT = "yyyy-MM-dd";
        public static final String PAGINATION_DATE_FORMAT = "yyyy-MM-dd";
        public static final String CONSENT_DATE_FORMAT = "yyyy-MM-dd";
    }

    public static class HeaderValues {

        public static final String TOKEN_PREFIX = "Bearer ";
        public static final Object X_REQUEST_ID =
                "99391c7e-ad88-49ec-a2ad-99ddcb1f7721"; // Constant for sandbox
    }
}
