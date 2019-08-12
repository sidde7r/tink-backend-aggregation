package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken;

public final class IcaBankenConstants {

    public static final String INTEGRATION_NAME = "icabanken";

    public static class ProductionUrls {
        private static String BASE =
                "https://mtls-apimgw-icabanken.ica.se/t/icabanken.tenant/ica/bank/psd2/accounts/1.0.0";

        public static final String AUTH_PATH = "https://ims.icagruppen.se/oauth/v2/authorize";
        public static final String TOKEN_PATH =
                "https://mtls-ims.icagruppen.se/oauth/v2/mtls-token";
        public static final String ACCOUNTS_PATH = BASE + "/Accounts";
        public static final String TRANSACTIONS_PATH = BASE + "/Accounts/{accountId}/transactions";
    }

    public static class SandboxUrls {
        private static String BASE_URL = "accounts/1.0.0";
        public static final String ACCOUNTS_PATH = BASE_URL + "/Accounts";
        public static final String TRANSACTIONS_PATH =
                BASE_URL + "/Accounts/{accountId}/transactions";
        // Base url is not the same for this request
        public static final String FETCH_TOKEN = "https://ims.icagruppen.se/oauth/v2/token";

        private static String BASE_URL_PIS =
                "https://apimgw.ica.se/t/icabanken.tenant/ica/bank/services/psd2";
        public static final String GET_PAYMENT =
                BASE_URL_PIS + "/payments/sandbox/1.0.0/Payments/{paymentProduct}/{paymentId}";
        public static final String INITIATE_PAYMENT =
                BASE_URL_PIS + "/payments/sandbox/1.0.0/Payments/{paymentProduct}";
    }

    public static class StorageKeys {
        public static final String BASE_URL = "BASE_URL";
        public static final String TOKEN = "OAUTH_TOKEN";
    }

    public static class QueryKeys {
        public static final String BEARER = "Bearer";
        public static final String WITH_BALANCE = "withBalance";
        public static final String DATE_FROM = "from";
        public static final String DATE_TO = "to";
        public static final String STATUS = "status";
        public static final String CLIENT_ID = "client_id";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String SCOPE = "scope";
        public static final String STATE = "state";
        public static final String SSN = "ssn";
        public static final String GRANT_TYPE = "grant_type";
        public static final String CODE = "code";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String PAYMENT_PRODUCT = "paymentProduct";
        public static final String PAYMENT_ID = "paymentId";
    }

    public static class QueryValues {
        public static final String STATUS = "both";
        public static final String CODE = "code";
        public static final String SCOPE = "account";
        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String WITH_BALANCE = "true";
        public static final String ACCOUNT = "account";
        public static final String REFRESH_TOKEN = "refresh_token";

        public static class PaymentProduct {
            public static final String SEPA = "SepaCreditTransfer";
            public static final String INTERNATIONAL = "CrossBorderCreditTransfers";
        }
    }

    public static class HeaderKeys {
        public static final String REQUEST_ID = "X-Request-ID";
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String AUTHORIZATION = "authorization";
        public static final String SCOPE = "scope";

        public static final String TINK_DEBUG = "X-Tink-Debug";
        public static final String TRUST_ALL = "trust_all";
    }

    public static class HeaderValues {
        public static final String BEARER = "Bearer ";
        public static final String ACCOUNT = "account";
    }

    public class Account {
        public static final String INTERIM_AVAILABLE_BALANCE = "interimAvailable";
        public static final String ACCOUNT_ID = "accountId";
    }

    public static class ErrorMessages {
        public static final String MISSING_CONFIGURATION = "ICA Banken configuration missing.";
        public static final String MAPPING =
                "Cannot map Ica payment status: %s to Tink payment status.";
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_TOKEN = "Cannot find Token!";
        public static final String UNSUPPORTED_TYPE = "Unsupported payment type";
    }

    public static class FormKeys {
        public static final String GRANT_TYPE = "grant_type";
        public static final String CLIENT_ID = "client_id";
        public static final String CLIENT_SECRET = "client_secret";
    }

    public static class FormValues {
        public static final String GRANT_TYPE = "client_credentials";
    }

    public static class TransactionResponse {
        public static final String UNVALID_TIME_ERROR =
                "Minimum Value Date or Value Date can not be older than 18 months";
    }
}
