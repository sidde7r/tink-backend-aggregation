package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

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
        public static final String INVALID_CONSENT_TRANSACTIONS =
                "Could not fetch transactions, because the consent is invalid. Please try again with a full consent.";
        public static final LocalizableKey BOTH_BALANCES_AND_TRANSACTIONS_CONSENTS_NEEDED =
                new LocalizableKey(
                        "You need to give both balance and transaction consent for all chosen accounts.");
        public static final int ACCESS_EXCEEDED_ERROR_CODE = 429;
        public static final int BAD_REQUEST_ERROR_CODE = 400;
        public static final String PERIOD_INVALID_ERROR = "PERIOD_INVALID";
        public static final String UNKNOWN_SIGNING_STEP = "Unknown step %s";
        public static final String MAPPING_STATUS_TO_TINK_STATUS_ERROR =
                "Cannot map: %s to Fineco payment status";
        public static final String FINECO_STATUS_MAPPING_ERROR =
                "Cannot map: %s to Fineco payment status";
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

    public static class BalanceTypes {
        public static final String FORWARD_AVAILABLE = "forwardAvailable";
        public static final String INTERIM_AVAILABLE = "interimAvailable";
    }

    public static class Formats {
        public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
        public static final String CURRENCY = "EUR";
    }

    public static class FormValues {
        public static final String MISSING_DESCRIPTION = "<Missing Description>";
    }
}
