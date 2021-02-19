package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.creditcard;

import org.junit.Ignore;

@Ignore
public class SwedbankDefaultCreditCardFetcherTestData {
    public static final String ENGAGEMENT_OVERVIEW_RESPONSE =
            "{\n"
                    + "  \"cardCredit\": {\n"
                    + "    \"currency\": \"SEK\",\n"
                    + "    \"name\": \"Kortkredit\",\n"
                    + "    \"availableAmount\": \"10 000,00\"\n"
                    + "  },\n"
                    + "  \"showCreditCardLink\": true,\n"
                    + "  \"errorFetchingCreditCards\": false,\n"
                    + "  \"transactionAccounts\": [],\n"
                    + "  \"savingAccounts\": [],\n"
                    + "  \"loanAccounts\": [],\n"
                    + "  \"cardAccounts\": [\n"
                    + "    {\n"
                    + "      \"blocked\": false,\n"
                    + "      \"internetPurchases\": false,\n"
                    + "      \"creditLimit\": \"20 000,00\",\n"
                    + "      \"productCode\": \"VBCP\",\n"
                    + "      \"visaBusinessCard\": true,\n"
                    + "      \"availableForFavouriteAccount\": false,\n"
                    + "      \"availableForPriorityAccount\": false,\n"
                    + "      \"name\": \"Visa Business Card P\",\n"
                    + "      \"cardNumber\": \"4444 44** **** 4444\",\n"
                    + "      \"availableAmount\": \"19 862,00\",\n"
                    + "      \"currency\": \"SEK\"\n"
                    + "    },\n"
                    + "    {\n"
                    + "      \"blocked\": true,\n"
                    + "      \"internetPurchases\": false,\n"
                    + "      \"creditLimit\": \"30 000,00\",\n"
                    + "      \"productCode\": \"mc_privat_charge\",\n"
                    + "      \"visaBusinessCard\": false,\n"
                    + "      \"availableForFavouriteAccount\": false,\n"
                    + "      \"availableForPriorityAccount\": false,\n"
                    + "      \"name\": \"betal- och kreditkort mastercard\",\n"
                    + "      \"cardNumber\": \"3333 33** **** 3333\",\n"
                    + "      \"availableAmount\": \"30 000,00\",\n"
                    + "      \"currency\": \"SEK\"\n"
                    + "    },\n"
                    + "    {\n"
                    + "      \"status\": \"ACTIVE\",\n"
                    + "      \"blocked\": false,\n"
                    + "      \"internetPurchases\": true,\n"
                    + "      \"supplementaryLink\": {\n"
                    + "        \"links\": {\n"
                    + "          \"next\": {\n"
                    + "            \"method\": \"GET\",\n"
                    + "            \"uri\": \"/v5/card/creditcard/0000000000000000000000000000000000000001/supplementary\"\n"
                    + "          }\n"
                    + "        }\n"
                    + "      },\n"
                    + "      \"creditLimit\": \"50 000,00\",\n"
                    + "      \"usedCredit\": \"3 295,04\",\n"
                    + "      \"productCode\": \"MC_GULD\",\n"
                    + "      \"visaBusinessCard\": false,\n"
                    + "      \"links\": {\n"
                    + "        \"edit\": {\n"
                    + "          \"method\": \"PUT\",\n"
                    + "          \"uri\": \"/v5/card/creditcard/0000000000000000000000000000000000000001/internetpurchases\"\n"
                    + "        },\n"
                    + "        \"next\": {\n"
                    + "          \"method\": \"GET\",\n"
                    + "          \"uri\": \"/v5/engagement/cardaccount/0000000000000000000000000000000000000001\"\n"
                    + "        }\n"
                    + "      },\n"
                    + "      \"availableForFavouriteAccount\": false,\n"
                    + "      \"availableForPriorityAccount\": false,\n"
                    + "      \"id\": \"0000000000000000000000000000000000000001\",\n"
                    + "      \"name\": \"Betal- och kreditkort Mastercard Guld\",\n"
                    + "      \"cardNumber\": \"1111 11** **** 1111\",\n"
                    + "      \"availableAmount\": \"46 704,96\",\n"
                    + "      \"currency\": \"SEK\",\n"
                    + "      \"details\": {\n"
                    + "        \"links\": {\n"
                    + "          \"next\": {\n"
                    + "            \"method\": \"GET\",\n"
                    + "            \"uri\": \"/v5/engagement/account/0000000000000000000000000000000000000001\"\n"
                    + "          }\n"
                    + "        }\n"
                    + "      }\n"
                    + "    }"
                    + "  ],\n"
                    + "  \"supplementaryCardAccounts\": [],\n"
                    + "  \"eligibleForOverdraftLimit\": true,\n"
                    + "  \"accessToHSB\": false\n"
                    + "}";

    public static final String CREDIT_CARD_DETAILS_RESPONSE =
            "{\n"
                    + "  \"cardAccount\": {\n"
                    + "    \"status\": \"ACTIVE\",\n"
                    + "    \"creditLimit\": \"50 000,00\",\n"
                    + "    \"currentBalance\": \"-3 295,04\",\n"
                    + "    \"internetPurchases\": true,\n"
                    + "    \"cardHolder\": \"Sven Svensson\",\n"
                    + "    \"creditCardProductId\": \"MC_GULD\",\n"
                    + "    \"cardTermsDocumentLink\": \"http://www.entercard.com/terms-and-conditions/mastercard_guld_8999.pdf\",\n"
                    + "    \"usedCredit\": \"3 295,04\",\n"
                    + "    \"reservedAmount\": \"0,00\",\n"
                    + "    \"expireDate\": \"2020-11\",\n"
                    + "    \"supplementary\": false,\n"
                    + "    \"blocked\": false,\n"
                    + "    \"internetPurchasesDisplayed\": true,\n"
                    + "    \"links\": {\n"
                    + "      \"edit\": {\n"
                    + "        \"method\": \"PUT\",\n"
                    + "        \"uri\": \"/v5/card/creditcard/0000000000000000000000000000000000000001/internetpurchases\"\n"
                    + "      },\n"
                    + "      \"next\": {\n"
                    + "        \"method\": \"GET\",\n"
                    + "        \"uri\": \"/v5/card/creditcard/0000000000000000000000000000000000000001/supplementary\"\n"
                    + "      }\n"
                    + "    },\n"
                    + "    \"availableForFavouriteAccount\": false,\n"
                    + "    \"availableForPriorityAccount\": false,\n"
                    + "    \"id\": \"0000000000000000000000000000000000000001\",\n"
                    + "    \"name\": \"Betal- och kreditkort Mastercard Guld\",\n"
                    + "    \"cardNumber\": \"1111 11** **** 1111\",\n"
                    + "    \"availableAmount\": \"46 704,96\",\n"
                    + "    \"expenseControl\": {\n"
                    + "      \"status\": \"INACTIVE\",\n"
                    + "      \"links\": {\n"
                    + "        \"next\": {\n"
                    + "          \"method\": \"POST\",\n"
                    + "          \"uri\": \"/v5/expensecontrol/accounts\"\n"
                    + "        }\n"
                    + "      }\n"
                    + "    }\n"
                    + "  },\n"
                    + "  \"transactions\": [\n"
                    + "    {\n"
                    + "      \"date\": \"2020-01-05\",\n"
                    + "      \"description\": \"BYGGMAX/3456\",\n"
                    + "      \"expenseControlIncluded\": \"UNAVAILABLE\",\n"
                    + "      \"localAmount\": {\n"
                    + "        \"amount\": \"-1 555,55\",\n"
                    + "        \"currencyCode\": \"SEK\"\n"
                    + "      }\n"
                    + "    },\n"
                    + "    {\n"
                    + "      \"date\": \"2019-12-30\",\n"
                    + "      \"description\": \"BYGGMAX/2345\",\n"
                    + "      \"expenseControlIncluded\": \"UNAVAILABLE\",\n"
                    + "      \"localAmount\": {\n"
                    + "        \"amount\": \"-1 444,44\",\n"
                    + "        \"currencyCode\": \"SEK\"\n"
                    + "      }\n"
                    + "    },\n"
                    + "    {\n"
                    + "      \"date\": \"2019-12-27\",\n"
                    + "      \"description\": \"INBETALNING BANKGIRO\",\n"
                    + "      \"expenseControlIncluded\": \"UNAVAILABLE\",\n"
                    + "      \"localAmount\": {\n"
                    + "        \"amount\": \"3 000,00\",\n"
                    + "        \"currencyCode\": \"SEK\"\n"
                    + "      }\n"
                    + "    },\n"
                    + "    {\n"
                    + "      \"date\": \"2019-12-20\",\n"
                    + "      \"description\": \"ÅRSAVGIFT\",\n"
                    + "      \"expenseControlIncluded\": \"UNAVAILABLE\",\n"
                    + "      \"localAmount\": {\n"
                    + "        \"amount\": \"-295,00\",\n"
                    + "        \"currencyCode\": \"SEK\"\n"
                    + "      }\n"
                    + "    },\n"
                    + "    {\n"
                    + "      \"date\": \"2019-12-01\",\n"
                    + "      \"description\": \"STADSMISSIONENS REST.\",\n"
                    + "      \"expenseControlIncluded\": \"UNAVAILABLE\",\n"
                    + "      \"localAmount\": {\n"
                    + "        \"amount\": \"-3 333,00\",\n"
                    + "        \"currencyCode\": \"SEK\"\n"
                    + "      }\n"
                    + "    },\n"
                    + "    {\n"
                    + "      \"date\": \"2019-11-29\",\n"
                    + "      \"description\": \"INBETALNING BANKGIRO\",\n"
                    + "      \"expenseControlIncluded\": \"UNAVAILABLE\",\n"
                    + "      \"localAmount\": {\n"
                    + "        \"amount\": \"5 555,55\",\n"
                    + "        \"currencyCode\": \"SEK\"\n"
                    + "      }\n"
                    + "    },\n"
                    + "    {\n"
                    + "      \"date\": \"2019-11-12\",\n"
                    + "      \"description\": \"BONUSUTBETALNING\",\n"
                    + "      \"expenseControlIncluded\": \"UNAVAILABLE\",\n"
                    + "      \"localAmount\": {\n"
                    + "        \"amount\": \"100,00\",\n"
                    + "        \"currencyCode\": \"SEK\"\n"
                    + "      }\n"
                    + "    },\n"
                    + "    {\n"
                    + "      \"date\": \"2019-10-29\",\n"
                    + "      \"description\": \"INBETALNING BANKGIRO\",\n"
                    + "      \"expenseControlIncluded\": \"UNAVAILABLE\",\n"
                    + "      \"localAmount\": {\n"
                    + "        \"amount\": \"1 111,11\",\n"
                    + "        \"currencyCode\": \"SEK\"\n"
                    + "      }\n"
                    + "    },\n"
                    + "    {\n"
                    + "      \"date\": \"2019-10-23\",\n"
                    + "      \"description\": \"GE-KAS\",\n"
                    + "      \"expenseControlIncluded\": \"UNAVAILABLE\",\n"
                    + "      \"localAmount\": {\n"
                    + "        \"amount\": \"-2 525,25\",\n"
                    + "        \"currencyCode\": \"SEK\"\n"
                    + "      }\n"
                    + "    },\n"
                    + "    {\n"
                    + "      \"date\": \"2019-10-17\",\n"
                    + "      \"description\": \"VEXJO FERG & TAPETAFFE\",\n"
                    + "      \"expenseControlIncluded\": \"UNAVAILABLE\",\n"
                    + "      \"localAmount\": {\n"
                    + "        \"amount\": \"-2 666,00\",\n"
                    + "        \"currencyCode\": \"SEK\"\n"
                    + "      }\n"
                    + "    },\n"
                    + "    {\n"
                    + "      \"date\": \"2019-09-30\",\n"
                    + "      \"description\": \"INBETALNING BANKGIRO\",\n"
                    + "      \"expenseControlIncluded\": \"UNAVAILABLE\",\n"
                    + "      \"localAmount\": {\n"
                    + "        \"amount\": \"2 198,95\",\n"
                    + "        \"currencyCode\": \"SEK\"\n"
                    + "      }\n"
                    + "    },\n"
                    + "    {\n"
                    + "      \"date\": \"2019-09-07\",\n"
                    + "      \"description\": \"BUBBLEROOM.SE\",\n"
                    + "      \"expenseControlIncluded\": \"UNAVAILABLE\",\n"
                    + "      \"localAmount\": {\n"
                    + "        \"amount\": \"-1 080,00\",\n"
                    + "        \"currencyCode\": \"SEK\"\n"
                    + "      }\n"
                    + "    },\n"
                    + "    {\n"
                    + "      \"date\": \"2019-09-02\",\n"
                    + "      \"description\": \"B507 JEM & FIX LJUNGBY\",\n"
                    + "      \"expenseControlIncluded\": \"UNAVAILABLE\",\n"
                    + "      \"localAmount\": {\n"
                    + "        \"amount\": \"-1 298,95\",\n"
                    + "        \"currencyCode\": \"SEK\"\n"
                    + "      }\n"
                    + "    },\n"
                    + "    {\n"
                    + "      \"date\": \"2019-08-29\",\n"
                    + "      \"description\": \"EUROSKO LJUNGBY\",\n"
                    + "      \"expenseControlIncluded\": \"UNAVAILABLE\",\n"
                    + "      \"localAmount\": {\n"
                    + "        \"amount\": \"-900,00\",\n"
                    + "        \"currencyCode\": \"SEK\"\n"
                    + "      }\n"
                    + "    },\n"
                    + "    {\n"
                    + "      \"date\": \"2019-08-29\",\n"
                    + "      \"description\": \"INBETALNING BANKGIRO\",\n"
                    + "      \"expenseControlIncluded\": \"UNAVAILABLE\",\n"
                    + "      \"localAmount\": {\n"
                    + "        \"amount\": \"4,99\",\n"
                    + "        \"currencyCode\": \"SEK\"\n"
                    + "      }\n"
                    + "    },\n"
                    + "    {\n"
                    + "      \"date\": \"2019-08-07\",\n"
                    + "      \"description\": \"RÄNTA\",\n"
                    + "      \"expenseControlIncluded\": \"UNAVAILABLE\",\n"
                    + "      \"localAmount\": {\n"
                    + "        \"amount\": \"-4,99\",\n"
                    + "        \"currencyCode\": \"SEK\"\n"
                    + "      }\n"
                    + "    },\n"
                    + "    {\n"
                    + "      \"date\": \"2019-07-31\",\n"
                    + "      \"description\": \"INBETALNING BANKGIRO\",\n"
                    + "      \"expenseControlIncluded\": \"UNAVAILABLE\",\n"
                    + "      \"localAmount\": {\n"
                    + "        \"amount\": \"2 104,45\",\n"
                    + "        \"currencyCode\": \"SEK\"\n"
                    + "      }\n"
                    + "    },\n"
                    + "    {\n"
                    + "      \"date\": \"2019-07-15\",\n"
                    + "      \"description\": \"NETONNET AB\",\n"
                    + "      \"expenseControlIncluded\": \"UNAVAILABLE\",\n"
                    + "      \"localAmount\": {\n"
                    + "        \"amount\": \"-1 239,00\",\n"
                    + "        \"currencyCode\": \"SEK\"\n"
                    + "      }\n"
                    + "    },\n"
                    + "    {\n"
                    + "      \"date\": \"2019-07-08\",\n"
                    + "      \"description\": \"INBETALNING BANKGIRO\",\n"
                    + "      \"expenseControlIncluded\": \"UNAVAILABLE\",\n"
                    + "      \"localAmount\": {\n"
                    + "        \"amount\": \"8 257,19\",\n"
                    + "        \"currencyCode\": \"SEK\"\n"
                    + "      }\n"
                    + "    },\n"
                    + "    {\n"
                    + "      \"date\": \"2019-07-08\",\n"
                    + "      \"description\": \"KORR FÖRSENINGSAVGIFT\",\n"
                    + "      \"expenseControlIncluded\": \"UNAVAILABLE\",\n"
                    + "      \"localAmount\": {\n"
                    + "        \"amount\": \"100,00\",\n"
                    + "        \"currencyCode\": \"SEK\"\n"
                    + "      }\n"
                    + "    }\n"
                    + "  ],\n"
                    + "  \"reservedTransactions\": [\n"
                    + "\n"
                    + "  ],\n"
                    + "  \"moreTransactionsAvailable\": true,\n"
                    + "  \"links\": {\n"
                    + "    \"next\": {\n"
                    + "      \"method\": \"GET\",\n"
                    + "      \"uri\": \"/v5/engagement/cardaccount/0000000000000000000000000000000000000001?transactionsPerPage=20&page=2\"\n"
                    + "    }\n"
                    + "  }\n"
                    + "}\n";
}
