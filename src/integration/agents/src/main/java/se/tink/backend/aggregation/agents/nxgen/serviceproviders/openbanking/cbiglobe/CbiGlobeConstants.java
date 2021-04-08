package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.account.enums.AccountFlag;

public final class CbiGlobeConstants {

    private CbiGlobeConstants() {
        throw new AssertionError();
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String BALANCE_NOT_FOUND = "Balance cannot be found.";
        public static final String MAPPING =
                "Cannot map payment status: %s to Tink payment status.";
        public static final String RESOURCE_UNKNOWN =
                "The addressed resource is unknown relative to the TPP";
        public static final String CBI_BAD_GATEWAY = "Controllo titolarita' conto fallito";
        public static final String PSU_CREDENTIALS_INVALID = "PSU_CREDENTIALS_INVALID";
    }

    public static class Urls {
        public static final String BASE_URL = "https://cbiglobeopenbankingapigateway.nexi.it";

        public static final URL TOKEN = new URL(BASE_URL + ApiServices.TOKEN);
        public static final URL CONSENTS = new URL(BASE_URL + ApiServices.CONSENTS);
        public static final URL UPDATE_CONSENTS = new URL(BASE_URL + ApiServices.UPDATE_CONSENTS);
        public static final URL ACCOUNTS = new URL(BASE_URL + ApiServices.ACCOUNTS);
        public static final URL BALANCES = new URL(BASE_URL + ApiServices.BALANCES);
        public static final URL TRANSACTIONS = new URL(BASE_URL + ApiServices.TRANSACTIONS);
        public static final URL CONSENTS_STATUS = new URL(BASE_URL + ApiServices.CONSENTS_STATUS);
        public static final URL PAYMENT = new URL(BASE_URL + ApiServices.PAYMENT);
        public static final URL FETCH_PAYMENT = new URL(BASE_URL + ApiServices.FETCH_PAYMENT);
        public static final URL FETCH_PAYMENT_STATUS =
                new URL(BASE_URL + ApiServices.FETCH_PAYMENT_STATUS);
        public static final URL CARD_ACCOUNTS = new URL(BASE_URL + ApiServices.CARD_ACCOUNTS);
        public static final URL CARD_BALANCES = new URL(BASE_URL + ApiServices.CARD_BALANCES);
        public static final URL CARD_TRANSACTIONS =
                new URL(BASE_URL + ApiServices.CARD_TRANSACTIONS);
    }

    public static class ApiServices {
        public static final String TOKEN = "/auth/oauth/v2/token";
        public static final String CONSENTS =
                "/platform/enabler/psd2orchestrator/ais/3.0.0/consents";
        public static final String UPDATE_CONSENTS =
                "/platform/enabler/psd2orchestrator/ais/2.3.2/consents";
        public static final String ACCOUNTS =
                "/platform/enabler/psd2orchestrator/ais/3.0.0/accounts";
        public static final String BALANCES =
                "/platform/enabler/psd2orchestrator/ais/2.3.2/accounts/{accountId}/balances";
        public static final String TRANSACTIONS =
                "/platform/enabler/psd2orchestrator/ais/2.4.0/accounts/{accountId}/transactions";
        public static final String CONSENTS_STATUS =
                "/platform/enabler/psd2orchestrator/ais/2.3.2/consents/{consentId}";
        private static final String PAYMENT =
                "/platform/enabler/psd2orchestrator/pis/2.3.2/{payment-service}/{payment-product}";
        private static final String FETCH_PAYMENT =
                "/platform/enabler/psd2orchestrator/pis/3.0.0/{payment-service}/{payment-product}/{payment-id}";
        private static final String FETCH_PAYMENT_STATUS =
                "/platform/enabler/psd2orchestrator/pis/2.3.2/{payment-service}/{payment-product}/{payment-id}/status";
        public static final String CARD_ACCOUNTS =
                "/platform/enabler/psd2orchestrator/ais/3.0.0/card-accounts";
        public static final String CARD_BALANCES =
                "/platform/enabler/psd2orchestrator/ais/2.3.2/card-accounts/{accountId}/balances";
        public static final String CARD_TRANSACTIONS =
                "/platform/enabler/psd2orchestrator/ais/2.3.2/card-accounts/{accountId}/transactions";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = PersistentStorageKeys.OAUTH_2_TOKEN;
        public static final String CONSENT_ID = "consent-id";
        public static final String ACCOUNTS = "accounts";
        public static final String LINK = "link";
        public static final String PAYMENT_PRODUCT = "payment-product";
    }

    public static class QueryKeys {
        public static final String GRANT_TYPE = "grant_type";
        public static final String STATE = "state";
        public static final String CODE = "code";
        public static final String BOOKING_STATUS = "booking_status";
        public static final String DATE_FROM = "date_from";
        public static final String DATE_TO = "date_to";
        public static final String TOTAL_PAGES = "cpaas-total-pages";
        public static final String OFFSET = "offset";
        public static final String SCOPE = "scope";
        public static final String RESULT = "result";
        public static final String LIMIT = "limit";
    }

    public static class QueryValues {
        public static final String CLIENT_CREDENTIALS = "client_credentials";
        public static final String BOTH = "both";
        public static final String BOOKED = "booked";
        public static final String PRODUCTION = "production";
        public static final String SUCCESS = "success";
        public static final String FAILURE = "failure";
    }

    public static class HeaderKeys {
        public static final String ASPSP_CODE = "aspsp-code";
        public static final String ASPSP_PRODUCT_CODE = "aspsp-product-code";
        public static final String DATE = "date";
        public static final String X_REQUEST_ID = "x-request-id";
        public static final String TPP_REDIRECT_URI = "tpp-redirect-uri";
        public static final String TPP_REDIRECT_PREFERRED = "tpp-redirect-preferred";
        public static final String TPP_NOK_REDIRECT_URI = "tpp-nok-redirect-uri";
        public static final String CONSENT_ID = "consent-id";
        public static final String PSU_IP_ADDRESS = "psu-ip-address";
        public static final String OPERATION_NAME = "operation-name";
        public static final String CODE = "code";
    }

    public static class HeaderValues {
        public static final String UPDATE_PSU_DATA = "updatePsuData";
        public static final String CODE = "code";
    }

    public static class FormValues {

        public static final String TRANSACTION_TYPE = "remote_transaction";
        public static final String ALL_ACCOUNTS = "allAccounts";
        public static final String TRUE = "true";
        public static final String FREQUENCY_PER_DAY_ONE = "1";
        public static final String FALSE = "false";
        public static final String FREQUENCY_PER_DAY = "4";
        public static final int CONSENT_VALID_PERIOD_DAYS = 89;
    }

    public static class IdTags {
        public static final String ACCOUNT_ID = "accountId";
        public static final String CONSENT_ID = "consentId";
        public static final String PAYMENT_ID = "payment-id";
    }

    public static class PathParameterKeys {
        public static final String PAYMENT_PRODUCT = "payment-product";
        public static final String PAYMENT_SERVICE = "payment-service";
    }

    public static class PathParameterValues {
        public static final String PAYMENT_SERVICE_PAYMENTS = "payments";
        public static final String PAYMENT_SERVICE_PERIODIC_PAYMENTS = "periodic-payments";
    }

    public static class PSUAuthenticationStatus {
        public static final String AUTHENTICATION_FAILED = "AUTHENTICATION_FAILED";
        public static final String IDENTIFICATION_REQUIRED = "IDENTIFICATION_REQUIRED";
        public static final String AUTHENTICATION_REQUIRED = "AUTHENTICATION_REQUIRED";
        public static final String AUTHENTICATED = "AUTHENTICATED";
        public static final String VERIFIED = "VERIFIED";
        public static final String FAILED = "FAILED";
    }

    public static class PaymentStep {
        public static final String IN_PROGRESS = "IN_PROGRESS";
    }

    public static class PaymentProduct {
        public static final String SEPA_CREDIT_TRANSFERS = "sepa-credit-transfers";
        public static final String INSTANT_SEPA_CREDIT_TRANSFERS = "instant-sepa-credit-transfers";
    }

    public static class HttpClient {
        public static final int MAX_RETRIES = 5;
        public static final int RETRY_SLEEP_MILLISECONDS = 1000;
    }

    public static class HttpClientParams {
        public static final int CLIENT_TIMEOUT = 60 * 1000;
    }

    public enum RequestContext {
        ACCOUNTS_GET,
        BALANCES_GET,
        CONSENT_CREATE,
        CONSENT_UPDATE,
        CONSENT_DETAILS,
        CONSENT_PSU_CREDENTIALS_UPDATE,
        PAYMENT_CREATE,
        PAYMENT_GET,
        PAYMENT_STATUS_GET,
        TOKEN_GET,
        TOKEN_IS_VALID,
        TRANSACTIONS_GET
    }

    public static final TransactionalAccountTypeMapper ACCOUNT_TYPE_MAPPER =
            TransactionalAccountTypeMapper.builder()
                    .put(
                            TransactionalAccountType.CHECKING,
                            AccountFlag.PSD2_PAYMENT_ACCOUNT,
                            "CACC",
                            "CASH",
                            "CHAR",
                            "CISH",
                            "COMM",
                            "SLRY",
                            "TRAN",
                            "TRAS")
                    .put(
                            TransactionalAccountType.SAVINGS,
                            AccountFlag.PSD2_PAYMENT_ACCOUNT,
                            "LLSV",
                            "ONDP",
                            "SVGS")
                    .build();
}
