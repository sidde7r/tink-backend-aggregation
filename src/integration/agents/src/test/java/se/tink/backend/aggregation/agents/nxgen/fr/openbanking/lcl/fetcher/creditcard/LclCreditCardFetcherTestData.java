package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.fetcher.creditcard;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.account.AccountsResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.transaction.TransactionsResponseDto;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class LclCreditCardFetcherTestData {

    static final AccountsResponseDto ACCOUNTS_CARDS_RESPONSE =
            SerializationUtils.deserializeFromString(
                    "{\n"
                            + "    \"accounts\": [\n"
                            + "        {\n"
                            + "            \"resourceId\": \"122112121221\",\n"
                            + "            \"accountId\": {\n"
                            + "                \"other\": {\n"
                            + "                    \"identification\": \"12345\",\n"
                            + "                    \"schemeName\": \"CPAN\"\n"
                            + "                }\n"
                            + "            },\n"
                            + "            \"name\": \"CREDITE CARTE EL FRANCUSO\",\n"
                            + "            \"detail\": \"Carte le credite france origne\",\n"
                            + "            \"linkedAccount\": \"31232141\",\n"
                            + "            \"cashAccountType\": \"CARD\",\n"
                            + "            \"product\": \"CARTE VISA\",\n"
                            + "            \"balances\": [\n"
                            + "                {\n"
                            + "                    \"name\": \"Encours\",\n"
                            + "                    \"balanceAmount\": {\n"
                            + "                        \"amount\": \"100\",\n"
                            + "                        \"currency\": \"EUR\"\n"
                            + "                    },\n"
                            + "                    \"balanceType\": \"OTHR\"\n"
                            + "                }\n"
                            + "            ],\n"
                            + "            \"_links\": {\n"
                            + "                \"balances\": {\n"
                            + "                    \"href\": \"HREF\"\n"
                            + "                },\n"
                            + "                \"transactions\": {\n"
                            + "                    \"href\": \"HREF1\"\n"
                            + "                }\n"
                            + "            }\n"
                            + "        }\n"
                            + "    ],\n"
                            + "    \"_links\": {\n"
                            + "        \"self\": {\n"
                            + "            \"href\": \"HREF2\"\n"
                            + "        },\n"
                            + "        \"endUserIdentity\": {\n"
                            + "            \"href\": \"HREF3\"\n"
                            + "        },\n"
                            + "        \"beneficiaries\": {\n"
                            + "            \"href\": \"HREF4\"\n"
                            + "        }\n"
                            + "    }\n"
                            + "}",
                    AccountsResponseDto.class);

    static final TransactionsResponseDto CREDIT_CARD_TRANSACTIONS =
            SerializationUtils.deserializeFromString(
                    "{\n"
                            + "  \"transactions\": [\n"
                            + "    {\n"
                            + "      \"resourceId\": \"resourceId1\",\n"
                            + "      \"entryReference\": \"entryReference1\",\n"
                            + "      \"transactionAmount\": {\n"
                            + "        \"amount\": \"-139\",\n"
                            + "        \"currency\": \"EUR\"\n"
                            + "      },\n"
                            + "      \"creditDebitIndicator\": \"DBIT\",\n"
                            + "      \"status\": \"OTHR\",\n"
                            + "      \"transactionDate\": \"2021-05-29\",\n"
                            + "      \"expectingBookingDate\": \"2021-06-30\",\n"
                            + "      \"expectedBookingDate\": \"2021-06-30\",\n"
                            + "      \"remittanceInformation\": {\n"
                            + "        \"unstructured\": [\n"
                            + "          \"DESCRIPTION1\"\n"
                            + "        ]\n"
                            + "      }\n"
                            + "    },\n"
                            + "    {\n"
                            + "      \"resourceId\": \"resourceId2\",\n"
                            + "      \"entryReference\": \"entryReference2\",\n"
                            + "      \"transactionAmount\": {\n"
                            + "        \"amount\": \"-60\",\n"
                            + "        \"currency\": \"EUR\"\n"
                            + "      },\n"
                            + "      \"creditDebitIndicator\": \"DBIT\",\n"
                            + "      \"status\": \"BOOK\",\n"
                            + "      \"bookingDate\": \"2021-05-31\",\n"
                            + "      \"transactionDate\": \"2021-05-25\",\n"
                            + "      \"remittanceInformation\": {\n"
                            + "        \"unstructured\": [\n"
                            + "          \"DESCRIPTION2\"\n"
                            + "        ]\n"
                            + "      }\n"
                            + "    }\n"
                            + "  ],\n"
                            + "  \"_links\": {\n"
                            + "    \"self\": {\n"
                            + "      \"href\": \"https://psd.lcl.fr/aisp/accounts/accountId/transactions?page=1\"\n"
                            + "    },\n"
                            + "    \"balances\": {\n"
                            + "      \"href\": \"https://psd.lcl.fr/aisp/accounts/accountId/balances\"\n"
                            + "    },\n"
                            + "    \"parent-list\": {\n"
                            + "      \"href\": \"https://psd.lcl.fr/aisp/accounts\"\n"
                            + "    }\n"
                            + "  }\n"
                            + "}",
                    TransactionsResponseDto.class);
}
