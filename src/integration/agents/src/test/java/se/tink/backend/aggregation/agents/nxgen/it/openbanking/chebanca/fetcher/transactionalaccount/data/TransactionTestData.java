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

    public static GetTransactionsResponse getTransactionsResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"_embedded\": {\n"
                        + "    \"transactions:generateExcel\": {\n"
                        + "      \"href\": \"retrieve?text=&nextAccounting=1&dateTo=2019-12-30&fromAmount=&period=&transactionsType=&nextNotAccounting=1&description=&accountingElementsNumber=&exportType=xls&notAccountingElementsNumber=&toAmount=&dateFrom=2019-09-30\",\n"
                        + "      \"method\": \"GET\"\n"
                        + "    },\n"
                        + "    \"transactions:generatePdf\": {\n"
                        + "      \"href\": \"retrieve?text=&nextAccounting=1&dateTo=2019-12-30&fromAmount=&period=&transactionsType=&nextNotAccounting=1&description=&accountingElementsNumber=&exportType=pdf&notAccountingElementsNumber=&toAmount=&dateFrom=2019-09-30\",\n"
                        + "      \"method\": \"GET\"\n"
                        + "    }\n"
                        + "  },\n"
                        + "  \"_links\": {\n"
                        + "    \"curies\": [\n"
                        + "      {\n"
                        + "        \"href\": \"https://api.chebanca.io/private/customers/2264691/products/0001470199/transactions/{rel}\",\n"
                        + "        \"name\": \"transactions\"\n"
                        + "      }\n"
                        + "    ],\n"
                        + "    \"self\": {\n"
                        + "      \"href\": \"retrieve\",\n"
                        + "      \"method\": \"GET\"\n"
                        + "    }\n"
                        + "  },\n"
                        + "  \"data\": {\n"
                        + "    \"numberTransactionsAccounting\": 529,\n"
                        + "    \"numberTransactionsNotAccounting\": 2,\n"
                        + "    \"showNotAccounting\": true,\n"
                        + "    \"totalEnter\": {\n"
                        + "      \"amount\": \"100507.790\",\n"
                        + "      \"currency\": \"EUR\"\n"
                        + "    },\n"
                        + "    \"totalOutput\": {\n"
                        + "      \"amount\": \"-130007.980\",\n"
                        + "      \"currency\": \"EUR\"\n"
                        + "    },\n"
                        + "    \"transactionDescriptions\": [\n"
                        + "      {\n"
                        + "        \"label\": \"Disposizione di pagamento\",\n"
                        + "        \"value\": \"Disposizione di pagamento\"\n"
                        + "      },\n"
                        + "      {\n"
                        + "        \"label\": \"Pagamento tramite POS\",\n"
                        + "        \"value\": \"Pagamento tramite POS\"\n"
                        + "      }\n"
                        + "    ],\n"
                        + "    \"transactionsAccounting\": [\n"
                        + "      {\n"
                        + "        \"amountTransaction\": {\n"
                        + "          \"amount\": \"-488.000\",\n"
                        + "          \"currency\": \"EUR\"\n"
                        + "        },\n"
                        + "        \"codeDescription\": \"C266\",\n"
                        + "        \"dateAccountingCurrency\": \"20191230T00:00:00\",\n"
                        + "        \"dateLiquidationValue\": \"20191230T00:00:00\",\n"
                        + "        \"extendedDescription\": \"Disposizione - RIF:077707111BEN. Giacomo Tia Fattuka n.187/2019 acconto libreria\",\n"
                        + "        \"idMoneyTransfer\": \"077707111\",\n"
                        + "        \"index\": \"A1\",\n"
                        + "        \"shortDescription\": \"Disposizione di pagamento\"\n"
                        + "      },\n"
                        + "      {\n"
                        + "        \"amountTransaction\": {\n"
                        + "          \"amount\": \"-9.500\",\n"
                        + "          \"currency\": \"EUR\"\n"
                        + "        },\n"
                        + "        \"codeDescription\": \"C296\",\n"
                        + "        \"dateAccountingCurrency\": \"20191227T00:00:00\",\n"
                        + "        \"dateLiquidationValue\": \"20191224T00:00:00\",\n"
                        + "        \"extendedDescription\": \"Pagam. POS - DEL 24/12 ORE 18:10 CARTA 1107425 C/O 4899111-CIOCCOLATITALIANI, MILANO CAU 43040 NDS 270824096\",\n"
                        + "        \"idMoneyTransfer\": \"\",\n"
                        + "        \"index\": \"A3\",\n"
                        + "        \"shortDescription\": \"Pagamento tramite POS\"\n"
                        + "      }\n"
                        + "    ],\n"
                        + "    \"transactionsNotAccounting\": [\n"
                        + "      {\n"
                        + "        \"amountTransaction\": {\n"
                        + "          \"amount\": \"-7.000\",\n"
                        + "          \"currency\": \"EUR\"\n"
                        + "        },\n"
                        + "        \"codeDescription\": \"\",\n"
                        + "        \"dateLiquidationValue\": \"20191228T10:52:04\",\n"
                        + "        \"extendedDescription\": \"\",\n"
                        + "        \"idMoneyTransfer\": \"\",\n"
                        + "        \"index\": \"N1\",\n"
                        + "        \"preAuthorized\": false,\n"
                        + "        \"shortDescription\": \"\"\n"
                        + "      },\n"
                        + "      {\n"
                        + "        \"amountTransaction\": {\n"
                        + "          \"amount\": \"-13.000\",\n"
                        + "          \"currency\": \"EUR\"\n"
                        + "        },\n"
                        + "        \"codeDescription\": \"\",\n"
                        + "        \"dateLiquidationValue\": \"20191227T20:50:04\",\n"
                        + "        \"extendedDescription\": \"\",\n"
                        + "        \"idMoneyTransfer\": \"\",\n"
                        + "        \"index\": \"N2\",\n"
                        + "        \"preAuthorized\": false,\n"
                        + "        \"shortDescription\": \"\"\n"
                        + "      }\n"
                        + "    ]\n"
                        + "  },\n"
                        + "  \"result\": {\n"
                        + "    \"flushMessages\": true,\n"
                        + "    \"messages\": [\n"
                        + "    ],\n"
                        + "    \"outcome\": \"SUCCESS\",\n"
                        + "    \"requestId\": \"717900376891617312259372\"\n"
                        + "  }\n"
                        + "}",
                GetTransactionsResponse.class);
    }

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
        private static final String TEST_DATA_PATH = "data/test/agents/it/ob/chebanca/transaction/";

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
