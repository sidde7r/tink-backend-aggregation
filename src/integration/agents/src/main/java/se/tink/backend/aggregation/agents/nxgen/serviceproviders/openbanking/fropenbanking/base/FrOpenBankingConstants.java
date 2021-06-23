package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base;

public final class FrOpenBankingConstants {

    public static class ErrorCodes {
        public static final String INCORRECT_ACCOUNT_NUMBER = "AC01";
        public static final String CLOSED_ACCOUNT_NUMBER = "AC04";
        public static final String BLOCKED_ACCOUNT = "AC06";
        public static final String TRANSACTION_FORBIDDEN = "AG01";
        public static final String INVALID_NUMBER_OF_TRANSACTIONS = "AM18";
        public static final String
                REQUESTED_EXECUTION_DATE_OR_REQUESTED_COLLECTION_DATE_TOO_FAR_IN_FUTURE = "CH03";
        public static final String INSUFFICIENT_FUNDS = "CUST";
        public static final String ORDER_CANCELLED = "DS02";
        public static final String INVALID_FILE_FORMAT = "FF01";
        public static final String FRAUDULENT_ORIGINATED = "FRAD";
        public static final String NOT_SPECIFIED_REASON_AGENT_GENERATED = "MS03";
        public static final String REGULATORY_REASON = "RR04";
        public static final String NO_ANSWER_FROM_CUSTOMER = "NOAS";
        public static final String MISSING_DEBTOR_ACCOUNT_OR_IDENTIFICATION = "RR01";
        public static final String MISSING_CREDITOR_NAME_OR_ADDRESS = "RR03";
        public static final String INVALID_PARTY_ID = "RR12";
    }
}
