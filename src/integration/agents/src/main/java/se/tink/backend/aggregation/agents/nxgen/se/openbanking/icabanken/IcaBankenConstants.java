package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken;

import se.tink.libraries.i18n.LocalizableEnum;
import se.tink.libraries.i18n.LocalizableKey;

public final class IcaBankenConstants {
    public static final String PROVIDER_MARKET = "SE";

    private IcaBankenConstants() {
        throw new IllegalStateException("Utility class");
    }

    public static class ProductionUrls {
        private ProductionUrls() {}

        private static String BASE =
                "https://mtls-apimgw-icabanken.ica.se/t/icabanken.tenant/ica/bank/psd2/accounts/1.0.0";

        public static final String AUTH_PATH = "https://ims.icagruppen.se/oauth/v2/authorize";
        public static final String TOKEN_PATH =
                "https://mtls-ims.icagruppen.se/oauth/v2/mtls-token";
        public static final String ACCOUNTS_PATH = BASE + "/Accounts";
        public static final String TRANSACTIONS_PATH = BASE + "/Accounts/{accountId}/transactions";
    }

    public static class SandboxUrls {
        private SandboxUrls() {}

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
        private StorageKeys() {}

        public static final String TOKEN = "OAUTH_TOKEN";
    }

    public static class QueryKeys {
        private QueryKeys() {}

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
        private QueryValues() {}

        public static final String STATUS = "both";
        public static final String CODE = "code";
        public static final String SCOPE = "account";
        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String WITH_BALANCE = "true";
        public static final String REFRESH_TOKEN = "refresh_token";

        public static class PaymentProduct {
            private PaymentProduct() {}

            public static final String SEPA = "SepaCreditTransfer";
            public static final String INTERNATIONAL = "CrossBorderCreditTransfers";
        }
    }

    public static class HeaderKeys {
        private HeaderKeys() {}

        public static final String SCOPE = "scope";
    }

    public static class HeaderValues {
        private HeaderValues() {}

        public static final String ACCOUNT = "account";
    }

    public static class Account {
        private Account() {}

        public static final String INTERIM_AVAILABLE_BALANCE = "interimAvailable";
        public static final String ACCOUNT_ID = "accountId";
    }

    public static class ErrorTypes {
        private ErrorTypes() {}

        public static final String SERVER_ERROR = "server_error";
        public static final String RESOURCE_BLOCKED = "RESOURCE_BLOCKED";
        public static final String RESOURCE_UNKNOWN = "RESOURCE_UNKNOWN";
        public static final String UNKNOWN = "UNKNOWN";
    }

    public static class ErrorMessages {
        private ErrorMessages() {}

        public static final String MAPPING =
                "Cannot map Ica payment status: %s to Tink payment status.";
        public static final String MISSING_TOKEN = "Cannot find Token!";
        public static final String UNEXPECTED_INTERNAL_EXCEPTION = "unexpected internal exception";
        public static final String OLD_KYC_INFO = "Old KYC information";
        public static final String NO_ACCOUNT_INFO = "Accountinformation not found";
        public static final String INTERNAL_SERVER_ERROR = "internal server error";
        public static final String START_FAILED = "startfailed";
        public static final String CANCEL = "cancel";
    }

    public static class FormKeys {
        private FormKeys() {}

        public static final String GRANT_TYPE = "grant_type";
        public static final String CLIENT_ID = "client_id";
        public static final String CLIENT_SECRET = "client_secret";
    }

    public static class FormValues {
        private FormValues() {}

        public static final String GRANT_TYPE = "client_credentials";
    }

    public static class TransactionResponse {
        private TransactionResponse() {}

        public static final String TRANSACTION_NOT_FOUND = "Transaction not found";
    }

    public enum EndUserMessage implements LocalizableEnum {
        MUST_ANSWER_KYC(
                new LocalizableKey(
                        "To be able to refresh your accounts you need to answer some questions from your bank. Please log in to your bank's app or internet bank."));
        private final LocalizableKey userMessage;

        EndUserMessage(LocalizableKey userMessage) {
            this.userMessage = userMessage;
        }

        @Override
        public LocalizableKey getKey() {
            return this.userMessage;
        }
    }
}
