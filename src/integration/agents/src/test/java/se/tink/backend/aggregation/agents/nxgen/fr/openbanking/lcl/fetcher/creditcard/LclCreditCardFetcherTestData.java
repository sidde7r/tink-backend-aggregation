package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.fetcher.creditcard;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.account.AccountsResponseDto;
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
}
