package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank;

import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;

public class DeutscheBankConstants {

    public static final String INTEGRATION_NAME = "deutschebank";
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
                    .setDefaultTranslationValue(TransactionalAccountType.OTHER)
                    .build();

    private DeutscheBankConstants() {
        throw new AssertionError();
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String MISSING_TOKEN = "Cannot find token.";
        public static final String MISSING_BALANCE = "Missing account balance.";
    }

    public static class Urls {
        public static final String CONSENT = "/v1/consents";
        public static final String ACCOUNTS = "/v1/accounts";
        public static final String BALANCES = "/v1/accounts/%s/balances";
        public static final String TRANSACTIONS = "/v1/accounts/%s/transactions";
    }

    public static class IdTags {
        public static final String REGION_ID = "regionId";
    }

    public static class StorageKeys {
        public static final String CONSENT_ID = "Consent-ID";
        public static final String TRANSACTIONS_URL = "TRANSACTIONS_URL";
    }

    public static class QueryKeys {
        public static final String STATE = "state";
        public static final String WITH_BALANCE = "withBalance";
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String DELTA_LIST = "deltaList";
    }

    public static class HeaderKeys {
        public static final String CONSENT_ID = "Consent-ID";
        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String TPP_REDIRECT_URI = "TPP-Redirect-URI";
        public static final String PSU_IP_ADDRESS = "PSU-IP-Address";
        public static final String PSU_ID = "PSU-ID";
        public static final String PSU_ID_TYPE = "PSU-ID-Type";
        public static final String TPP_NOK_REDIRECT_URI = "TPP-Nok-Redirect_URI";
    }

    public static class QueryValues {

        public static final String WITH_BALANCE = "true";
        public static final String BOOKING_STATUS = "both";
        public static final String DELTA_LIST = "true";
    }

    public static class CredentialKeys {
        public static final String IBAN = "iban";
        public static final String PSU_ID = "psuId";
        public static final String PASSWORD = "password";
    }

    public static class Accounts {
        public static final String BALANCE_CLOSING_BOOKED = "closingBooked";
        public static final String CLBD = "CLBD";
    }
}
