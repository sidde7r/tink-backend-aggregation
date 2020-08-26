package se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen;

import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public final class RaiffeisenConstants {

    public static final String INTEGRATION_NAME = "raiffeisen-at";

    private RaiffeisenConstants() {
        throw new AssertionError();
    }

    public static final TypeMapper<TransactionalAccountType> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<TransactionalAccountType>builder()
                    .put(
                            TransactionalAccountType.CHECKING,
                            "CACC",
                            "CASH",
                            "CHAR",
                            "CISH",
                            "COMM",
                            "SLRY",
                            "TRAN",
                            "TRAS",
                            "CurrentAccount",
                            "Current")
                    .put(TransactionalAccountType.SAVINGS, "LLSV", "ONDP", "SVGS")
                    .build();

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_TOKEN = "No token in storage.";
        public static final String MAPPING =
                "Cannot map Ica payment status: %s to Tink payment status.";
        public static final String INVALID_IBAN = "IBAN invalid";
        public static final String UNKNOWN_ERROR = "Error unknown";
        public static final String CONSENT_UNKNOWN = "CONSENT_UNKNOWN";
    }

    public static class Urls {

        private static final String BASE_AUTH_URL = "https://sso-psd2.raiffeisen.at/as";
        private static final String BASE_API_URL = "https://psd2.raiffeisen.at/api";
        public static final URL AUTHENTICATE = new URL(BASE_AUTH_URL + Endpoints.AUTHENTICATE);
        public static final URL ACCOUNTS = new URL(BASE_API_URL + Endpoints.ACCOUNTS);
        public static final URL CONSENTS = new URL(BASE_API_URL + Endpoints.CONSENTS);
        public static final URL TRANSACTIONS = new URL(BASE_API_URL + Endpoints.TRANSACTIONS);
        public static final URL INITIATE_PAYMENT = new URL(BASE_API_URL + Endpoints.INITIATION);
        public static final URL GET_PAYMENT = new URL(BASE_API_URL + Endpoints.GET_PAYMENT);
        public static final URL CONSENT_STATUS = new URL(BASE_API_URL + Endpoints.CONSENT_STATUS);
    }

    public static class Endpoints {

        private static final String AUTHENTICATE = "/token.oauth2";
        private static final String ACCOUNTS = "/psd2-xs2a/rest/v1/accounts";
        private static final String CONSENTS = "/psd2-xs2a/rest/v1/consents";
        private static final String TRANSACTIONS =
                "/psd2-xs2a/rest/v1/accounts/{account-id}/transactions";
        private static final String INITIATION =
                "/psd2-xs2a/rest/v1/payments/sepa-credit-transfers";
        private static final String GET_PAYMENT =
                "/psd2-xs2a/rest/v1/payments/sepa-credit-transfers/{paymentId}";
        private static final String CONSENT_STATUS = CONSENTS + "/{consent-id}/status";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = "OAUTH_TOKEN";
        public static final String CONSENT_ID = "CONSENT_ID";
    }

    public static class QueryKeys {

        public static final String WITH_BALANCE = "withBalance";
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String STATE = "state";
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
    }

    public static class QueryValues {

        public static final String BOTH = "both";
    }

    public static class HeaderKeys {

        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String CONSENT_ID = "Consent-ID";
        public static final String AUTHORIZATION = "Authorization";
        public static final String TPP_REDIRECT_URI = "TPP-Redirect-URI";
        public static final String CACHE_CONTROL = "Cache-Control";
        public static final String PSU_IP_ADDRESS = "PSU-IP-Address";
    }

    public static class FormKeys {

        public static final String GRANT_TYPE = "grant_type";
        public static final String CLIENT_ID = "client_id";
        public static final String SCOPE = "scope";
    }

    public static class FormValues {

        public static final String GRANT_TYPE = "client_credentials";
        public static final String SCOPE = "apic-psd2";
    }

    public static class CredentialKeys {

        public static final String IBAN = "IBAN";
    }

    public static class BalanceTypes {

        public static final String FORWARD_AVAILABLE = "forwardAvailable";
        public static final String INTERIM_AVAILABLE = "interimAvailable";
    }

    public static class ParameterKeys {

        public static final String ACCOUNT_ID = "account-id";
        public static final String CONSENT_ID = "consent-id";
    }

    public static class Formats {

        public static final String TRANSACTION_DATE_FORMAT = "yyyy-MM-dd";
    }

    public static class HeaderValues {

        public static final String TOKEN_PREFIX = "Bearer ";
        public static final Object X_REQUEST_ID =
                "99391c7e-ad88-49ec-a2ad-99ddcb1f7721"; // Constant for sandbox
        public static final String CACHE_CONTROL = "no-cache";
        public static final String X_TINK_DEBUG_TRUST_ALL = "trust_all";
        public static final String PSU_IP_ADDRESS = "0.0.0.0";
    }

    public static class IdTags {

        public static final String PAYMENT_ID = "paymentId";
    }

    public static class StatusValues {

        public static final String REJECTED = "rejected";
        public static final String VALID = "valid";
    }

    public static class ErrorTexts {

        public static final String INVALID_IBAN = "invalid iban";
        public static final String ACCESS_EXCEEDED_TEXT = "XS2A006E";
    }

    public static class BodyValues {

        public static final Integer FREQUENCY_PER_DAY = 4;
        public static final Integer CONSENT_DAYS_VALID = 90;
    }

    public static class Currency {
        public static final String EUR = "EUR";
    }
}
