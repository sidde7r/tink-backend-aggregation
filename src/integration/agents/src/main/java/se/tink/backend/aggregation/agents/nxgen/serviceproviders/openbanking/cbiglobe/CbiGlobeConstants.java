package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.http.URL;

public final class CbiGlobeConstants {

    public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder()
                    .put(AccountTypes.CHECKING, "some_string1_the_integratee_uses")
                    .put(AccountTypes.SAVINGS, "some_string2_the_integratee_uses")
                    .put(AccountTypes.CREDIT_CARD, "some_string3_the_integratee_uses")
                    .ignoreKeys("some_string4_the_integratee_uses")
                    .build();

    private CbiGlobeConstants() {
        throw new AssertionError();
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String MISSING_TOKEN = "Cannot find token.";
        public static final String BALANCE_NOT_FOUND = "Balance cannot be found.";
        public static final String MAPPING =
                "Cannot map payment status: %s to Tink payment status.";
    }

    public static class Urls {
        public static final String BASE_URL = "https://cbiglobeopenbankingapigateway.nexi.it";

        public static final URL TOKEN = new URL(BASE_URL + ApiServices.TOKEN);
        public static final URL CONSENTS = new URL(BASE_URL + ApiServices.CONSENTS);
        public static final URL ACCOUNTS = new URL(BASE_URL + ApiServices.ACCOUNTS);
        public static final URL BALANCES = new URL(BASE_URL + ApiServices.BALANCES);
        public static final URL TRANSACTIONS = new URL(BASE_URL + ApiServices.TRANSACTIONS);
        public static final URL CONSENTS_STATUS = new URL(BASE_URL + ApiServices.CONSENTS_STATUS);
        public static final URL PAYMENT = new URL(BASE_URL + ApiServices.PAYMENT);
        public static final URL FETCH_PAYMENT = new URL(BASE_URL + ApiServices.FETCH_PAYMENT);
    }

    // TODO: Remove 'sbx' prefix for production
    public static class ApiServices {
        public static final String TOKEN = "/auth/oauth/v2/token";
        public static final String CONSENTS =
                "/platform/enabler/psd2orchestrator/ais/2.3.2/consents";
        public static final String ACCOUNTS =
                "/platform/enabler/psd2orchestrator/ais/2.3.2/accounts";
        public static final String BALANCES =
                "/platform/enabler/psd2orchestrator/ais/2.3.2/accounts/{accountId}/balances";
        public static final String TRANSACTIONS =
                "/platform/enabler/psd2orchestrator/ais/2.3.2/accounts/{accountId}/transactions";
        public static final String CONSENTS_STATUS =
                "/platform/enabler/psd2orchestrator/ais/2.3.2/consents/{consentId}";
        private static final String PAYMENT =
                "/sbx/platform/enabler/psd2orchestrator/pis/2.3.2/payments/sepa-credit-transfers";
        private static final String FETCH_PAYMENT =
                "/sbx/platform/enabler/psd2orchestrator/pis/2.3.2/payments/sepa-credit-transfers/{payment-id}";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN;
        public static final String CONSENT_ID = "consent-id";
        public static final String ACCOUNTS = "accounts";
        public static final String LINK = "link";
    }

    public static class QueryKeys {
        public static final String GRANT_TYPE = "grant_type";
        public static final String STATE = "state";
        public static final String CODE = "code";
        public static final String BOOKING_STATUS = "booking_status";
        public static final String DATE_FROM = "date_from";
        public static final String DATE_TO = "date_to";
    }

    public static class QueryValues {
        public static final String CLIENT_CREDENTIALS = "client_credentials";
        public static final String CODE = "code";
        public static final String BOTH = "both";
        public static final String BOOKED = "booked";
        public static final String STATE = "state";
    }

    public static class HeaderKeys {
        public static final String AUTHORIZATION = "Authorization";
        public static final String ASPSP_CODE = "aspsp-code";
        public static final String ASPSP_PRODUCT_CODE = "aspsp-product-code";
        public static final String PSU_ID = "psu-id";
        public static final String DATE = "date";
        public static final String X_REQUEST_ID = "x-request-id";
        public static final String TPP_REDIRECT_URI = "tpp-redirect-uri";
        public static final String TPP_NOK_REDIRECT_URI = "tpp-nok-redirect-uri";
        public static final String CONSENT_ID = "consent-id";
        public static final String PSU_ID_TYPE = "psu-id-type";
        public static final String PSU_IP_ADDRESS = "psu-ip-address";
    }

    public static class HeaderValues {
        public static final String DEFAULT_PSU_IP_ADDRESS = "0.0.0.0";
    }

    public static class FormValues {

        public static final String TRANSACTION_TYPE = "remote_transaction";
        public static final String ALL_ACCOUNTS = "allAccounts";
        public static final String TRUE = "true";
        public static final String FREQUENCY_PER_DAY_ONE = "1";
        public static final String FALSE = "false";
        public static final String FREQUENCY_PER_DAY = "4";
    }

    public static class LogTags {
        public static final String UNKNOWN_STATE = "unknown state";
    }

    public static class IdTags {
        public static final String ACCOUNT_ID = "accountId";
        public static final String CONSENT_ID = "consentId";
        public static final String PAYMENT_ID = "payment-id";
    }
}
