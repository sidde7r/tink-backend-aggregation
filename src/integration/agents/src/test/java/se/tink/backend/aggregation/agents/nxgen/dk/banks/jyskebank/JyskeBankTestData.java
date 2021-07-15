package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank;

import org.junit.Ignore;

@Ignore
public class JyskeBankTestData {
    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/dk/banks/jyskebank/resources/";

    public static class CreditCardTestData {
        private static final String CREDIT_CARD_TEST_DATA_DIR = TEST_DATA_PATH + "creditcard/";

        public static final String CREDIT_CARD_ACCOUNTS_FILE =
                CREDIT_CARD_TEST_DATA_DIR + "credit_card_accounts.json";
        public static final String CREDIT_CARD_TRANSACTIONS_PAGE_1_FILE =
                CREDIT_CARD_TEST_DATA_DIR + "credit_card_transactions_page_1.json";
        public static final String CREDIT_CARD_TRANSACTIONS_PAGE_2_FILE =
                CREDIT_CARD_TEST_DATA_DIR + "credit_card_transactions_page_2.json";
        public static final String CREDIT_CARD_1_ID = "12341234567";
    }

    public static class InvestmentTestData {
        private static final String INVESTMENT_TEST_DATA_DIR = TEST_DATA_PATH + "investment/";

        public static final String INVESTMENT_ACCOUNTS_FILE =
                INVESTMENT_TEST_DATA_DIR + "investment_accounts.json";
        public static final String INVESTMENT_1_ID = "501287123";
    }
}
