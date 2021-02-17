package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea;

import org.junit.Ignore;

@Ignore
public class NordeaTestData {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/dk/banks/nordea/resources/";

    public static class CreditCardTestData {
        private static final String CREDIT_CARD_TEST_DATA_DIR = TEST_DATA_PATH + "creditcard/";

        public static final String CREDIT_CARDS_FILE =
                CREDIT_CARD_TEST_DATA_DIR + "credit_cards.json";
        public static final String CREDIT_CARD_DETAILS_FILE =
                CREDIT_CARD_TEST_DATA_DIR + "credit_card_details.json";
        public static final String CREDIT_CARD_ID = "9638527410963852";

        public static final String CREDIT_CARD_TRANSACTIONS_PAGE_1_FILE =
                CREDIT_CARD_TEST_DATA_DIR + "credit_card_transactions_page_1.json";
        public static final String CREDIT_CARD_TRANSACTIONS_PAGE_2_FILE =
                CREDIT_CARD_TEST_DATA_DIR + "credit_card_transactions_page_2.json";

        public static final String CREDIT_CARD_TRANSACTIONS_WITHOUT_DATE_FILE =
                CREDIT_CARD_TEST_DATA_DIR + "credit_card_transaction_without_date.json";

        public static final String CREDIT_CARD_TRANSACTIONS_FULL_PAGE_FILE =
                CREDIT_CARD_TEST_DATA_DIR + "credit_card_transactions_full_page.json";
        public static final String CREDIT_CARD_TRANSACTIONS_NOT_FULL_PAGE_FILE =
                CREDIT_CARD_TEST_DATA_DIR + "credit_card_transactions_not_full_page.json";
    }

    public static class LoansTestData {
        private static final String LOAN_TEST_DATA_DIR = TEST_DATA_PATH + "loans/";

        public static final String
                LOAN_DETAILS_PROPERTIES_ASSUMED_TO_EXIST_FOR_TINK_MODEL_CONVERSION_FILE =
                        LOAN_TEST_DATA_DIR
                                + "loan_details_properties_assumed_to_exist_for_tink_model_conversion.json";
        public static final String LOAN_DETAILS_WITH_ALL_PROPERTIES_RELEVANT_FOR_TINK_MODEL_FILE =
                LOAN_TEST_DATA_DIR
                        + "loan_details_with_all_properties_relevant_for_tink_model.json";
    }

    public static class TransactionalAccountTestData {
        private static final String TRANSACTIONAL_ACCOUNT_TEST_DATA_DIR =
                TEST_DATA_PATH + "transactionalaccount/";

        public static final String TRANSACTIONAL_ACCOUNTS_FILE =
                TRANSACTIONAL_ACCOUNT_TEST_DATA_DIR + "transactional_accounts.json";

        public static final String ACCOUNT_TRANSACTIONS_FILE =
                TRANSACTIONAL_ACCOUNT_TEST_DATA_DIR + "account_transactions.json";
        public static final String ACCOUNT_1_API_ID = "NAID-DK-DKK-7418529630";
        public static final String ACCOUNT_WITH_CREDIT_API_ID = "NAID-DK-DKK-2551697498";

        public static final String ACCOUNT_TRANSACTIONS_WITHOUT_DATE_FILE =
                TRANSACTIONAL_ACCOUNT_TEST_DATA_DIR + "transactions_without_date.json";

        public static final String ACCOUNT_TRANSACTIONS_WITH_CONTINUATION_FILE =
                TRANSACTIONAL_ACCOUNT_TEST_DATA_DIR + "account_transactions_with_continuation.json";
        public static final String ACCOUNT_TRANSACTIONS_CONTINUATION_FILE =
                TRANSACTIONAL_ACCOUNT_TEST_DATA_DIR + "account_transactions_continuation.json";
        public static final String TRANSACTIONS_CONTINUATION_KEY =
                "LEG_EjcqIJ8Abe9-bsc61MMA-UMCB7stdmeb2qPfCQVuitc=:rO0ABXNyAD9jb20ubm9yZGVhLmRiZi5kay5hY2NvdW50LmludGVncmF0aW9uLmNvcHlib29rcy5Db250aW51YXRpb25LZXlgVTENuzwM0gIAAUwAD2NvbnRpbnVhdGlvbmtleXQAEkxqYXZhL2xhbmcvU3RyaW5nO3hwdAA3UDAwMDAwMDAzNkc2ODk3NjQ3MDdHwIDAgMCAwIDAgMCAwIDAgDAwMDAwMDAwMDAwNTYyOTU0RA==";
    }

    public static class InvestmentTestData {
        private static final String INVESTMENT_TEST_DATA_DIR = TEST_DATA_PATH + "investment/";

        public static final String INVESTMENT_ACCOUNTS_FILE =
                INVESTMENT_TEST_DATA_DIR + "investment_accounts.json";
        public static final String INVESTMENT_1_ID = "DKWRAP1234123456";
        public static final String PENSION_ID = "CICS0000000000";
    }
}
