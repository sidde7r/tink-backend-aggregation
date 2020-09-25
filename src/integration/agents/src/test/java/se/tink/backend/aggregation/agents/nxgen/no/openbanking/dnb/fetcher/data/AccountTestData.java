package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data;

import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.rpc.BalancesResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class AccountTestData {
    public static AccountsResponse getAccountsResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"accounts\": [\n"
                        + "        {\n"
                        + "            \"iban\": \"NO6012045357110\",\n"
                        + "            \"bban\": \"12045357110\",\n"
                        + "            \"currency\": \"NOK\",\n"
                        + "            \"name\": \"Sparekonto\",\n"
                        + "            \"_links\": {\n"
                        + "                \"balances\": {\n"
                        + "                    \"href\": \"/v1/accounts/12045357110/balances\"\n"
                        + "                },\n"
                        + "                \"transactions\": {\n"
                        + "                    \"href\": \"/v1/accounts/12045357110/transactions\"\n"
                        + "                }\n"
                        + "            }\n"
                        + "        },\n"
                        + "        {\n"
                        + "            \"iban\": \"NO6012045357110\",\n"
                        + "            \"bban\": \"12045357110\",\n"
                        + "            \"currency\": \"NOK\",\n"
                        + "            \"name\": \"PlasSEringsKOnto\",\n"
                        + "            \"_links\": {\n"
                        + "                \"balances\": {\n"
                        + "                    \"href\": \"/v1/accounts/12045357110/balances\"\n"
                        + "                },\n"
                        + "                \"transactions\": {\n"
                        + "                    \"href\": \"/v1/accounts/12045357110/transactions\"\n"
                        + "                }\n"
                        + "            }\n"
                        + "        },\n"
                        + "        {\n"
                        + "            \"iban\": \"NO6912050150591\",\n"
                        + "            \"bban\": \"12050150591\",\n"
                        + "            \"currency\": \"NOK\",\n"
                        + "            \"name\": \"Brukskonto\",\n"
                        + "            \"_links\": {\n"
                        + "                \"balances\": {\n"
                        + "                    \"href\": \"/v1/accounts/12050150591/balances\"\n"
                        + "                },\n"
                        + "                \"transactions\": {\n"
                        + "                    \"href\": \"/v1/accounts/12050150591/transactions\"\n"
                        + "                }\n"
                        + "            }\n"
                        + "        },\n"
                        + "        {\n"
                        + "            \"iban\": \"NO6912050150591\",\n"
                        + "            \"bban\": \"12050150591\",\n"
                        + "            \"currency\": \"NOK\",\n"
                        + "            \"name\": \"Superspar 123\",\n"
                        + "            \"_links\": {\n"
                        + "                \"balances\": {\n"
                        + "                    \"href\": \"/v1/accounts/12050150591/balances\"\n"
                        + "                },\n"
                        + "                \"transactions\": {\n"
                        + "                    \"href\": \"/v1/accounts/12050150591/transactions\"\n"
                        + "                }\n"
                        + "            }\n"
                        + "        },\n"
                        + "        {\n"
                        + "            \"iban\": \"NO6912050150591\",\n"
                        + "            \"bban\": \"12050150591\",\n"
                        + "            \"currency\": \"NOK\",\n"
                        + "            \"name\": \"My Little Savings Account\",\n"
                        + "            \"_links\": {\n"
                        + "                \"balances\": {\n"
                        + "                    \"href\": \"/v1/accounts/12050150591/balances\"\n"
                        + "                },\n"
                        + "                \"transactions\": {\n"
                        + "                    \"href\": \"/v1/accounts/12050150591/transactions\"\n"
                        + "                }\n"
                        + "            }\n"
                        + "        }\n"
                        + "    ]\n"
                        + "}",
                AccountsResponse.class);
    }

    public static BalancesResponse getBalancesResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"account\": {\n"
                        + "        \"bban\": \"12045357110\",\n"
                        + "        \"currency\": \"NOK\"\n"
                        + "    },\n"
                        + "    \"balances\": [\n"
                        + "        {\n"
                        + "            \"balanceAmount\": {\n"
                        + "                \"currency\": \"NOK\",\n"
                        + "                \"amount\": \"63257.00\"\n"
                        + "            },\n"
                        + "            \"balanceType\": \"openingBooked\"\n"
                        + "        },\n"
                        + "        {\n"
                        + "            \"balanceAmount\": {\n"
                        + "                \"currency\": \"NOK\",\n"
                        + "                \"amount\": \"63257.00\"\n"
                        + "            },\n"
                        + "            \"balanceType\": \"authorised\"\n"
                        + "        },\n"
                        + "        {\n"
                        + "            \"balanceAmount\": {\n"
                        + "                \"currency\": \"NOK\",\n"
                        + "                \"amount\": \"63257.00\"\n"
                        + "            },\n"
                        + "            \"balanceType\": \"expected\"\n"
                        + "        }\n"
                        + "    ]\n"
                        + "}",
                BalancesResponse.class);
    }
}
