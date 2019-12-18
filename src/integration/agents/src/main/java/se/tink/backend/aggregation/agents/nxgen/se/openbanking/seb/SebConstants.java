package se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb;

public abstract class SebConstants {
    public static final String INTEGRATION_NAME = "seb";

    public static class Urls {
        public static final String BRANDED_ACCOUNTS = "/ais/v1/identified2/branded-card-accounts";
        public static final String BRANDED_TRANSACTIONS = "/{accountId}/transactions";
        public static final String BASE_URL = "https://api-sandbox.sebgroup.com";
        public static final String OAUTH = BASE_URL + "/mga/sps/oauth/oauth20/authorize";
        public static final String TOKEN = BASE_URL + "/mga/sps/oauth/oauth20/token";
        private static final String BASE_AIS = "/ais/v5";
        public static final String ACCOUNTS = BASE_URL + BASE_AIS + "/identified2/accounts";
        public static final String TRANSACTIONS =
                BASE_URL + BASE_AIS + "/identified2/accounts/{accountId}/transactions";
        private static final String BASE_PIS = BASE_URL + "/pis/v5/identified2/payments";
        public static final String CREATE_PAYMENT = BASE_PIS + "/{paymentProduct}";
        public static final String GET_PAYMENT = BASE_PIS + "/{paymentProduct}/{paymentId}";
        public static final String GET_PAYMENT_STATUS =
                BASE_PIS + "/{paymentProduct}/{paymentId}/status";
        public static final String SIGN_PAYMENT =
                BASE_PIS + "/{paymentProduct}/{paymentId}/authorisations";
    }

    public static class QueryKeys {
        public static final String CLIENT_ID = "client_id";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String SCOPE = "scope";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String STATE = "state";
        public static final String CODE = "code";
        public static final String CLIENT_SECRET = "client_secret";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String GRANT_TYPE = "grant_type";
        public static final String WITH_BALANCE = "withBalance";
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String TRANSACTION_SEQUENCE_NUMBER = "transactionSequenceNumber";
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
        public static final String BRAND_ID = "brandId";
    }

    public static class QueryValues {
        public static final String RESPONSE_TYPE_TOKEN = "code";
        public static final String SCOPE = "psd2_accounts psd2_payments";
        public static final String GRANT_TYPE = "authorization_code";
        public static final String BOOKED_TRANSACTIONS = "booked";
        public static final String WITH_BALANCE = "true";
        public static final String EUROCARD_BRAND_ID = "ecse";
    }

    public static class HeaderKeys {
        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String PSU_CORPORATE_ID = "PSU-Corporate-ID";
        public static final String PSU_IP_ADDRESS = "PSU-IP-Address";
    }

    public static class StorageKeys {
        public static final String BASE_URL = "BASE_BRANDED_URL";
        public static final String CLIENT_ID = "CLIENT_ID";
        public static final String CLIENT_SECRET = "CLIENT_SECRET";
        public static final String REDIRECT_URI = "REDIRECT_URI";
        public static final String TOKEN = "OAUTH_TOKEN";
        public static final String ACCOUNT_ID = "ACCOUNT_ID";
    }

    public static class Accounts {
        public static final String AVAILABLE_BALANCE = "interimAvailable";
        public static final String STATUS_ENABLED = "enabled";
    }

    public static class IdTags {
        public static final String ACCOUNT_ID = "accountId";
        public static final String PAYMENT_PRODUCT = "paymentProduct";
        public static final String PAYMENT_ID = "paymentId";
    }

    public static class Fetcher {
        public static final int START_PAGE = 1;
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String UNKNOWN_PAYMENT_PRODUCT =
                "The payment product could not be determined";
        public static final String CROSS_BORDER_PAYMENT_NOT_SUPPORTED =
                "Cross border payment is still not supported";
        public static final String AUTHENTICATION_METHOD_ID_MISSING =
                "Could not find authentication method id";
    }

    public static class HeaderValues {
        // This value is from their docs: https://developer.sebgroup.com/node/2187
        // Include the PSU-Corporate-ID parameter in the API call to trigger corporate data in the
        // dynamic sandbox. Leaving this field black will trigger private sandbox data.
        public static final Object PSU_CORPORATE_ID = "40073144970009";
    }

    public class SebSignSteps {
        public static final String SAMPLE_STEP = "SAMPLE_STEP";
    }
}
