package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.http.URL;

public final class SwedbankConstants {

    private SwedbankConstants() {
        throw new AssertionError();
    }

    public static final String INTEGRATION_NAME = "swedbank";

    public static class Format {
        public static final String TRANSACTION_BOOKING_DATE_FORMAT = "yyyy-MM-dd";
        public static final String HEADER_TIMESTAMP = "E, dd MMM yyyy HH:mm:ss z";
        public static final String CONSENT_VALIDITY_TIMESTAMP = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    }

    public static class Urls {
        public static final String BASE = "https://psd2.api.swedbank.com";
        public static final URL AUTHORIZE = new URL(BASE + Endpoints.AUTHORIZE);
        public static final URL TOKEN = new URL(BASE + Endpoints.TOKEN);
        public static final URL ACCOUNTS = new URL(BASE + Endpoints.ACCOUNTS);
        public static final URL ACCOUNT_BALANCES = new URL(BASE + Endpoints.ACCOUNT_BALANCES);
        public static final URL ACCOUNT_TRANSACTIONS =
                new URL(BASE + Endpoints.ACCOUNT_TRANSACTIONS);
        public static final URL CONSENTS = new URL(BASE + Endpoints.CONSENTS);
    }

    public static class Endpoints {
        public static final String AUTHORIZE = "/psd2/authorize";
        public static final String TOKEN = "/psd2/token";
        public static final String ACCOUNTS = "/sandbox/v1/accounts";
        public static final String ACCOUNT_BALANCES = "/sandbox/v1/accounts/{account-id}/balances";
        public static final String ACCOUNT_TRANSACTIONS =
                "/sandbox/v1/accounts/{account-id}/transactions";
        public static final String CONSENTS = "/sandbox/v1/consents";
    }

    public static class UrlParameters {
        public static final String ACCOUNT_ID = "account-id";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN;
        public static final String ACCOUNT_ID = "ACCOUNT_ID";
        public static final String CONSENT = "CONSENT";
    }

    public static class QueryKeys {
        public static final String CLIENT_ID = "client_id";
        public static final String SCOPE = "scope";
        public static final String BIC = "bic";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String STATE = "state";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String CLIENT_SECRET = "client_secret";
        public static final String CODE = "code";
        public static final String GRANT_TYPE = "grant_type";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public static class QueryValues {
        public static final String SCOPE = "PSD2sandbox";
        public static final String RESPONSE_TYPE_TOKEN = "code";
        public static final String GRANT_TYPE = "authorization_code";
    }

    public static class HeaderKeys {
        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String CONSENT_ID = "Consent-ID";
        public static final String DATE = "Date";
        public static final String FROM_DATE = "dateFrom";
        public static final String TO_DATE = "dateTo";
        public static final String TPP_TRANSACTION_ID = "TPP-Transaction-ID";
        public static final String TPP_REQUEST_ID = "TPP-Request-ID";
    }

    public static class BICSandbox {
        public static final String SWEDEN = "SANDSESS";
        public static final String LITHUANIA = "SANDLT22";
        public static final String LATVIA = "SANDLV22";
        public static final String ESTONIA = "SANDEE2X";
        public static final String COOPERATING_SAVINGSBANKS_SWEDEN = "SANDSESS";
    }

    public static class BICProduction {
        public static final String SWEDEN = "SWEDSESS";
        public static final String LITHUANIA = "HABALT22";
        public static final String LATVIA = "HABALV22";
        public static final String ESTONIA = "HABAEE2X";
        public static final String COOPERATING_SAVINGSBANKS_SWEDEN = "SWEDSESS";
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String MISSING_TOKEN = "Cannot find token.";
    }
}
