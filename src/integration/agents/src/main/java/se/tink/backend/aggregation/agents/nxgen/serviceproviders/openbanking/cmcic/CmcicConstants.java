package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;

public final class CmcicConstants {

    private CmcicConstants() {
        throw new AssertionError();
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String NO_PIS_OAUTH_TOKEN_IN_STORAGE = "No PIS Oauth Token In Storage";
    }

    public static class Urls {
        public static final String BASE_API_PATH = "stet-psd2-api/v1.1";
        public static final String FETCH_ACCOUNTS_PATH = BASE_API_PATH + "/accounts";
        public static final String FETCH_END_USER_IDENTITY = BASE_API_PATH + "/end-user-identity";
        public static final String TOKEN_PATH = "oauth2/token";
        public static final String FETCH_TRANSACTIONS_PATH =
                FETCH_ACCOUNTS_PATH + "/%s/transactions";
        public static final String PAYMENT_REQUESTS = BASE_API_PATH + "/payment-requests";
        public static final String SUCCESS_REPORT_PATH = "?code=123&state=";
        public static final String BENEFICIARIES_PATH = "/trusted-beneficiaries";
        public static final String PIS_CONFIRMATION_PATH = "confirmation";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = PersistentStorageKeys.OAUTH_2_TOKEN;
        public static final String CODE_VERIFIER = "CODE_VERIFIER";
        public static final String PISP_TOKEN = "PISP_TOKEN";
        public static final String STATE = "STATE";
        public static final String AUTH_URL = "AUTH_URL";
        public static final String AUTH_FACTOR = "AUTH_FACTOR";
    }

    public static class QueryKeys {
        public static final String RESPONSE_TYPE = "response_type";
        public static final String CLIENT_ID = "client_id";
        public static final String SCOPE = "scope";
        public static final String STATE = "state";
        public static final String CODE_CHALLENGE = "code_challenge";
        public static final String CODE_CHALLENGE_METHOD = "code_challenge_method";
        public static final String PSU_AUTHENTICATION_FACTOR = "psuAF";
    }

    public static class QueryValues {
        public static final String RESPONSE_TYPE = "code";
        public static final String SCOPE = "aisp";
        public static final String CODE_CHALLENGE_METHOD = "S256";
    }

    public static class Signature {
        public static final String TIMEZONE = "GMT";
        public static final String DIGEST_PREFIX = "SHA-256=";
        public static final String DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss z";
    }

    public static class HeaderKeys {
        public static final String DIGEST = "Digest";
        public static final String HOST = "Host";
        public static final String DATE = "Date";
        public static final String X_REQUEST_ID = "X-Request-Id";
        public static final String SIGNATURE = "Signature";
        public static final String CONTENT_TYPE = "Content-Type";
    }

    public static class FormKeys {

        public static final String CLIENT_ID = "client_id";
        public static final String GRANT_TYPE = "grant_type";
        public static final String SCOPE = "scope";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String CODE = "code";
        public static final String CODE_VERIFIER = "code_verifier";
    }

    public static class FormValues {
        public static final String BENEFICIARY_NAME = "Payment Receiver";
        public static final String CREDITOR_NAME = "Payment Initiator";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String CLIENT_CREDENTIALS = "client_credentials";
        public static final String PIS_SCOPE = "pisp";
        public static final int NUMBER_OF_TRANSACTIONS = 1;
    }

    public static class DateFormat {
        public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    }

    public static class PaymentSteps {
        public static final String POST_SIGN_STEP = "post_sign_state";
        public static final String CONFIRM_PAYMENT_STEP = "confirm_payment_step";
    }

    public static class PaymentTypeInformation {
        public static final String SEPA_INSTANT_CREDIT_TRANSFER = "INST";
    }
}
