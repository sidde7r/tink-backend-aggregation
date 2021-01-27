package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.creditcard;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.rpc.FetchAccountsResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class CmcicCreditCardFetcherTestData {

    static final FetchAccountsResponse CREDIT_CARDS_ACCOUNT_RESPONSE =
            SerializationUtils.deserializeFromString(
                    "{\n"
                            + "    \"accounts\": [\n"
                            + "        {\n"
                            + "            \"cashAccountType\": \"CARD\",\n"
                            + "            \"resourceId\": \"987398127398\",\n"
                            + "            \"accountId\": {\n"
                            + "                \"iban\": \"FR1231872361\",\n"
                            + "                \"currency\": \"EUR\"\n"
                            + "            },\n"
                            + "            \"name\": \"Francua carte le credite oui\",\n"
                            + "            \"linkedAccount\": \"13242342342\",\n"
                            + "            \"currency\": \"EUR\",\n"
                            + "            \"balances\": [\n"
                            + "                {\n"
                            + "                    \"name\": \"Card purchases from 0 to 1\",\n"
                            + "                    \"balanceAmount\": {\n"
                            + "                        \"currency\": \"EUR\",\n"
                            + "                        \"amount\": \"231.07\"\n"
                            + "                    },\n"
                            + "                    \"referenceDate\": \"2020-12-31\",\n"
                            + "                    \"balanceType\": \"OTHR\"\n"
                            + "                },\n"
                            + "                {\n"
                            + "                    \"name\": \"Card purchases from 1 to 2\",\n"
                            + "                    \"balanceAmount\": {\n"
                            + "                        \"currency\": \"EUR\",\n"
                            + "                        \"amount\": \"234.07\"\n"
                            + "                    },\n"
                            + "                    \"referenceDate\": \"2021-02-01\",\n"
                            + "                    \"balanceType\": \"OTHR\"\n"
                            + "                }\n"
                            + "            ],\n"
                            + "            \"_links\": {\n"
                            + "                \"balances\": {\n"
                            + "                    \"href\": \"balancesHref\"\n"
                            + "                },\n"
                            + "                \"transactions\": {\n"
                            + "                    \"href\": \"transactionsHref\"\n"
                            + "                }\n"
                            + "            }\n"
                            + "        }\n"
                            + "    ],\n"
                            + "    \"_links\": {\n"
                            + "        \"self\": {\n"
                            + "            \"href\": \"linksHref1\"\n"
                            + "        }\n"
                            + "    }\n"
                            + "}",
                    FetchAccountsResponse.class);
}
