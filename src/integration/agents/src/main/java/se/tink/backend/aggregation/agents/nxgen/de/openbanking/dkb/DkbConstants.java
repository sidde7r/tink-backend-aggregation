package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.http.URL;

public final class DkbConstants {

    public static final String INTEGRATION_NAME = "dkb";

    private DkbConstants() {
        throw new AssertionError();
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
    }

    public static class Urls {
        public static final String BASE_URL = "https://api.dkb.de";
        public static final String BASE_PSD2_URL = BASE_URL + "/psd2/1.3.2";

        public static final URL TOKEN = new URL(BASE_URL + ApiService.TOKEN);
        public static final URL CONSENT = new URL(BASE_PSD2_URL + ApiService.CONSENT);
        public static final URL GET_ACCOUNTS = new URL(BASE_PSD2_URL + ApiService.GET_ACCOUNTS);
        public static final URL GET_TRANSACTIONS =
                new URL(BASE_PSD2_URL + ApiService.GET_TRANSACTIONS);
    }

    public static class ApiService {
        public static final String TOKEN = "/token";
        public static final String CONSENT = "/v1/consents";
        public static final String GET_ACCOUNTS = "/v1/accounts";
        public static final String GET_TRANSACTIONS = "/v1/accounts/{accountId}/transactions";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN;
        public static final String CONSENT_ID = "consent_id";
    }

    public static class QueryKeys {
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
    }

    public static class QueryValues {
        public static final String BOTH = "both";
    }

    public static class HeaderKeys {
        public static final String AUTHORIZATION = "Authorization";
        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String CONSENT_ID = "Consent-ID";
    }

    public static class FormKeys {
        public static final String GRANT_TYPE = "grant_type";
        public static final String USERNAME = "username";
        public static final String PASSWORD = "password";
    }

    public static class FormValues {
        public static final String PASSWORD = "password";
        public static final String ALL_ACCOUNTS = "allAccounts";
        public static final String EUR = "EUR";
        public static final String MSISDN = "+49 170 1234567";
        public static final String BBAN = "BARC12345612345678";
        public static final String PAN = "5409050000000000";
        public static final String MASKED_PAN = "123456xxxxxx1234";
        public static final Boolean FALSE = false;
        public static final Integer FREQUENCY_PER_DAY = 4;
        public static final String VALID_UNTIL = "2020-12-31";
    }

    public static class CredentialKeys {
        public static final String IBAN = "iban";
    }

    public static class IdTags {
        public static final String ACCOUNT_ID = "accountId";
    }

    public static class BalanceTypes {
        public static final String INTERIM_AVAILABLE = "interimAvailable";
        public static final String FORWARD_AVAILABLE = "forwardAvailable";
    }
}
