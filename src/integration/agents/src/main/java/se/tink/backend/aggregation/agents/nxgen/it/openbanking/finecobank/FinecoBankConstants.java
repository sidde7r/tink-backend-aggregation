package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank;

import com.google.common.collect.ImmutableList;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.http.URL;

public final class FinecoBankConstants {

    public static final String INTEGRATION_NAME = "finecobank";

    private FinecoBankConstants() {
        throw new AssertionError();
    }

    public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder()
                    .put(AccountTypes.CHECKING, "Multicurrency account", "Main account")
                    .put(AccountTypes.SAVINGS, "SVGS")
                    .put(AccountTypes.CREDIT_CARD, "FINECO CARD VISA MULTIFUNZIONE CHIP")
                    .ignoreKeys("OTHR")
                    .build();

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String MISSING_TOKEN = "Cannot find token.";
        public static final String INVALID_CONSENT_BALANCES =
                "Could not fetch the balances, because the consent is invalid. Please try again with a full consent.";
        public static final String INVALID_CONSENT_TRANSACTIONS =
                "Could not fetch transactions, because the consent is invalid. Please try again with a full consent.";
        public static final int ACCESS_EXCEEDED_ERROR_CODE = 429;
        public static final int PERIOD_INVALID_ERROR = 400;
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
    }

    public static class Endpoints {
        public static final String CONSENTS = "/consents";
        public static final String ACCOUNTS = "/accounts";
        public static final String TRANSACTIONS = "/accounts/{account-id}/transactions";
        public static final String CONSENT_STATUS = "/consents/{consentId}/status";
        public static final String CARD_ACCOUNTS = "/card-accounts";
        public static final String CARD_TRANSACTIONS = "/card-accounts/{account-id}/transactions";
        public static final String CONSENT_AUTHORIZATIONS = "/consents/{consentId}";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN;
        public static final String CONSENT_ID = "consentId";
        public static final String ACCOUNT_ID = "accountId";
        public static final String CARD_ID = "cardId";
        public static final String TRANSACTION_ACCOUNTS = "transactionAccounts";
        public static final String BALANCE_ACCOUNTS = "balanceAccounts";
    }

    public static class QueryKeys {

        public static final String WITH_BALANCE = "withBalance";
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
        public static final String STATE = "state";
        public static final String SUPPLEMENTAL_INFORMATION = "tpcb_%s";
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
    }

    public static class HeaderValues {
        public static final String X_REQUEST_ID_ACCOUNTS = "123e4567-e89b-42d3-a456-556642440048";
        public static final String X_REQUEST_ID_TRANSACTIONS =
                "123e4567-e89b-42d3-a456-556642440071";
        public static final String CONSENT_ID = "76b908cc-4605-4d06-8869-4387f5daa318";
    }

    public static class FormValues {
        public static final Boolean FALSE = false;
        public static final Boolean TRUE = true;
        public static final String ALL_ACCOUNTS = "allAccounts";
        public static final int NUMBER_DAYS = 90;
        public static final int FREQUENCY_PER_DAY = 4;
        public static final String MISSING_DESCRIPTION = "<Missing Description>";
        public static final int MAX_TIMEOUT_COUNTER = 10000;
    }

    public static class StatusValues {
        public static final String EXPIRED = "expired";
        public static final String RECEIVED = "received";
        public static final String VALID = "valid";
        public static final String REVOKED_BY_PSU = "revokedByPsu";
        public static final String TERMINATED_BY_TPP = "terminatedByTpp";
        public static final ImmutableList<String> FAILED =
                ImmutableList.of(EXPIRED, REVOKED_BY_PSU, TERMINATED_BY_TPP);
    }
}
