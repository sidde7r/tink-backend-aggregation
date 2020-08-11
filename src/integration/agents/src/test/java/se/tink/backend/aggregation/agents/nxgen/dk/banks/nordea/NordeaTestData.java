package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea;

public class NordeaTestData {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/dk/banks/nordea/resources/";

    static final String ACCOUNT_1_API_ID = "NAID-DK-DKK-7418529630";
    static final String ACCOUNT_WITH_CREDIT_API_ID = "NAID-DK-DKK-2551697498";
    static final String TRANSACTIONS_CONTINUATION_KEY =
            "LEG_EjcqIJ8Abe9-bsc61MMA-UMCB7stdmeb2qPfCQVuitc=:rO0ABXNyAD9jb20ubm9yZGVhLmRiZi5kay5hY2NvdW50LmludGVncmF0aW9uLmNvcHlib29rcy5Db250aW51YXRpb25LZXlgVTENuzwM0gIAAUwAD2NvbnRpbnVhdGlvbmtleXQAEkxqYXZhL2xhbmcvU3RyaW5nO3hwdAA3UDAwMDAwMDAzNkc2ODk3NjQ3MDdHwIDAgMCAwIDAgMCAwIDAgDAwMDAwMDAwMDAwNTYyOTU0RA==";
    static final String CREDIT_CARD_ID = "9638527410963852";
    static final String INVESTMENT_1_ID = "DKWRAP1234123456";

    static final String FETCH_ACCOUNTS_FILE_PATH = TEST_DATA_PATH + "transactional_accounts.json";
    static final String FETCH_INVESTMENT_ACCOUNTS_FILE_PATH =
            TEST_DATA_PATH + "investment_accounts.json";
    static final String FETCH_CREDIT_CARDS_FILE_PATH = TEST_DATA_PATH + "credit_cards.json";
    static final String FETCH_CREDIT_CARDS_DETAILS_FILE_PATH =
            TEST_DATA_PATH + "credit_card_details.json";
    static final String FETCH_ACCOUNT_TRANSACTIONS_FILE_PATH =
            TEST_DATA_PATH + "account_transactions.json";
    static final String FETCH_ACCOUNT_TRANSACTIONS_CONTINUATION_FILE_PATH =
            TEST_DATA_PATH + "account_transactions_continuation.json";
    static final String FETCH_CREDIT_CARD_TRANSACTIONS_FILE_PATH =
            TEST_DATA_PATH + "credit_card_transactions.json";
    static final String FETCH_CREDIT_TRANSACTIONS_CONTINUATION_FILE_PATH =
            TEST_DATA_PATH + "credit_card_transactions_continuation.json";
}
