package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.data;

import java.nio.file.Paths;
import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class TransactionTestData {
    private static final String AMOUNT = "50.000";
    private static final String CURRENCY = "EUR";
    private static final String DESCRIPTION = "ADDEBITO BONIFICO";
    private static final String TIME = "T00:00:00";

    public static TransactionEntity getTransactionEntity(
            String accountingDate, String liquidationDate) {
        return getTransactionEntity(accountingDate, liquidationDate, AMOUNT);
    }

    public static TransactionEntity getTransactionEntity(
            String accountingDate, String liquidationDate, String amount) {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "\"shortDescription\": \""
                        + DESCRIPTION
                        + "\",\n"
                        + "\"extendedDescription\": \"ADDEBITO BONIFICO BLA BLA\",\n"
                        + getAmount(amount)
                        + getDateWithTime("dateAccountingCurrency", accountingDate)
                        + getDateWithTime("dateLiquidationValue", liquidationDate)
                        + "\"codeDescription\": \"GRAC01\"\n"
                        + "}",
                TransactionEntity.class);
    }

    private static String getAmount(String amount) {
        if (amount != null) {
            return "\"amountTransaction\": {\n"
                    + "\"amount\": \""
                    + amount
                    + "\",\n"
                    + "\"currency\": \""
                    + CURRENCY
                    + "\"\n"
                    + "},\n";
        }
        return "";
    }

    private static String getDateWithTime(String label, String date) {
        if (date != null) {
            return "\"" + label + "\": \"" + date + TIME + "\",\n";
        }
        return "";
    }

    public static class PagedTransactionTestData {
        private static final String TEST_DATA_PATH =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/it/openbanking/chebanca/resources";

        /**
         * returns a response telling there are more paged transactions to be fetched (paging by
         * nextAccounting)
         */
        public static GetTransactionsResponse getFirstResponseForScenario1() {
            return getResponse("PagingScenario1_FirstResponse.json");
        }

        /**
         * returns a response telling that all paged transactions has been fetched (paging by
         * nextAccounting)
         */
        public static GetTransactionsResponse getSecondResponseForScenario1() {
            return getResponse("PagingScenario1_SecondResponse.json");
        }

        /**
         * returns a response telling there are more paged transactions to be fetched (paging by
         * nextNotAccounting)
         */
        public static GetTransactionsResponse getFirstResponseForScenario2() {
            return getResponse("PagingScenario2_FirstResponse.json");
        }

        /**
         * returns a response telling that all paged transactions has been fetched (paging by
         * nextNotAccounting)
         */
        public static GetTransactionsResponse getSecondResponseForScenario2() {
            return getResponse("PagingScenario2_SecondResponse.json");
        }

        /**
         * returns a response telling there are more paged transactions to be fetched (paging by
         * nextNotAccounting and nextAccounting)
         */
        public static GetTransactionsResponse getFirstResponseForScenario3() {
            return getResponse("PagingScenario3_FirstResponse.json");
        }

        /**
         * returns a response telling that all not accounting transactions has been fetched but some
         * accounting are still left (paging by nextAccounting)
         */
        public static GetTransactionsResponse getSecondResponseForScenario3() {
            return getResponse("PagingScenario3_SecondResponse.json");
        }

        /**
         * returns a response telling that all paged transactions has been fetched (paging by
         * nextNotAccounting and nextAccounting)
         */
        public static GetTransactionsResponse getThirdResponseForScenario3() {
            return getResponse("PagingScenario3_ThirdResponse.json");
        }

        /**
         * returns a response telling there are no transactions paged (all will be fetched at once)
         */
        public static GetTransactionsResponse getResponseForScenario4() {
            return getResponse("PagingScenario4_Response.json");
        }

        private static GetTransactionsResponse getResponse(String fileName) {
            return SerializationUtils.deserializeFromString(
                    Paths.get(TEST_DATA_PATH, fileName).toFile(), GetTransactionsResponse.class);
        }
    }
}
