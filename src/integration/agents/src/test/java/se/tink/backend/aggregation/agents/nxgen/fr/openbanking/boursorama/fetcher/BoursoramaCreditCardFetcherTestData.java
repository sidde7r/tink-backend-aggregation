package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.fetcher;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity.BalanceResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class BoursoramaCreditCardFetcherTestData {

    static final AccountsResponse CARD_ACC_RESPONSE =
            SerializationUtils.deserializeFromString(
                    "{\n"
                            + "    \"accounts\": [\n"
                            + "        {\n"
                            + "            \"resourceId\": \"DCF27527D5243CD68D0FDF644744163E\",\n"
                            + "            \"accountId\":{\"iban\":\"FR21312313123123\",\"currency\":\"EUR\"},\n"
                            + "            \"name\": \"Visa classique MLE Bli Bla Blo\",\n"
                            + "            \"details\": \"immediate_debit\",\n"
                            + "            \"linkedAccount\": \"3B9F0FCF487ECF9FCDC4CBAFDD0A2E6D\",\n"
                            + "            \"usage\": \"PRIV\",\n"
                            + "            \"cashAccountType\": \"CARD\",\n"
                            + "            \"product\": \"CBI2WIM\",\n"
                            + "            \"psuStatus\": \"Account holder\",\n"
                            + "            \"_links\": {\n"
                            + "                \"endUserIdentity\": {\n"
                            + "                    \"href\": \"/services/api/v1.7/_user_/_70352f41061eda4_/dsp2/users/identity\",\n"
                            + "                    \"method\": \"GET\"\n"
                            + "                },\n"
                            + "                \"balances\": {\n"
                            + "                    \"href\": \"/services/api/v1.7/_user_/_70352f41061eda4_/dsp2/accounts/balances/DCF27527D5243CD68D0FDF644744163E\",\n"
                            + "                    \"method\": \"GET\"\n"
                            + "                },\n"
                            + "                \"transactions\": {\n"
                            + "                    \"href\": \"/services/api/v1.7/_user_/_70352f41061eda4_/dsp2/accounts/transactions/DCF27527D5243CD68D0FDF644744163E\",\n"
                            + "                    \"method\": \"GET\"\n"
                            + "                }\n"
                            + "            }\n"
                            + "        }\n"
                            + "    ],\n"
                            + "    \"_links\": {\n"
                            + "        \"self\": {\n"
                            + "            \"href\": \"/services/api/v1.7/_user_/_70352f41061eda4_/dsp2/accounts\",\n"
                            + "            \"method\": \"GET\"\n"
                            + "        }\n"
                            + "    }\n"
                            + "}",
                    AccountsResponse.class);

    static final BalanceResponse CARD_BALANCE_RES =
            SerializationUtils.deserializeFromString(
                    "{\n"
                            + "    \"balances\": [\n"
                            + "        {\n"
                            + "            \"name\": \"Solde en valeur\",\n"
                            + "            \"balanceAmount\": {\n"
                            + "                \"currency\": \"EUR\",\n"
                            + "                \"amount\": \"1642.68\"\n"
                            + "            },\n"
                            + "            \"balanceType\": \"OTHR\",\n"
                            + "            \"lastCommittedTransaction\": \"2018-07-02\"\n"
                            + "        }\n"
                            + "    ],\n"
                            + "    \"_links\": {\n"
                            + "        \"self\": {\n"
                            + "            \"href\": \"/services/api/v1.7/_user_/_***MASKED***_/dsp2/accounts/balances/3B9F0FCF487ECF9FCDC4CBAFDD0A2E6D\",\n"
                            + "            \"method\": \"GET\"\n"
                            + "        },\n"
                            + "        \"parent-list\": {\n"
                            + "            \"href\": \"/services/api/v1.7/_user_/_***MASKED***_/dsp2/accounts\",\n"
                            + "            \"method\": \"GET\"\n"
                            + "        },\n"
                            + "        \"transactions\": {\n"
                            + "            \"href\": \"/services/api/v1.7/_user_/_***MASKED***_/dsp2/accounts/transactions/3B9F0FCF487ECF9FCDC4CBAFDD0A2E6D\",\n"
                            + "            \"method\": \"GET\"\n"
                            + "        }\n"
                            + "    }\n"
                            + "}",
                    BalanceResponse.class);
}
