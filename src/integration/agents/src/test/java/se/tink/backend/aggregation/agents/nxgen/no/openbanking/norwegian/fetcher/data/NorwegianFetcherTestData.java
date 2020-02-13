package se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.fetcher.data;

import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.fetcher.transactionalaccount.rpc.BalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class NorwegianFetcherTestData {

    // ===========================TRANSACTIONS==========================================
    // booked
    public static final String TRANSACTION_0_DESCRIPTION = "TRANSACTION_0_DESCRIPTION";
    public static final String TRANSACTION_1_DESCRIPTION = "TRANSACTION_1_DESCRIPTION";
    public static final String TRANSACTION_2_DESCRIPTION = "TRANSACTION_2_DESCRIPTION";
    public static final String TRANSACTION_0_AMOUNT = "-75";
    public static final String TRANSACTION_1_AMOUNT = "40";
    public static final String TRANSACTION_2_AMOUNT = "-161.31";
    // pending
    public static final String TRANSACTION_3_DESCRIPTION = "TRANSACTION_3_DESCRIPTION";
    public static final String TRANSACTION_4_DESCRIPTION = "TRANSACTION_4_DESCRIPTION";
    public static final String TRANSACTION_3_AMOUNT = "300";
    public static final String TRANSACTION_4_AMOUNT = "-3563.58";
    // ===========================ACCOUNTS==============================================
    public static final String RESOURCE_ID = "someResourceId";
    public static final String BBAN = "01234567890";
    // ===========================BALANCES==============================================
    public static final String BALANCE = "3000.01";

    public static TransactionsResponse getTransactionResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"_links\": {\n"
                        + "    \"account\": {\n"
                        + "      \"href\": \"/accounts/ffa06464-8171-4908-9748-2aa6a61f7694\"\n"
                        + "    }"
                        + "  },\n"
                        + "  \"accountReference\": {\n"
                        + "    \"bban\": \"01234567890\",\n"
                        + "    \"currency\": \"NOK\"\n"
                        + "  },\n"
                        + "  \"transactions\": {\n"
                        + "    \"booked\": [\n"
                        + "      {\n"
                        + "        \"bookingDate\": \"2020-02-12T00:00:00.0000000+01:00\",\n"
                        + "        \"creditorName\": \"UBER TRIP HELP.UBER.COM  \",\n"
                        + "        \"exchangeRate\": [\n"
                        + "          {\n"
                        + "            \"currencyFrom\": \"EUR\",\n"
                        + "            \"currencyTo\": \"NOK\",\n"
                        + "            \"rate\": \"10.316369\",\n"
                        + "            \"rateDate\": \"2020-02-11T00:00:00.0000000\"\n"
                        + "          }\n"
                        + "        ],\n"
                        + "        \"mccName\": \"Taxicabs and Limousines             \",\n"
                        + "        \"proprietaryBankTransactionCode\": \"Kjøp\",\n"
                        + "        \"remittanceInformationUnstructured\": \""
                        + TRANSACTION_0_DESCRIPTION
                        + "\",\n"
                        + "        \"transactionAmount\": {\n"
                        + "          \"amount\": \""
                        + TRANSACTION_0_AMOUNT
                        + "\",\n"
                        + "          \"currency\": \"NOK\"\n"
                        + "        },\n"
                        + "        \"transactionDate\": \"2020-02-11T00:00:00.0000000\",\n"
                        + "        \"transactionId\": \"1298063261\",\n"
                        + "        \"valueDate\": \"2020-03-15T00:00:00.0000000+01:00\"\n"
                        + "      },\n"
                        + "      {\n"
                        + "        \"bookingDate\": \"2020-02-11T00:00:00.0000000+01:00\",\n"
                        + "        \"creditorName\": \"UBER TRIP HELP.UBER.COM  \",\n"
                        + "        \"exchangeRate\": [\n"
                        + "          {\n"
                        + "            \"currencyFrom\": \"EUR\",\n"
                        + "            \"currencyTo\": \"NOK\",\n"
                        + "            \"rate\": \"10.374752\",\n"
                        + "            \"rateDate\": \"2020-02-10T00:00:00.0000000\"\n"
                        + "          }\n"
                        + "        ],\n"
                        + "        \"mccName\": \"Taxicabs and Limousines             \",\n"
                        + "        \"proprietaryBankTransactionCode\": \"Kjøp\",\n"
                        + "        \"remittanceInformationUnstructured\": \""
                        + TRANSACTION_1_DESCRIPTION
                        + "\",\n"
                        + "        \"transactionAmount\": {\n"
                        + "          \"amount\": \""
                        + TRANSACTION_1_AMOUNT
                        + "\",\n"
                        + "          \"currency\": \"NOK\"\n"
                        + "        },\n"
                        + "        \"transactionDate\": \"2020-02-10T00:00:00.0000000\",\n"
                        + "        \"transactionId\": \"1297071204\",\n"
                        + "        \"valueDate\": \"2020-03-15T00:00:00.0000000+01:00\"\n"
                        + "      },\n"
                        + "      {\n"
                        + "        \"bookingDate\": \"2020-02-11T00:00:00.0000000+01:00\",\n"
                        + "        \"creditorName\": \"UBER TRIP HELP.UBER.COM  \",\n"
                        + "        \"exchangeRate\": [\n"
                        + "          {\n"
                        + "            \"currencyFrom\": \"EUR\",\n"
                        + "            \"currencyTo\": \"NOK\",\n"
                        + "            \"rate\": \"10.375\",\n"
                        + "            \"rateDate\": \"2020-02-10T00:00:00.0000000\"\n"
                        + "          }\n"
                        + "        ],\n"
                        + "        \"mccName\": \"Taxicabs and Limousines             \",\n"
                        + "        \"proprietaryBankTransactionCode\": \"Kjøp\",\n"
                        + "        \"remittanceInformationUnstructured\": \""
                        + TRANSACTION_2_DESCRIPTION
                        + "\",\n"
                        + "        \"transactionAmount\": {\n"
                        + "          \"amount\": \""
                        + TRANSACTION_2_AMOUNT
                        + "\",\n"
                        + "          \"currency\": \"NOK\"\n"
                        + "        },\n"
                        + "        \"transactionDate\": \"2020-02-10T00:00:00.0000000\",\n"
                        + "        \"transactionId\": \"1296940455\",\n"
                        + "        \"valueDate\": \"2020-03-15T00:00:00.0000000+01:00\"\n"
                        + "      }\n"
                        + "    ],\n"
                        + "    \"pending\": [\n"
                        + "      {\n"
                        + "        \"creditorAccount\": {},\n"
                        + "        \"creditorName\": \"\",\n"
                        + "        \"proprietaryBankTransactionCode\": \"Reservert\",\n"
                        + "        \"remittanceInformationStructured\": \"02-12 14:02:40 Panda TaxibetriebsGmbH Berlin DE\",\n"
                        + "        \"remittanceInformationUnstructured\": \""
                        + TRANSACTION_3_DESCRIPTION
                        + "\",\n"
                        + "        \"transactionAmount\": {\n"
                        + "          \"amount\": \""
                        + TRANSACTION_3_AMOUNT
                        + "\",\n"
                        + "          \"currency\": \"NOK\"\n"
                        + "        },\n"
                        + "        \"transactionDate\": \"2020-02-12T00:00:00.0000000+01:00\",\n"
                        + "        \"transactionId\": \"1297478698\"\n"
                        + "      },\n"
                        + "      {\n"
                        + "        \"creditorAccount\": {},\n"
                        + "        \"creditorName\": \"\",\n"
                        + "        \"proprietaryBankTransactionCode\": \"Reservert\",\n"
                        + "        \"remittanceInformationStructured\": \"02-12 04:27:26 INTERCONTI BERLIN BERLIN DE\",\n"
                        + "        \"remittanceInformationUnstructured\": \""
                        + TRANSACTION_4_DESCRIPTION
                        + "\",\n"
                        + "        \"transactionAmount\": {\n"
                        + "          \"amount\": \""
                        + TRANSACTION_4_AMOUNT
                        + "\",\n"
                        + "          \"currency\": \"NOK\"\n"
                        + "        },\n"
                        + "        \"transactionDate\": \"2020-02-12T00:00:00.0000000+01:00\",\n"
                        + "        \"transactionId\": \"1297385596\"\n"
                        + "      }\n"
                        + "    ]\n"
                        + "  }\n"
                        + "}",
                TransactionsResponse.class);
    }

    public static AccountsResponse getAccountsResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"accounts\": [\n"
                        + "    {\n"
                        + "      \"_links\": {\n"
                        + "        \"balances\": {\n"
                        + "          \"href\": \"/accounts/"
                        + RESOURCE_ID
                        + "/balances\"\n"
                        + "        },\n"
                        + "        \"transactions\": {\n"
                        + "          \"href\": \"/accounts/"
                        + RESOURCE_ID
                        + "/transactions\"\n"
                        + "        }\n"
                        + "      },\n"
                        + "      \"bban\": \""
                        + BBAN
                        + "\",\n"
                        + "      \"cashAccountType\": \"CurrentAccount\",\n"
                        + "      \"currency\": \"NOK\",\n"
                        + "      \"name\": \"CreditCard\",\n"
                        + "      \"product\": \"CreditCard\",\n"
                        + "      \"remittanceInformation\": {\n"
                        + "        \"accountType\": \"BBAN\",\n"
                        + "        \"bban\": \"01234567890\",\n"
                        + "        \"remittanceInformationStructured\": {\n"
                        + "          \"reference\": \"012345678901\"\n"
                        + "        },\t\t\t\t\n"
                        + "        \"resourceId\": \""
                        + RESOURCE_ID
                        + "\"\n"
                        + "      },\n"
                        + "      \"resourceId\": \""
                        + RESOURCE_ID
                        + "\",\n"
                        + "      \"status\": \"Enabled\"\n"
                        + "    }\n"
                        + "  ]\n"
                        + "}",
                AccountsResponse.class);
    }

    public static BalanceResponse getBalanceResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"accountReference\": {\n"
                        + "    \"bban\": \""
                        + BBAN
                        + "\",\n"
                        + "    \"currency\": \"NOK\"\n"
                        + "  },\n"
                        + "  \"balances\": [\n"
                        + "    {\n"
                        + "      \"balanceAmount\": {\n"
                        + "        \"amount\": \""
                        + BALANCE
                        + "\",\n"
                        + "        \"currency\": \"NOK\"\n"
                        + "      },\n"
                        + "      \"balanceType\": \"ClosingBooked\",\n"
                        + "      \"creditLimitIncluded\": false,\n"
                        + "      \"referenceDate\": \"2020-02-13T00:00:00.0000000+01:00\"\n"
                        + "    },\n"
                        + "    {\n"
                        + "      \"balanceAmount\": {\n"
                        + "        \"amount\": \""
                        + BALANCE
                        + "\",\n"
                        + "        \"currency\": \"NOK\"\n"
                        + "      },\n"
                        + "      \"balanceType\": \"Expected\",\n"
                        + "      \"creditLimitIncluded\": false,\n"
                        + "      \"referenceDate\": \"2020-02-13T10:45:35.1702989+01:00\"\n"
                        + "    },\n"
                        + "    {\n"
                        + "      \"balanceAmount\": {\n"
                        + "        \"amount\": \""
                        + BALANCE
                        + "\",\n"
                        + "        \"currency\": \"NOK\"\n"
                        + "      },\n"
                        + "      \"balanceType\": \"InterimAvailable\",\n"
                        + "      \"creditLimitIncluded\": false,\n"
                        + "      \"referenceDate\": \"2020-02-13T10:45:35.1703001+01:00\"\n"
                        + "    }\n"
                        + "  ]\n"
                        + "}",
                BalanceResponse.class);
    }
}
