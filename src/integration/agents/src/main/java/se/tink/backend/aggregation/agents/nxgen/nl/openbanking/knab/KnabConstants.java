package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public final class KnabConstants {

    private KnabConstants() {
        throw new AssertionError();
    }

    public static class ErrorMessages {
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String NO_SUPPLEMENTAL_INFORMATION =
                "No suplemental information returned";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = PersistentStorageKeys.OAUTH_2_TOKEN;
        public static final String CONSENT_ID = "consent_id";
    }

    public static class HeaderKeys {

        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String DATE = "Date";
        public static final String PSU_IP_ADDRESS = "PSU-IP-Address";
        public static final String TPP_REDIRECT_URI = "TPP-Redirect-URI";
        public static final String CONSENT_ID = "Consent-ID";
    }

    public static class Urls {

        public static final String BASE_AUTH_URL = "https://login.knab.nl";
        public static final String BASE_API_URL = "https://tpp-loket.knab.nl";

        public static final URL AUTHORIZE = new URL(BASE_AUTH_URL + Endpoints.AUTHORIZE);
        public static final URL TOKEN = new URL(BASE_AUTH_URL + Endpoints.TOKEN);
        public static final URL CONSENT = new URL(BASE_API_URL + Endpoints.CONSENT);
        public static final URL CONSENT_STATUS = new URL(CONSENT + "/{consent-id}/status");
        public static final URL ACCOUNTS = new URL(BASE_API_URL + Endpoints.ACCOUNTS);
        public static final URL BALANCES = new URL(BASE_API_URL + Endpoints.BALANCES);
        public static final URL TRANSACTIONS = new URL(BASE_API_URL + Endpoints.TRANSACTIONS);
    }

    public static class Endpoints {
        public static final String AUTHORIZE = "/connect/authorize";
        public static final String TOKEN = "/connect/token";
        public static final String CONSENT = "/openbanking/v1/consents";
        public static final String ACCOUNTS = "/openbanking/v1/accounts";
        public static final String BALANCES = "/openbanking/v1/accounts/{accountId}/balances";
        public static final String TRANSACTIONS =
                "/openbanking/v1/accounts/{accountId}/transactions";
    }

    public static final class UrlParameters {
        public static final String CONSENT_ID = "consent-id";
    }

    public static class PathVariables {
        public static final String ACCOUNT_ID = "accountId";
    }

    public static class QueryKeys {
        public static final String RESPONSE_TYPE = "response_type";
        public static final String STATE = "state";
        public static final String CLIENT_ID = "client_id";
        public static final String SCOPE = "scope";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String CODE = "code";
        public static final String WITH_BALANCE = "withBalance";
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
    }

    public static class QueryValues {
        public static final String CODE = "code";
        public static final String INITIAL_SCOPE = "openid profile psd2 offline_access";
        public static final String CONSENTED_SCOPE = INITIAL_SCOPE.concat(" AIS:%s");
        public static final String BOOKED = "booked";
    }

    public static class BodyValues {
        public static final String VALID = "valid";
    }

    public static class FormKeys {
        public static final String GRANT_TYPE = "grant_type";
        public static final String CODE = "code";
        public static final String CLIENT_ID = "client_id";
        public static final String CLIENT_SECRET = "client_secret";
        public static final String STATE = "state";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public static class FormValues {
        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public static class CredentialKeys {
        public static final String IBANS = "ibans";
    }

    public static class BalanceTypes {
        public static final String INTERIM_BOOKED = "interimBooked";
    }

    public static class Formats {
        public static final String CONSENT_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss z";
        public static final String DATE_FORMAT = "yyyy-MM-dd";
    }
}
