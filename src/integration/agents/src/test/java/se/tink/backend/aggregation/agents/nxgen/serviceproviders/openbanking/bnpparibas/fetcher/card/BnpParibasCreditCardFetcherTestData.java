package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.card;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.rpc.BalanceResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class BnpParibasCreditCardFetcherTestData {

    static final AccountsResponse CREDIT_CARDS_RESPONSE =
            SerializationUtils.deserializeFromString(
                    "{\n"
                            + "    \"accounts\": [\n"
                            + "        {\n"
                            + "            \"resourceId\": \"31231231dqeqweqw312\",\n"
                            + "            \"accountId\": {\n"
                            + "                \"iban\": \"FR3213123131231\",\n"
                            + "                \"currency\": \"EUR\"\n"
                            + "            },\n"
                            + "            \"name\": \"card visa\",\n"
                            + "            \"linkedAccount\": \"FR333123311\",\n"
                            + "            \"details\": \"Bnp Paribas\",\n"
                            + "            \"usage\": \"PRIV\",\n"
                            + "            \"cashAccountType\": \"CARD\",\n"
                            + "            \"product\": \"visa123\",\n"
                            + "            \"psuStatus\": \"Account Holder\",\n"
                            + "            \"_links\": {\n"
                            + "                \"balances\": {\n"
                            + "                    \"href\": \"/accounts/xxx/balances\"\n"
                            + "                },\n"
                            + "                \"transactions\": {\n"
                            + "                    \"href\": \"/accounts/xxx/transactions\"\n"
                            + "                }\n"
                            + "            }\n"
                            + "        }\n"
                            + "    ],\n"
                            + "    \"_links\": {\n"
                            + "        \"self\": {\n"
                            + "            \"href\": \"/accounts\"\n"
                            + "        }\n"
                            + "    }\n"
                            + "}",
                    AccountsResponse.class);

    static final BalanceResponse BALANCE_RESPONSE =
            SerializationUtils.deserializeFromString(
                    "{\n"
                            + "    \"balances\": [\n"
                            + "        {\n"
                            + "            \"name\": \"SOLDE PREVISIONNEL au 29/01/2021\",\n"
                            + "            \"referenceDate\": \"2021-01-29\",\n"
                            + "            \"balanceAmount\": {\n"
                            + "                \"amount\": \"3589.30\",\n"
                            + "                \"currency\": \"EUR\"\n"
                            + "            },\n"
                            + "            \"balanceType\": \"OTHR\"\n"
                            + "        }\n"
                            + "    ],\n"
                            + "    \"_links\": {\n"
                            + "        \"self\": {\n"
                            + "            \"href\": \"/accounts/xxx/balances\"\n"
                            + "        }\n"
                            + "    }\n"
                            + "}",
                    BalanceResponse.class);
}
