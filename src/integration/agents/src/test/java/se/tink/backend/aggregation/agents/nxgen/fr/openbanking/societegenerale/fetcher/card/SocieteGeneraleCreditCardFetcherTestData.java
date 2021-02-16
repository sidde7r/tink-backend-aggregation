package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.card;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class SocieteGeneraleCreditCardFetcherTestData {

    static final AccountsResponse CARD_RESPONSE =
            SerializationUtils.deserializeFromString(
                    "{\n"
                            + "    \"accounts\": [\n"
                            + "        {\n"
                            + "            \"resourceId\": \"2314311342234234243\",\n"
                            + "            \"accountId\": {\n"
                            + "                \"other\": {\n"
                            + "                    \"identification\": \"213123213123\",\n"
                            + "                    \"schemeName\": \"CPAN\"\n"
                            + "                }\n"
                            + "            },\n"
                            + "            \"name\": \"CB Mastercard\",\n"
                            + "            \"details\": \"Carte visa\",\n"
                            + "            \"linkedAccount\": \"9832749872938943875938\",\n"
                            + "            \"usage\": \"PRIV\",\n"
                            + "            \"cashAccountType\": \"CARD\",\n"
                            + "            \"balances\": [\n"
                            + "            ],\n"
                            + "            \"psuStatus\": \"Account Holder\",\n"
                            + "            \"_links\": {\n"
                            + "                \"balances\": {\n"
                            + "                    \"href\": \"/accounts/xxx/balances\"\n"
                            + "                }\n"
                            + "            }\n"
                            + "        }\n"
                            + "    ],\n"
                            + "    \"_links\": {\n"
                            + "        \"self\": {\n"
                            + "            \"href\": \"/accounts\"\n"
                            + "        },\n"
                            + "        \"endUserIdentity\": {\n"
                            + "            \"href\": \"/end-user-identity\"\n"
                            + "        },\n"
                            + "        \"beneficiaries\": {\n"
                            + "            \"href\": \"/trusted-beneficiaries\"\n"
                            + "        }\n"
                            + "    }\n"
                            + "}",
                    AccountsResponse.class);
}
