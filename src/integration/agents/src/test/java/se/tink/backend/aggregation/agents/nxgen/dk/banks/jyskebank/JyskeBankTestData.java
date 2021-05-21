package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank;

public class JyskeBankTestData {
    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/dk/banks/jyskebank/resources/";

    public static class InvestmentTestData {
        private static final String INVESTMENT_TEST_DATA_DIR = TEST_DATA_PATH + "investment/";

        public static final String INVESTMENT_ACCOUNTS_FILE =
                INVESTMENT_TEST_DATA_DIR + "investment_accounts.json";
        public static final String INVESTMENT_1_ID = "501287123";
    }
}
