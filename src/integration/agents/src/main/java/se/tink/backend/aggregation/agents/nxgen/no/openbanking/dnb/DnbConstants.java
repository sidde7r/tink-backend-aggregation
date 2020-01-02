package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants.PersistentStorageKeys;

public final class DnbConstants {

    public static final String INTEGRATION_NAME = "dnb";
    public static final String BASE_URL = "https://api.psd.dnb.no";

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
        public static final String PAYMENTS = "/v1/payments/{paymentType}";
        public static final String GET_PAYMENT = PAYMENTS + "/{paymentId}";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = PersistentStorageKeys.OAUTH_2_TOKEN;
        public static final String CONSENT_OBJECT = "consentObject";
        public static final String STATE = "state";
    }

    public static class HeaderKeys {
        public static final String PSU_ID = "PSU-ID";
        public static final String CONSENT_ID = "Consent-ID";
        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String TPP_REDIRECT_URI = "TPP-Redirect-Uri";
        public static final String PSU_IP_ADDRESS = "PSU-IP-Address";
    }

    public static class HeaderValues {
        public static final String PSU_IP_ADDRESS = "0.0.0.0";
    }

    public static class CredentialsKeys {
        public static final String PSU_ID = "PSU-ID";
    }

    public static class BalanceTypes {
        public static final String EXPECTED = "expected";
    }

    public static class AccountTypes {
        public static final String SAVINGS_NO = "SPAREKONTO";
        public static final String SAVINGS_EN = "SAVING";
    }

    public static class QueryKeys {
        public static final String CLIENT_ID = "client_id";
        public static final String SCOPE = "scope";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String STATE = "state";
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String CODE = "code";
        public static final String FROM_DATE = "dateFrom";
        public static final String TO_DATE = "dateTo";
    }

    public static class QueryValues {
        public static final String BOTH = "both";
    }

    public static class ConsentRequestValues {
        public static final int FREQUENCY_PER_DAY = 4;
    }

    public static class IdTags {
        public static final String PAYMENT_TYPE = "paymentType";
        public static final String PAYMENT_ID = "paymentId";
    }

    public static class PaymentRequestValues {
        public static final String REGULATORY_REPORTING_CODE = "14";
        public static final String REGULATORY_REPORTING_INFORMATION = "Text message to creditor";
        public static final String CREDITOR_AGENT = "DNBADKKK";
    }
}
