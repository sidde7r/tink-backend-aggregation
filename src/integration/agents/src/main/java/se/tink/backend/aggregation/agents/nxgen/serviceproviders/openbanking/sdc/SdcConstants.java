package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;

public final class SdcConstants {

    private SdcConstants() {
        throw new AssertionError();
    }

    public static class PathParameters {
        public static final String ACCOUNT_ID = "account-id";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = PersistentStorageKeys.OAUTH_2_TOKEN;
        public static final String TIMESTAMP = "timestamp";
        public static final String BOOKING_STATUS_STORAGE = "booking_status";
    }

    public static class QueryKeys {
        public static final String SCOPE = "scope";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String CLIENT_ID = "client_id";
        public static final String STATE = "state";
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String LOGIN_TYPE = "login_type";
    }

    public static class QueryValues {
        public static final String CODE = "code";
        public static final String BOOKED = "booked";
        public static final String BOTH = "both";
        public static final String NEMID_BANK_LOGIN = "NemID Bank 2 factor";
    }

    public static class HeaderKeys {
        public static final String X_REQUEST_ID = "x_request_id";
        public static final String OCP_APIM_SUBSCRIPTION_KEY = "Ocp-Apim-Subscription-Key";
    }

    public static class FormValues {
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String SCOPE_AIS = "psd2.aisp";
    }

    public static class HeaderValues {
        public static final String SCOPE_AIS = "psd2.aisp";
    }

    public static class Account {
        public static final String CLOSING_BOOKED = "closingBooked";
    }

    public static class Transactions {
        public static final int MAX_CONSECUTIVE_EMPTY_PAGES = 4;
        public static final int MONTHS_TO_FETCH = 1;
    }

    public static class CredentialKeys {
        public static final String IBAN = "iban";
    }

    public static class ErrorMessages {
        public static final String TOKEN_EXPIRED =
                "Lifetime validation failed. The token is expired.";
    }
}
