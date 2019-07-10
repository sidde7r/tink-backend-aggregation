package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants;

public final class DnbConstants {

    public static final String INTEGRATION_NAME = "dnb";

    private DnbConstants() {
        throw new AssertionError();
    }

    public static class ErrorMessages {
        public static final String SCA_REDIRECT_LINK_MISSING = "ScaRedirect link is missing.";
        public static final String URL_ENCODING_ERROR = "Url is not well defined.";
        public static final String OAUTH_TOKEN_ERROR =
                "This version of Dnb API doesn't support tokens.";
        public static final String WRONG_BALANCE_TYPE =
                "Wrong balance type. Expected type not found.";
        public static final String MISSING_CONFIGURATION = "Agent configuration is missing.";
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_TOKEN = "Cannot find token.";
    }

    public static class Urls {
        public static final String CONSENTS = "/v1/consents";
        public static final String ACCOUNTS = "/v1/accounts";
        public static final String BALANCES = ACCOUNTS + "/%s/balances";
        public static final String TRANSACTIONS = ACCOUNTS + "/%s/transactions";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN;
        public static final String CONSENT_OBJECT = "consentObject";
        public static final String STATE = "state";
    }

    public static class HeaderKeys {
        public static final String PSU_ID = "PSU-ID";
        public static final String CONSENT_ID = "Consent-ID";
        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String TPP_REDIRECT_URI = "TPP-Redirect-Uri";
    }

    public static class CredentialsKeys {
        public static final String PSU_ID = "PSU-ID";
    }

    public static class BalanceTypes {
        public static final String EXPECTED = "expected";
    }

    public static class QueryKeys {
        public static final String CLIENT_ID = "client_id";
        public static final String SCOPE = "scope";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String STATE = "state";
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String CODE = "code";
    }

    public static class QueryValues {
        public static final String BOTH = "both";
    }
}
