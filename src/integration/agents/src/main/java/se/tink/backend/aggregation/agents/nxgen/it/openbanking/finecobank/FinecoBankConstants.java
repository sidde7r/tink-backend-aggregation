package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public final class FinecoBankConstants {

    private FinecoBankConstants() {
        throw new AssertionError();
    }

    public static final AccountTypeMapper ACCOUNT_TYPE_MAPPER =
            AccountTypeMapper.builder()
                    .put(AccountTypes.CHECKING, "Multicurrency account", "Main account")
                    .put(AccountTypes.SAVINGS, "SVGS")
                    .ignoreKeys("OTHR")
                    .build();

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String INVALID_CONSENT_BALANCES =
                "Could not fetch the balances, because the consent is invalid. Please try again with a full consent.";
        public static final String INVALID_CONSENT_TRANSACTIONS =
                "Could not fetch transactions, because the consent is invalid. Please try again with a full consent.";
        public static final int ACCESS_EXCEEDED_ERROR_CODE = 429;
        public static final int BAD_REQUEST_ERROR_CODE = 400;
        public static final String PERIOD_INVALID_ERROR = "PERIOD_INVALID";
        public static final String UNKNOWN_SIGNING_STEP = "Unknown step %s";
        public static final String MISSING_SIGNING_LINK = "Signing link is missing";
        public static final String MAPPING_STATUS_TO_TINK_STATUS_ERROR =
                "Cannot map: %s to Fineco payment status";
        public static final String FINECO_STATUS_MAPPING_ERROR =
                "Cannot map: %s to Fineco payment status";
        public static final String STATE_MISSING_ERROR =
                "State could not be retrieved from storage";
    }

    public static class Urls {
        public static final String BASE_URL = "https://api.finecobank.com/v1";
        public static final URL CONSENTS = new URL(BASE_URL + Endpoints.CONSENTS);
        public static final URL ACCOUNTS = new URL(BASE_URL + Endpoints.ACCOUNTS);
        public static final URL TRANSACTIONS = new URL(BASE_URL + Endpoints.TRANSACTIONS);
        public static final URL CONSENT_STATUS = new URL(BASE_URL + Endpoints.CONSENT_STATUS);
        public static final URL CARD_ACCOUNTS = new URL(BASE_URL + Endpoints.CARD_ACCOUNTS);
        public static final URL CARD_TRANSACTIONS = new URL(BASE_URL + Endpoints.CARD_TRANSACTIONS);
        public static final URL CONSENT_AUTHORIZATIONS =
                new URL(BASE_URL + Endpoints.CONSENT_AUTHORIZATIONS);
        public static final URL PAYMENT_INITIATION =
                new URL(BASE_URL + Endpoints.PAYMENT_INITIATION);
        public static final URL GET_PAYMENT = new URL(BASE_URL + Endpoints.GET_PAYMENT);
        public static final URL GET_PAYMENT_STATUS =
                new URL(BASE_URL + Endpoints.GET_PAYMENT_STATUS);
    }

    public static class Endpoints {
        public static final String CONSENTS = "/consents";
        public static final String ACCOUNTS = "/accounts";
        public static final String TRANSACTIONS = "/accounts/{account-id}/transactions";
        public static final String CONSENT_STATUS = "/consents/{consentId}/status";
        public static final String CARD_ACCOUNTS = "/card-accounts";
        public static final String CARD_TRANSACTIONS = "/card-accounts/{account-id}/transactions";
        public static final String CONSENT_AUTHORIZATIONS = "/consents/{consentId}";
        public static final String PAYMENT_INITIATION = "/payments/{paymentProduct}";
        public static final String GET_PAYMENT = "/payments/{paymentProduct}/{paymentId}";
        public static final String GET_PAYMENT_STATUS =
                "/payments/{paymentProduct}/{paymentId}/status";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = PersistentStorageKeys.OAUTH_2_TOKEN;
        public static final String CONSENT_ID = "consentId";
        public static final String ACCOUNT_ID = "accountId";
        public static final String CARD_ID = "cardId";
        public static final String TRANSACTIONS_CONSENTS = "transactionAccounts";
        public static final String BALANCES_CONSENTS = "balanceAccounts";
        public static final String STATE = "STATE";
    }

    public static class QueryKeys {

        public static final String WITH_BALANCE = "withBalance";
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
        public static final String STATE = "state";
    }

    public static class QueryValues {

        public static final String BOOKED = "booked";
    }

    public static class HeaderKeys {

        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String TPP_REDIRECT_URI = "TPP-Redirect-URI";
        public static final String CONSENT_ID = "Consent-ID";
        public static final String PSU_IP_ADDRESS = "PSU-IP-Address";
    }

    public static class BalanceTypes {

        public static final String FORWARD_AVAILABLE = "forwardAvailable";
        public static final String INTERIM_AVAILABLE = "interimAvailable";
    }

    public static class Formats {

        public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
        public static final String CURRENCY = "EUR";
    }

    public static class ParameterKeys {
        public static final String ACCOUNT_ID = "account-id";
        public static final String PAYMENT_PRODUCT = "paymentProduct";
        public static final String PAYMENT_ID = "paymentId";
    }

    public static class FinecoBankSignSteps {
        public static final String SAMPLE_STEP = "SAMPLE_STEP";
    }

    public static class HeaderValues {
        public static final String X_REQUEST_ID_PAYMENT_INITIATION =
                "4e0c57ba-e655-4ebd-9574-9aeeee104e7f";
        public static final String X_REQUEST_ID_GET_PAYMENT =
                "07bc32b0-d3a0-4261-9df3-acf7e21ca149";
        public static final String X_REQUEST_ID_GET_PAYMENT_STATUS =
                "d5997372-c072-47ce-89c9-e41f5562d582";
    }

    public static class FormValues {
        public static final Boolean FALSE = false;
        public static final Boolean TRUE = true;
        public static final String ALL_ACCOUNTS = "allAccounts";
        public static final int NUMBER_DAYS = 90;
        public static final int FREQUENCY_PER_DAY = 4;
        public static final String MISSING_DESCRIPTION = "<Missing Description>";
        public static final int MAX_POLLS_COUNTER = 50;
    }

    public static class StatusValues {
        public static final String VALID = "valid";
    }
}
