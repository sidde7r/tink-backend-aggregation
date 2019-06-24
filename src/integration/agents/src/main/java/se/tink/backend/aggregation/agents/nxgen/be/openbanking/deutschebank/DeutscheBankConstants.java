package se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;

public final class DeutscheBankConstants {

    public static final String INTEGRATION_NAME = "deutschebank";

    private DeutscheBankConstants() {
        throw new AssertionError();
    }

    public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder()
                    .put(AccountTypes.CHECKING, "some_string1_the_integratee_uses")
                    .put(AccountTypes.SAVINGS, "some_string2_the_integratee_uses")
                    .put(AccountTypes.CREDIT_CARD, "some_string3_the_integratee_uses")
                    .ignoreKeys("some_string4_the_integratee_uses")
                    .build();

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String MISSING_TOKEN = "Cannot find token.";
    }

    public static class Urls {
        public static final String CONSENT = "/ais/DE/SB-DB/v1/consents";
        public static final String ACCOUNTS = "/ais/DE/SB-DB/v1/accounts";
        public static final String BALANCES = "/ais/DE/SB-DB/v1/accounts/%s/balances";
        public static final String TRANSACTIONS = "/ais/DE/SB-DB/v1/accounts/%s/transactions";
    }

    public static class StorageKeys {
        public static final String CONSENT_ID = "Consent-ID";
    }

    public static class QueryKeys {
        public static final String STATE = "state";
        public static final String WITH_BALANCE = "withBalance";
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String DELTA_LIST = "deltaList";
    }

    public static class HeaderValues {
        public static final String PSU_ID_TYPE = "DE_ONLB_DB";
        public static final String PSU_ID = "555-REDIRECTEXT-01";
        public static final Object PSY_IP_ADDRESS = "localhost";
    }

    public static class QueryValues {

        public static final String WITH_BALANCE = "true";
        public static final String BOOKING_STATUS = "booked";
        public static final String DELTA_LIST = "true";
    }

    public static class HeaderKeys {}

    public static class FormKeys {}

    public static class FormValues {}

    public static class LogTags {}

    public static class CredentialKeys {
        public static final String IBAN = "iban";
    }
}
