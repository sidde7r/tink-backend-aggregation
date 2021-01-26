package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.card;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class LaBanquePostaleCardFetcherTestData {

    public static final AccountResponse ACCOUNTS_RESPONSE_WITH_BALANCE =
            SerializationUtils.deserializeFromString(
                    "{\n"
                            + "    \"_links\": {\n"
                            + "        \"self\": {\n"
                            + "            \"href\": \"/accounts\"\n"
                            + "        }\n"
                            + "    },\n"
                            + "    \"accounts\": [\n"
                            + "        {\n"
                            + "            \"_links\": {\n"
                            + "                \"balances\": {\n"
                            + "                    \"href\": \"/accounts/xxx/balances\"\n"
                            + "                },\n"
                            + "                \"transactions\": {\n"
                            + "                    \"href\": \"*/accounts/xxx/transactions\"\n"
                            + "                }\n"
                            + "            },\n"
                            + "            \"resourceId\": \"123123123\",\n"
                            + "            \"accountId\": {\n"
                            + "                \"iban\": \"FR3213123123\",\n"
                            + "                \"currency\": \"EUR\"\n"
                            + "            },\n"
                            + "            \"balances\": [\n"
                            + "                {\n"
                            + "                    \"balanceAmount\": {\n"
                            + "                        \"amount\": \"821.25\",\n"
                            + "                        \"currency\": \"EUR\"\n"
                            + "                    },\n"
                            + "                    \"balanceType\": \"XPCD\",\n"
                            + "                    \"name\": \"Money on card\"\n"
                            + "                }\n"
                            + "            ],\n"
                            + "            \"_links\": {\n"
                            + "                \"self\": {\n"
                            + "                    \"href\": \"/accounts/xxx/balances\"\n"
                            + "                },\n"
                            + "                \"transactions\": {\n"
                            + "                    \"href\": \"/accounts/xxx/transactions\"\n"
                            + "                },\n"
                            + "                \"parent-list\": {\n"
                            + "                    \"href\": \"/accounts\"\n"
                            + "                }\n"
                            + "            },\n"
                            + "            \"name\": \"visa123\",\n"
                            + "            \"usage\": \"PRIV\",\n"
                            + "            \"cashAccountType\": \"CARD\",\n"
                            + "            \"linkedAccount\": \"321321321312\"\n"
                            + "        }\n"
                            + "    ]\n"
                            + "}",
                    AccountResponse.class);
}
