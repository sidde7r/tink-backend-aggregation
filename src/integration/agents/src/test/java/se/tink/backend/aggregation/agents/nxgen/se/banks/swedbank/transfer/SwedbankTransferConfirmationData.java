package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.transfer;

import org.junit.Ignore;

@Ignore
public class SwedbankTransferConfirmationData {

    public static final String TRANSFER_ID = "1234567";

    public static final String TRANSFER_CONFIRMED_RESPONSE =
            "{\n"
                    + "  \"confirmedTotalAmount\": {\n"
                    + "    \"amount\": \"***MASKED***\",\n"
                    + "    \"currencyCode\": \"SEK\"\n"
                    + "  },\n"
                    + "  \"possibleEinvoices\": [],\n"
                    + "  \"confirmedTransactions\": [\n"
                    + "    {\n"
                    + "      \"amount\": \"100,00\",\n"
                    + "      \"currencyCode\": \"SEK\",\n"
                    + "      \"fromAccount\": {\n"
                    + "        \"amount\": \"***MASKED***\",\n"
                    + "        \"currencyCode\": \"SEK\",\n"
                    + "        \"name\": \"***MASKED***\",\n"
                    + "        \"accountNumber\": \"***MASKED***\",\n"
                    + "        \"clearingNumber\": \"***MASKED***\",\n"
                    + "        \"fullyFormattedNumber\": \"***MASKED***\"\n"
                    + "      },\n"
                    + "      \"transactions\": [\n"
                    + "        {\n"
                    + "          \"links\": {\n"
                    + "            \"self\": {\n"
                    + "              \"method\": \"GET\",\n"
                    + "              \"uri\": \"/v5/payment/1234567\"\n"
                    + "            },\n"
                    + "            \"edit\": {\n"
                    + "              \"method\": \"PUT\",\n"
                    + "              \"uri\": \"/v5/payment/transfer/1234567\"\n"
                    + "            }\n"
                    + "          },\n"
                    + "          \"id\": \"1234567\",\n"
                    + "          \"type\": \"TRANSFER\",\n"
                    + "          \"amount\": \"100,00\",\n"
                    + "          \"currencyCode\": \"SEK\",\n"
                    + "          \"noteToSender\": \"\",\n"
                    + "          \"transfer\": {\n"
                    + "            \"noteToRecipient\": \"\",\n"
                    + "            \"dateDependency\": \"DIRECT\",\n"
                    + "            \"periodicity\": \"***MASKED***\",\n"
                    + "            \"changedButNotConfirm\": false,\n"
                    + "            \"toAccount\": {\n"
                    + "              \"amount\": \"***MASKED***\",\n"
                    + "              \"name\": \"***MASKED***\",\n"
                    + "              \"accountNumber\": \"***MASKED***\",\n"
                    + "              \"clearingNumber\": \"***MASKED***\",\n"
                    + "              \"fullyFormattedNumber\": \"***MASKED***\"\n"
                    + "            }\n"
                    + "          },\n"
                    + "          \"selected\": true\n"
                    + "        }\n"
                    + "      ]\n"
                    + "    }\n"
                    + "  ],\n"
                    + "  \"pendingCounterSignTransactions\": [],\n"
                    + "  \"rejectedTransactions\": []\n"
                    + "}";

    public static final String TRANSFER_REJECTED_DUE_TO_INSUFFICIENT_FUNDS_RESPONSE =
            "{\n"
                    + "  \"confirmedTotalAmount\": {\n"
                    + "    \"amount\": \"***MASKED***\",\n"
                    + "    \"currencyCode\": \"SEK\"\n"
                    + "  },\n"
                    + "  \"possibleEinvoices\": [],\n"
                    + "  \"confirmedTransactions\": [],\n"
                    + "  \"pendingCounterSignTransactions\": [],\n"
                    + "  \"rejectedTransactions\": [\n"
                    + "    {\n"
                    + "      \"amount\": \"100,00\",\n"
                    + "      \"currencyCode\": \"SEK\",\n"
                    + "      \"fromAccount\": {\n"
                    + "        \"amount\": \"***MASKED***\",\n"
                    + "        \"currencyCode\": \"SEK\",\n"
                    + "        \"name\": \"***MASKED***\",\n"
                    + "        \"accountNumber\": \"***MASKED***\",\n"
                    + "        \"clearingNumber\": \"***MASKED***\",\n"
                    + "        \"fullyFormattedNumber\": \"***MASKED***\"\n"
                    + "      },\n"
                    + "      \"transactions\": [\n"
                    + "        {\n"
                    + "          \"rejectionCauses\": [\n"
                    + "            {\n"
                    + "              \"code\": \"INSUFFICIENT_FUNDS\",\n"
                    + "              \"message\": \"Överföringen kunde inte genomföras för att det inte fanns tillräckligt med pengar på kontot.\"\n"
                    + "            }\n"
                    + "          ],\n"
                    + "          \"links\": {\n"
                    + "            \"self\": {\n"
                    + "              \"method\": \"GET\",\n"
                    + "              \"uri\": \"/v5/payment/1234567\"\n"
                    + "            },\n"
                    + "            \"edit\": {\n"
                    + "              \"method\": \"PUT\",\n"
                    + "              \"uri\": \"/v5/payment/transfer/1234567\"\n"
                    + "            }\n"
                    + "          },\n"
                    + "          \"id\": \"1234567\",\n"
                    + "          \"type\": \"TRANSFER\",\n"
                    + "          \"amount\": \"100,00\",\n"
                    + "          \"currencyCode\": \"SEK\",\n"
                    + "          \"noteToSender\": \"\",\n"
                    + "          \"transfer\": {\n"
                    + "            \"noteToRecipient\": \"\",\n"
                    + "            \"dateDependency\": \"DIRECT\",\n"
                    + "            \"periodicity\": \"***MASKED***\",\n"
                    + "            \"changedButNotConfirm\": false,\n"
                    + "            \"toAccount\": {\n"
                    + "              \"amount\": \"***MASKED***\",\n"
                    + "              \"name\": \"***MASKED***\",\n"
                    + "              \"accountNumber\": \"***MASKED***\",\n"
                    + "              \"clearingNumber\": \"***MASKED***\",\n"
                    + "              \"fullyFormattedNumber\": \"***MASKED***\"\n"
                    + "            }\n"
                    + "          },\n"
                    + "          \"selected\": true\n"
                    + "        }\n"
                    + "      ]\n"
                    + "    }\n"
                    + "  ]\n"
                    + "}";

    public static final String TRANSFER_REJECTED_DUE_TO_WRONG_DATE =
            "{\n"
                    + "  \"confirmedTotalAmount\": {\n"
                    + "    \"amount\": \"***MASKED***\",\n"
                    + "    \"currencyCode\": \"SEK\"\n"
                    + "  },\n"
                    + "  \"possibleEinvoices\": [],\n"
                    + "  \"confirmedTransactions\": [],\n"
                    + "  \"pendingCounterSignTransactions\": [],\n"
                    + "  \"rejectedTransactions\": [\n"
                    + "    {\n"
                    + "      \"amount\": \"100,00\",\n"
                    + "      \"currencyCode\": \"SEK\",\n"
                    + "      \"fromAccount\": {\n"
                    + "        \"amount\": \"***MASKED***\",\n"
                    + "        \"currencyCode\": \"SEK\",\n"
                    + "        \"name\": \"***MASKED***\",\n"
                    + "        \"accountNumber\": \"***MASKED***\",\n"
                    + "        \"clearingNumber\": \"***MASKED***\",\n"
                    + "        \"fullyFormattedNumber\": \"***MASKED***\"\n"
                    + "      },\n"
                    + "      \"transactions\": [\n"
                    + "        {\n"
                    + "          \"rejectionCauses\": [\n"
                    + "            {\n"
                    + "              \"code\": \"DATE_PASSED\",\n"
                    + "              \"message\": \"Vänligen välj ett datum framåt i tiden. Lämna fältet blankt om du vill att pengarna ska nå mottagaren så snart som möjligt.\"\n"
                    + "            }\n"
                    + "          ],\n"
                    + "          \"links\": {\n"
                    + "            \"self\": {\n"
                    + "              \"method\": \"GET\",\n"
                    + "              \"uri\": \"/v5/payment/1234567\"\n"
                    + "            },\n"
                    + "            \"edit\": {\n"
                    + "              \"method\": \"PUT\",\n"
                    + "              \"uri\": \"/v5/payment/transfer/1234567\"\n"
                    + "            }\n"
                    + "          },\n"
                    + "          \"id\": \"1234567\",\n"
                    + "          \"type\": \"TRANSFER\",\n"
                    + "          \"amount\": \"100,00\",\n"
                    + "          \"currencyCode\": \"SEK\",\n"
                    + "          \"noteToSender\": \"\",\n"
                    + "          \"transfer\": {\n"
                    + "            \"noteToRecipient\": \"\",\n"
                    + "            \"dateDependency\": \"DIRECT\",\n"
                    + "            \"periodicity\": \"***MASKED***\",\n"
                    + "            \"changedButNotConfirm\": false,\n"
                    + "            \"toAccount\": {\n"
                    + "              \"amount\": \"***MASKED***\",\n"
                    + "              \"name\": \"***MASKED***\",\n"
                    + "              \"accountNumber\": \"***MASKED***\",\n"
                    + "              \"clearingNumber\": \"***MASKED***\",\n"
                    + "              \"fullyFormattedNumber\": \"***MASKED***\"\n"
                    + "            }\n"
                    + "          },\n"
                    + "          \"selected\": true\n"
                    + "        }\n"
                    + "      ]\n"
                    + "    }\n"
                    + "  ]\n"
                    + "}";

    public static final String TRANSFER_REJECTED_WITH_UNKNOWN_CAUSE_RESPONSE =
            "{\n"
                    + "  \"confirmedTotalAmount\": {\n"
                    + "    \"amount\": \"***MASKED***\",\n"
                    + "    \"currencyCode\": \"SEK\"\n"
                    + "  },\n"
                    + "  \"possibleEinvoices\": [],\n"
                    + "  \"confirmedTransactions\": [],\n"
                    + "  \"pendingCounterSignTransactions\": [],\n"
                    + "  \"rejectedTransactions\": [\n"
                    + "    {\n"
                    + "      \"amount\": \"100,00\",\n"
                    + "      \"currencyCode\": \"SEK\",\n"
                    + "      \"fromAccount\": {\n"
                    + "        \"amount\": \"***MASKED***\",\n"
                    + "        \"currencyCode\": \"SEK\",\n"
                    + "        \"name\": \"***MASKED***\",\n"
                    + "        \"accountNumber\": \"***MASKED***\",\n"
                    + "        \"clearingNumber\": \"***MASKED***\",\n"
                    + "        \"fullyFormattedNumber\": \"***MASKED***\"\n"
                    + "      },\n"
                    + "      \"transactions\": [\n"
                    + "        {\n"
                    + "          \"rejectionCauses\": [\n"
                    + "            {\n"
                    + "              \"code\": \"UNKNOWN ERROR\",\n"
                    + "              \"message\": \"Why could this be?\"\n"
                    + "            }\n"
                    + "          ],\n"
                    + "          \"links\": {\n"
                    + "            \"self\": {\n"
                    + "              \"method\": \"GET\",\n"
                    + "              \"uri\": \"/v5/payment/1234567\"\n"
                    + "            },\n"
                    + "            \"edit\": {\n"
                    + "              \"method\": \"PUT\",\n"
                    + "              \"uri\": \"/v5/payment/transfer/1234567\"\n"
                    + "            }\n"
                    + "          },\n"
                    + "          \"id\": \"1234567\",\n"
                    + "          \"type\": \"TRANSFER\",\n"
                    + "          \"amount\": \"100,00\",\n"
                    + "          \"currencyCode\": \"SEK\",\n"
                    + "          \"noteToSender\": \"\",\n"
                    + "          \"transfer\": {\n"
                    + "            \"noteToRecipient\": \"\",\n"
                    + "            \"dateDependency\": \"DIRECT\",\n"
                    + "            \"periodicity\": \"***MASKED***\",\n"
                    + "            \"changedButNotConfirm\": false,\n"
                    + "            \"toAccount\": {\n"
                    + "              \"amount\": \"***MASKED***\",\n"
                    + "              \"name\": \"***MASKED***\",\n"
                    + "              \"accountNumber\": \"***MASKED***\",\n"
                    + "              \"clearingNumber\": \"***MASKED***\",\n"
                    + "              \"fullyFormattedNumber\": \"***MASKED***\"\n"
                    + "            }\n"
                    + "          },\n"
                    + "          \"selected\": true\n"
                    + "        }\n"
                    + "      ]\n"
                    + "    }\n"
                    + "  ]\n"
                    + "}";

    public static final String TRANSFER_REJECTED_WITH_NO_CAUSE_RESPONSE =
            "{\n"
                    + "  \"confirmedTotalAmount\": {\n"
                    + "    \"amount\": \"***MASKED***\",\n"
                    + "    \"currencyCode\": \"SEK\"\n"
                    + "  },\n"
                    + "  \"possibleEinvoices\": [],\n"
                    + "  \"confirmedTransactions\": [],\n"
                    + "  \"pendingCounterSignTransactions\": [],\n"
                    + "  \"rejectedTransactions\": [\n"
                    + "    {\n"
                    + "      \"amount\": \"100,00\",\n"
                    + "      \"currencyCode\": \"SEK\",\n"
                    + "      \"fromAccount\": {\n"
                    + "        \"amount\": \"***MASKED***\",\n"
                    + "        \"currencyCode\": \"SEK\",\n"
                    + "        \"name\": \"***MASKED***\",\n"
                    + "        \"accountNumber\": \"***MASKED***\",\n"
                    + "        \"clearingNumber\": \"***MASKED***\",\n"
                    + "        \"fullyFormattedNumber\": \"***MASKED***\"\n"
                    + "      },\n"
                    + "      \"transactions\": [\n"
                    + "        {\n"
                    + "          \"links\": {\n"
                    + "            \"self\": {\n"
                    + "              \"method\": \"GET\",\n"
                    + "              \"uri\": \"/v5/payment/1234567\"\n"
                    + "            },\n"
                    + "            \"edit\": {\n"
                    + "              \"method\": \"PUT\",\n"
                    + "              \"uri\": \"/v5/payment/transfer/1234567\"\n"
                    + "            }\n"
                    + "          },\n"
                    + "          \"id\": \"1234567\",\n"
                    + "          \"type\": \"TRANSFER\",\n"
                    + "          \"amount\": \"100,00\",\n"
                    + "          \"currencyCode\": \"SEK\",\n"
                    + "          \"noteToSender\": \"\",\n"
                    + "          \"transfer\": {\n"
                    + "            \"noteToRecipient\": \"\",\n"
                    + "            \"dateDependency\": \"DIRECT\",\n"
                    + "            \"periodicity\": \"***MASKED***\",\n"
                    + "            \"changedButNotConfirm\": false,\n"
                    + "            \"toAccount\": {\n"
                    + "              \"amount\": \"***MASKED***\",\n"
                    + "              \"name\": \"***MASKED***\",\n"
                    + "              \"accountNumber\": \"***MASKED***\",\n"
                    + "              \"clearingNumber\": \"***MASKED***\",\n"
                    + "              \"fullyFormattedNumber\": \"***MASKED***\"\n"
                    + "            }\n"
                    + "          },\n"
                    + "          \"selected\": true\n"
                    + "        }\n"
                    + "      ]\n"
                    + "    }\n"
                    + "  ]\n"
                    + "}";

    public static final String TRANSFER_REJECTED_WITH_MULTIPLE_CAUSES_RESPONSE =
            "{\n"
                    + "  \"confirmedTotalAmount\": {\n"
                    + "    \"amount\": \"***MASKED***\",\n"
                    + "    \"currencyCode\": \"SEK\"\n"
                    + "  },\n"
                    + "  \"possibleEinvoices\": [],\n"
                    + "  \"confirmedTransactions\": [],\n"
                    + "  \"pendingCounterSignTransactions\": [],\n"
                    + "  \"rejectedTransactions\": [\n"
                    + "    {\n"
                    + "      \"amount\": \"100,00\",\n"
                    + "      \"currencyCode\": \"SEK\",\n"
                    + "      \"fromAccount\": {\n"
                    + "        \"amount\": \"***MASKED***\",\n"
                    + "        \"currencyCode\": \"SEK\",\n"
                    + "        \"name\": \"***MASKED***\",\n"
                    + "        \"accountNumber\": \"***MASKED***\",\n"
                    + "        \"clearingNumber\": \"***MASKED***\",\n"
                    + "        \"fullyFormattedNumber\": \"***MASKED***\"\n"
                    + "      },\n"
                    + "      \"transactions\": [\n"
                    + "        {\n"
                    + "          \"rejectionCauses\": [\n"
                    + "            {\n"
                    + "              \"code\": \"INSUFFICIENT_FUNDS\",\n"
                    + "              \"message\": \"Överföringen kunde inte genomföras för att det inte fanns tillräckligt med pengar på kontot.\"\n"
                    + "            },\n"
                    + "            {\n"
                    + "              \"code\": \"BECAUSE_WHY_NOT_TWO_CAUSES\",\n"
                    + "              \"message\": \"One rejection cause wasn't enought.\"\n"
                    + "            }\n"
                    + "          ],\n"
                    + "          \"links\": {\n"
                    + "            \"self\": {\n"
                    + "              \"method\": \"GET\",\n"
                    + "              \"uri\": \"/v5/payment/1234567\"\n"
                    + "            },\n"
                    + "            \"edit\": {\n"
                    + "              \"method\": \"PUT\",\n"
                    + "              \"uri\": \"/v5/payment/transfer/1234567\"\n"
                    + "            }\n"
                    + "          },\n"
                    + "          \"id\": \"1234567\",\n"
                    + "          \"type\": \"TRANSFER\",\n"
                    + "          \"amount\": \"100,00\",\n"
                    + "          \"currencyCode\": \"SEK\",\n"
                    + "          \"noteToSender\": \"\",\n"
                    + "          \"transfer\": {\n"
                    + "            \"noteToRecipient\": \"\",\n"
                    + "            \"dateDependency\": \"DIRECT\",\n"
                    + "            \"periodicity\": \"***MASKED***\",\n"
                    + "            \"changedButNotConfirm\": false,\n"
                    + "            \"toAccount\": {\n"
                    + "              \"amount\": \"***MASKED***\",\n"
                    + "              \"name\": \"***MASKED***\",\n"
                    + "              \"accountNumber\": \"***MASKED***\",\n"
                    + "              \"clearingNumber\": \"***MASKED***\",\n"
                    + "              \"fullyFormattedNumber\": \"***MASKED***\"\n"
                    + "            }\n"
                    + "          },\n"
                    + "          \"selected\": true\n"
                    + "        }\n"
                    + "      ]\n"
                    + "    }\n"
                    + "  ]\n"
                    + "}";

    public static final String NO_CONFIRMED_OR_REJECTED_TRANSFERS_RESPONSE =
            "{\n"
                    + "  \"confirmedTotalAmount\": {\n"
                    + "    \"amount\": \"***MASKED***\",\n"
                    + "    \"currencyCode\": \"SEK\"\n"
                    + "  },\n"
                    + "  \"possibleEinvoices\": [],\n"
                    + "  \"confirmedTransactions\": [],\n"
                    + "  \"pendingCounterSignTransactions\": [],\n"
                    + "  \"rejectedTransactions\": []\n"
                    + "}";
}
