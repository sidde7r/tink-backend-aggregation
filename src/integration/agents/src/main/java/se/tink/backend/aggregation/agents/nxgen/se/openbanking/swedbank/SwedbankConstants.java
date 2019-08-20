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
        public static final URL AUTHORIZE = new URL(BASE.concat(Endpoints.AUTHORIZE));
        public static final URL TOKEN = new URL(BASE.concat(Endpoints.TOKEN));
        public static final URL ACCOUNTS = new URL(BASE.concat(Endpoints.ACCOUNTS));
        public static final URL ACCOUNT_BALANCES = new URL(BASE.concat(Endpoints.ACCOUNT_BALANCES));
        public static final URL ACCOUNT_TRANSACTIONS =
                new URL(BASE.concat(Endpoints.ACCOUNT_TRANSACTIONS));
        public static final URL CONSENTS = new URL(BASE.concat(Endpoints.CONSENTS));
        public static final URL CONSENT_STATUS = new URL(BASE.concat(Endpoints.CONSENT_STATUS));
        public static final URL INITIATE_PAYMENT = new URL(BASE + Endpoints.INITIATE_PAYMENT);
        public static final URL GET_PAYMENT = new URL(BASE + Endpoints.GET_PAYMENT);
        public static final URL GET_PAYMENT_STATUS = new URL(BASE + Endpoints.GET_PAYMENT_STATUS);
        public static final URL INITIATE_PAYMENT_AUTH =
                new URL(BASE + Endpoints.INITIATE_PAYMENT_AUTH);
    }

    public static class Endpoints {
        public static final String AUTHORIZE = "/psd2/authorize";
        public static final String TOKEN = "/psd2/token";
        public static final String ACCOUNTS = "/v2/accounts";
        public static final String ACCOUNT_BALANCES = "/v2/accounts/{account-id}/balances";
        public static final String ACCOUNT_TRANSACTIONS = "/v2/accounts/{account-id}/transactions";
        public static final String CONSENTS = "/v2/consents";
        public static final String CONSENT_STATUS = CONSENTS + "/{consent-id}/status";
        public static final String INITIATE_PAYMENT = "/sandbox/v2/payments/{paymentType}";
        public static final String GET_PAYMENT = "/sandbox/v2/payments/{paymentId}";
        public static final String GET_PAYMENT_STATUS = "/sandbox/v2/payments/{paymentId}/status";
        public static final String INITIATE_PAYMENT_AUTH =
                "/sandbox/v2/payments/{paymentId}/authorisations";
    }

    public static class UrlParameters {
        public static final String ACCOUNT_ID = "account-id";
        public static final String CONSENT_ID = "consent-id";
        public static final String PAYMENT_TYPE = "paymentType";
        public static final String PAYMENT_ID = "paymentId";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN;
        public static final String ACCOUNT_ID = "ACCOUNT_ID";
        public static final String CONSENT = "CONSENT";
        public static final String CONSENT_STATUS = "CONSENT_STATUS";
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
        public static final String BOOKING_STATUS = "bookingStatus";
    }

    public static class QueryValues {
        public static final String SCOPE = "PSD2";
        public static final String RESPONSE_TYPE_TOKEN = "code";
        public static final String GRANT_TYPE = "authorization_code";
        public static final String BOOKING_STATUS = "both";
    }

    public static class HeaderKeys {
        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String CONSENT_ID = "Consent-ID";
        public static final String DATE = "Date";
        public static final String FROM_DATE = "dateFrom";
        public static final String TO_DATE = "dateTo";
        public static final String TPP_TRANSACTION_ID = "TPP-Transaction-ID";
        public static final String TPP_REQUEST_ID = "TPP-Request-ID";
        public static final String TPP_REDIRECT_URI = "tpp-redirect-uri";
        public static final String TPP_NOK_REDIRECT_URI = "tpp-nok-redirect-uri";
        public static final String PSU_IP_ADDRESS = "PSU-IP-Address";
        public static final String PSU_USER_AGENT = "PSU-User-Agent";
    }

    public static class HeaderValues {
        public static final String PSU_IP_ADDRESS = "127.0.0.1";
        public static final String PSU_USER_AGENT = "Desktop Mode";
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
        public static final String SCA_REQUIRED = "sca_required";
        public static final String TRANSACTION_SCA_ALREADY_SIGNED =
                "transactions resource has already been signed";
        public static final String INVALID_ACCOUNT_TYPE = "Invalid account type: %s";
        public static final String INVALID_PAYMENT_TYPE =
                "No SwedbankPaymentType found for AccountIdentifiers pair %s";
    }

    public static class ConsentStatus {
        public static final String VALID = "valid";
        public static final String SIGNED = "signed";
    }

    public static class HeaderResponse {
        public static final String LOCATION = "Location";
    }

    public static final class BodyParameter {
        public static final String ALL_ACCOUNTS = "allAccounts";
        public static final int FREQUENCY_PER_DAY = 4;
        public static final boolean RECURRING_INDICATOR = true;
        public static final boolean COMBINED_SERVICE_INDICATOR = false;
    }

    public static final class TimeValues {
        public static final int SLEEP_TIME_MILLISECONDS = 4000;
        public static final int MONTHS_TO_FETCH_MAX = 25;
        public static final int MONTHS_TO_FETCH_MIN = 1;
        public static final int ATTEMPS_BEFORE_TIMEOUT = 10;
        public static final int CONSENT_DURATION_IN_DAYS = 90;
        public static final int MONTHS_TO_FETCH = 25;
    }

    public static final class LogMessages {
        public static final String SIGNING_COMPLETE = "Signing complete";
        public static final String WAITING_FOR_SIGNING = "Waiting for signing";
        public static final String TRANSACTION_SIGNING_TIMED_OUT =
                "Sign for fetching transactions for the last 25 months, timed out";
    }
}
