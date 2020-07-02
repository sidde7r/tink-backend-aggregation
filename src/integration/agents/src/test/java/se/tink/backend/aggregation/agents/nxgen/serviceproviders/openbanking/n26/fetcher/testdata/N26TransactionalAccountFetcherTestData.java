package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.fetcher.testdata;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class N26TransactionalAccountFetcherTestData {

    private static final String TRANSACTION_DESCRIPTION_1 = "From Savings Account";
    private static final String TRANSACTION_DESCRIPTION_2 = "IKEA";
    private static final long TRANSACTION_DATE_2 = 1593439994418L;
    private static final long TRANSACTION_DATE_1 = 1593439984418L;
    private static final String TRANSACTION_AMOUNT_1 = "100.00";
    private static final String TRANSACTION_AMOUNT_2 = "-50.00";

    public static final String RESOURCE_ID = "DUMMY_ID";
    public static final String ACCOUNT_NAME = "Main Account";
    public static final String ACCOUNT_IBAN = "DE62500105177853469928";
    public static final String CURRENT_BALANCE = "100.50";
    public static final String AVAILABLE_BALANCE = "50.50";
    public static final String CURRENCY = "EUR";
    public static final String OFFSET_1 = "DUMMY_OFFSET_1";
    public static final String OFFSET_2 = "DUMMY_OFFSET_2";

    public static final String ACCOUNT_RESPONSE_JSON =
            "{\n"
                    + "    \"accounts\": [\n"
                    + "        {\n"
                    + "            \"id\": \""
                    + RESOURCE_ID
                    + "\",\n"
                    + "            \"name\": \""
                    + ACCOUNT_NAME
                    + "\",\n"
                    + "            \"bankId\": \"n26\",\n"
                    + "            \"accountFeatures\": {\n"
                    + "                \"supportsInformation\": true,\n"
                    + "                \"supportsSendPayment\": true,\n"
                    + "                \"supportsReceivePayment\": true\n"
                    + "            },\n"
                    + "            \"accountDetails\": {\n"
                    + "                \"identifier\": \"DUMMY_ACCOUNT_ID2\",\n"
                    + "                \"type\": \"CHECKING\",\n"
                    + "                \"status\": \"active\",\n"
                    + "                \"providerAccountDetails\": {\n"
                    + "                    \"nextGenPsd2AccountDetails\": {\n"
                    + "                        \"iban\": \""
                    + ACCOUNT_IBAN
                    + "\"\n"
                    + "                    }\n"
                    + "                }\n"
                    + "            }\n"
                    + "        },\n"
                    + "        {\n"
                    + "            \"id\": \"DUMMY_ID2\",\n"
                    + "            \"name\": \"Savings Account\",\n"
                    + "            \"bankId\": \"n26\",\n"
                    + "            \"accountFeatures\": {\n"
                    + "                \"supportsInformation\": true,\n"
                    + "                \"supportsSendPayment\": true,\n"
                    + "                \"supportsReceivePayment\": true\n"
                    + "            },\n"
                    + "            \"accountDetails\": {\n"
                    + "                \"identifier\": \"DUMMY_ACCOUNT_ID2\",\n"
                    + "                \"type\": \"SAVINGS\",\n"
                    + "                \"status\": \"active\",\n"
                    + "                \"providerAccountDetails\": {}\n"
                    + "            }\n"
                    + "        }\n"
                    + "    ]\n"
                    + "}";

    public static final String ACCOUNT_RESPONSE_JSON_WITHOUT_IBAN =
            "{\n"
                    + "    \"accounts\": [\n"
                    + "        {\n"
                    + "            \"id\": \""
                    + RESOURCE_ID
                    + "\",\n"
                    + "            \"name\": \""
                    + ACCOUNT_NAME
                    + "\",\n"
                    + "            \"bankId\": \"n26\",\n"
                    + "            \"accountFeatures\": {\n"
                    + "                \"supportsInformation\": true,\n"
                    + "                \"supportsSendPayment\": true,\n"
                    + "                \"supportsReceivePayment\": true\n"
                    + "            },\n"
                    + "            \"accountDetails\": {\n"
                    + "                \"identifier\": \"DUMMY_ACCOUNT_ID2\",\n"
                    + "                \"type\": \"CHECKING\",\n"
                    + "                \"status\": \"active\",\n"
                    + "                \"providerAccountDetails\": {\n"
                    + "                    \"nextGenPsd2AccountDetails\": {\n"
                    + "                    }\n"
                    + "                }\n"
                    + "            }\n"
                    + "        }\n"
                    + "    ]\n"
                    + "}";

    public static final String EMPTY_ACCOUNT_RESPONSE_JSON = "{\n" + "    \"accounts\": []\n" + "}";

    public static final String ACCOUNT_BALANCE_RESPONSE_JSON =
            "{\n"
                    + "  \"balance\": {\n"
                    + "    \"accountId\": \""
                    + RESOURCE_ID
                    + "\",\n"
                    + "    \"current\": {\n"
                    + "      \"currency\": \""
                    + CURRENCY
                    + "\",\n"
                    + "      \"value\": \""
                    + CURRENT_BALANCE
                    + "\"\n"
                    + "    },\n"
                    + "    \"available\": {\n"
                    + "      \"currency\": \""
                    + CURRENCY
                    + "\",\n"
                    + "      \"value\": \""
                    + AVAILABLE_BALANCE
                    + "\"\n"
                    + "    },\n"
                    + "    \"updatedAtMs\": \"1593462516560\"\n"
                    + "  },\n"
                    + "  \"status\": \"SUCCESSFUL_REQUEST\"\n"
                    + "}";

    public static final String ACCOUNT_BALANCE_RESPONSE_JSON_WITHOUT_CURRENT_BALANCE =
            "{\n"
                    + "  \"balance\": {\n"
                    + "    \"accountId\": \""
                    + RESOURCE_ID
                    + "\",\n"
                    + "    \"available\": {\n"
                    + "      \"currency\": \""
                    + CURRENCY
                    + "\",\n"
                    + "      \"value\": \""
                    + AVAILABLE_BALANCE
                    + "\"\n"
                    + "    },\n"
                    + "    \"updatedAtMs\": \"1593462516560\"\n"
                    + "  },\n"
                    + "  \"status\": \"SUCCESSFUL_REQUEST\"\n"
                    + "}";

    public static final String ACCOUNT_BALANCE_RESPONSE_JSON_WITHOUT_AVAILABLE_BALANCE =
            "{\n"
                    + "  \"balance\": {\n"
                    + "    \"accountId\": \""
                    + RESOURCE_ID
                    + "\",\n"
                    + "    \"current\": {\n"
                    + "      \"currency\": \""
                    + CURRENCY
                    + "\",\n"
                    + "      \"value\": \""
                    + AVAILABLE_BALANCE
                    + "\"\n"
                    + "    },\n"
                    + "    \"updatedAtMs\": \"1593462516560\"\n"
                    + "  },\n"
                    + "  \"status\": \"SUCCESSFUL_REQUEST\"\n"
                    + "}";

    public static final String TRANSACTIONS_RESPONSE_JSON =
            "{\n"
                    + "  \"transactions\": [{\n"
                    + "    \"id\": \"TRANSACTION_ID_2\",\n"
                    + "    \"type\": \"DEBIT\",\n"
                    + "    \"status\": \"SUCCESS\",\n"
                    + "    \"amount\": {\n"
                    + "      \"currency\": \"EUR\",\n"
                    + "      \"value\": \""
                    + TRANSACTION_AMOUNT_2
                    + "\"\n"
                    + "    },\n"
                    + "    \"description\": \""
                    + TRANSACTION_DESCRIPTION_2
                    + "\",\n"
                    + "    \"createdAtMs\": \""
                    + TRANSACTION_DATE_2
                    + "\",\n"
                    + "    \"providerTransactionDetails\": {\n"
                    + "      \"nextGenPsd2TransactionDetails\": {\n"
                    + "        \"debtorAccount\": {\n"
                    + "          \"iban\": \""
                    + ACCOUNT_IBAN
                    + "\"\n"
                    + "        }\n"
                    + "      }\n"
                    + "    }\n"
                    + "  }, {\n"
                    + "    \"id\": \"TRANSACTION_ID_1\",\n"
                    + "    \"type\": \"CREDIT\",\n"
                    + "    \"status\": \"SUCCESS\",\n"
                    + "    \"amount\": {\n"
                    + "      \"currency\": \"EUR\",\n"
                    + "      \"value\": \""
                    + TRANSACTION_AMOUNT_1
                    + "\"\n"
                    + "    },\n"
                    + "    \"description\": \""
                    + TRANSACTION_DESCRIPTION_1
                    + "\",\n"
                    + "    \"createdAtMs\": \""
                    + TRANSACTION_DATE_1
                    + "\",\n"
                    + "    \"providerTransactionDetails\": {\n"
                    + "      \"nextGenPsd2TransactionDetails\": {\n"
                    + "        \"debtorName\": \""
                    + TRANSACTION_DESCRIPTION_1
                    + "\"\n"
                    + "      }\n"
                    + "    }\n"
                    + "  }],\n"
                    + "  \"offset\": \""
                    + OFFSET_1
                    + "\",\n"
                    + "  \"status\": \"SUCCESSFUL_REQUEST\"\n"
                    + "}";

    public static final String EMPTY_TRANSACTIONS_RESPONSE_JSON =
            "{\n"
                    + "  \"transactions\": [],\n"
                    + "  \"offset\": \""
                    + OFFSET_2
                    + "\",\n"
                    + "  \"status\": \"SUCCESSFUL_REQUEST\"\n"
                    + "}";
}
