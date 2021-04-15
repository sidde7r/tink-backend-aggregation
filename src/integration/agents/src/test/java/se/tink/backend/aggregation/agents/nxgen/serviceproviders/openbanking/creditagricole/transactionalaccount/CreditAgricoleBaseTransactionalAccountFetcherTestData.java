package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount;

import org.junit.Ignore;

@Ignore
public class CreditAgricoleBaseTransactionalAccountFetcherTestData {

    static final String ACCOUNT_WITH_ALL_LINKS =
            fromTemplate(
                    "{\"href\": \"/accounts/123123123/transactions\",\"templated\": true\n}",
                    "{\"href\": \"/end-user-identity\",\"templated\": false}",
                    "{\"href\": \"/trusted-beneficiaries\",\"templated\": false}");

    static final String ACCOUNTS_WITHOUT_IDENTITY_LINK =
            fromTemplate(
                    "{\"href\": \"/accounts/123123123/transactions\",\"templated\": true\n}",
                    null,
                    "{\"href\": \"/trusted-beneficiaries\",\"templated\": false}");

    static final String ACCOUNT_WITHOUT_TRANSACTIONS_LINK =
            fromTemplate(
                    null,
                    "{\"href\": \"/end-user-identity\",\"templated\": false}",
                    "{\"href\": \"/trusted-beneficiaries\",\"templated\": false}");

    static final String ACCOUNTS_WITHOUT_LINKS = fromTemplate(null, null, null);

    private static final String ACCOUNTS_RESPONSE_TEMPLATE =
            "{\n"
                    + "    \"accounts\": [\n"
                    + "        {\n"
                    + "            \"resourceId\": \"123123123\",\n"
                    + "            \"bicFi\": null,\n"
                    + "            \"accountId\": {\n"
                    + "                \"iban\": \"FR12312312323123123\",\n"
                    + "                \"other\": null,\n"
                    + "                \"currency\": \"EUR\"\n"
                    + "            },\n"
                    + "            \"name\": \"MONSIEUR ALLO ALLO\",\n"
                    + "            \"details\": null,\n"
                    + "            \"linkedAccount\": null,\n"
                    + "            \"usage\": \"PRIV\",\n"
                    + "            \"cashAccountType\": \"CACC\",\n"
                    + "            \"product\": \"COMPTE EKO\",\n"
                    + "            \"balances\": [\n"
                    + "                {\n"
                    + "                    \"name\": \"Accounting Balance\",\n"
                    + "                    \"balanceAmount\": {\n"
                    + "                        \"currency\": \"EUR\",\n"
                    + "                        \"amount\": \"666.66\"\n"
                    + "                    },\n"
                    + "                    \"balanceType\": \"CLBD\",\n"
                    + "                    \"lastChangeDateTime\": null,\n"
                    + "                    \"referenceDate\": \"2020-01-29\",\n"
                    + "                    \"lastCommittedTransaction\": null\n"
                    + "                }\n"
                    + "            ],\n"
                    + "            \"psuStatus\": null,\n"
                    + "            \"_links\": {\n"
                    + "                \"balances\": {\n"
                    + "                    \"href\": \"/accounts/123123123/balances\",\n"
                    + "                    \"templated\": false\n"
                    + "                },\n"
                    + "                \"transactions\": %s\n"
                    + "            }\n"
                    + "        }\n"
                    + "    ],\n"
                    + "    \"_links\": {\n"
                    + "        \"self\": {\n"
                    + "            \"href\": \"/accounts\",\n"
                    + "            \"templated\": false\n"
                    + "        },\n"
                    + "        \"endUserIdentity\": %s,\n"
                    + "        \"beneficiaries\": %s,\n"
                    + "        \"first\": null,\n"
                    + "        \"last\": null,\n"
                    + "        \"next\": null,\n"
                    + "        \"prev\": null\n"
                    + "    }\n"
                    + "}";

    private static String fromTemplate(
            String transactionsValue, String endUserIdentityValue, String beneficiariesValue) {
        return String.format(
                ACCOUNTS_RESPONSE_TEMPLATE,
                transactionsValue,
                endUserIdentityValue,
                beneficiariesValue);
    }
}
