package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants.PersistentStorageKeys;

public final class LaBanquePostaleConstants {

    public static class Urls {

        public static final String OAUTH = "/authorize";
        public static final String GET_TOKEN = "/token";
        public static final String FETCH_ACCOUNTS = "/accounts";
        public static final String FETCH_BALANCES = "/accounts/%s/balances";
        public static final String FETCH_TRANSACTIONS = "/accounts/%s/transactions";
        public static final String PAYMENT_INITIATION = "/payment-requests";
        public static final String GET_PAYMENT = "/payment-requests/%s";
        public static final String CONFIRM_PAYMENT = "/payment-requests/%s/confirmation";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = PersistentStorageKeys.OAUTH_2_TOKEN;
    }

    public static class HeaderKeys {
        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String SIGNATURE = "Signature";
        public static final String CONTENT_TYPE = "Content-Type";
    }

    public static class HeaderValues {

        public static final String CONTENT_TYPE = "application/json";
    }

    public static class QueryKeys {
        public static final String CLIENT_ID = "client_id";
        public static final String SCOPE = "scope";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String STATE = "state";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String CODE_CHALLENGE = "code_challenge";
        public static final String CODE_CHALLENGE_METHOD = "code_challenge_method";
        public static final String WITH_BALANCE = "withBalance";
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String CLIENT_SECRET = "client_secret";
        public static final String GRANT_TYPE = "grant_type";
        public static final String CODE = "code";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public static class Payload {
        public static final String EMPTY = "";
    }

    public static class PaymentTypeInformation {
        public static final String CATEGORY_PURPOSE = "DVPM";
        public static final String LOCAL_INSTRUMENT = "INST";
        public static final String SERVICE_LEVEL = "SEPA";
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String PAYMENT_NOT_FOUND = "Payment can not be found";
    }

    public static class QueryValues {
        public static final String SCORE = "aisp";
    }
}
