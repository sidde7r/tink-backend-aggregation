package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea;

public class NordeaTestData {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/dk/banks/nordea/resources/";

    public static class LoansTestDataFiles {

        private static final String LOAN_TEST_DATA_DIR = TEST_DATA_PATH + "loans/";

        public static final String
                LOAN_DETAILS_PROPERTIES_ASSUMED_TO_EXIST_FOR_TINK_MODEL_CONVERSION =
                        LOAN_TEST_DATA_DIR
                                + "loan_details_properties_assumed_to_exist_for_tink_model_conversion.json";
        public static final String LOAN_DETAILS_WITH_ALL_PROPERTIES_RELEVANT_FOR_TINK_MODEL =
                LOAN_TEST_DATA_DIR
                        + "loan_details_with_all_properties_relevant_for_tink_model.json";
    }

    public static final String ACCOUNT_1_API_ID = "NAID-DK-DKK-7418529630";
    public static final String ACCOUNT_WITH_CREDIT_API_ID = "NAID-DK-DKK-2551697498";
    public static final String TRANSACTIONS_CONTINUATION_KEY =
            "LEG_EjcqIJ8Abe9-bsc61MMA-UMCB7stdmeb2qPfCQVuitc=:rO0ABXNyAD9jb20ubm9yZGVhLmRiZi5kay5hY2NvdW50LmludGVncmF0aW9uLmNvcHlib29rcy5Db250aW51YXRpb25LZXlgVTENuzwM0gIAAUwAD2NvbnRpbnVhdGlvbmtleXQAEkxqYXZhL2xhbmcvU3RyaW5nO3hwdAA3UDAwMDAwMDAzNkc2ODk3NjQ3MDdHwIDAgMCAwIDAgMCAwIDAgDAwMDAwMDAwMDAwNTYyOTU0RA==";
    public static final String CREDIT_CARD_ID = "9638527410963852";
    public static final String INVESTMENT_1_ID = "DKWRAP1234123456";

    public static final String ACCOUNT_TRANSACTIONS_WITHOUT_DATE =
            TEST_DATA_PATH + "transactions_without_date.json";

    public static final String ACCOUNT_TRANSACTIONS = TEST_DATA_PATH + "account_transactions.json";

    public static final String CREDIT_CARD_ID_WITH_TRANSACTIONS_WITHOUT_DATE =
            "CREDIT_CARD_ID_WITH_PENDING_TRANSACTIONS_WITHOUT_DATE";
    static final String CREDIT_CARD_TRANSACTIONS_WITHOUT_DATE =
            TEST_DATA_PATH + "credit_card_transaction_without_date.json";

    static final String FETCH_ACCOUNTS_FILE_PATH = TEST_DATA_PATH + "transactional_accounts.json";
    static final String FETCH_INVESTMENT_ACCOUNTS_FILE_PATH =
            TEST_DATA_PATH + "investment_accounts.json";
    static final String FETCH_CREDIT_CARDS_FILE_PATH = TEST_DATA_PATH + "credit_cards.json";
    static final String FETCH_CREDIT_CARDS_DETAILS_FILE_PATH =
            TEST_DATA_PATH + "credit_card_details.json";
    public static final String ACCOUNT_TRANSACTIONS_WITH_CONTINUATION =
            TEST_DATA_PATH + "account_transactions_with_continuation.json";
    public static final String ACCOUNT_TRANSACTIONS_CONTINUATION =
            TEST_DATA_PATH + "account_transactions_continuation.json";

    static final String FETCH_CREDIT_CARD_TRANSACTIONS_FILE_PATH =
            TEST_DATA_PATH + "credit_card_transactions.json";
    static final String FETCH_CREDIT_TRANSACTIONS_CONTINUATION_FILE_PATH =
            TEST_DATA_PATH + "credit_card_transactions_continuation.json";
}
