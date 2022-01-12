package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.fetcher;

import static se.tink.libraries.serialization.utils.SerializationUtils.*;

import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.authenticator.ArkeaTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.dto.ArkeaAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.dto.ArkeaBalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.dto.ArkeaEndUserIdentityResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.dto.ArkeaTransactionResponse;

public class ArkeaFetcherFixtures {
    public static final ArkeaTokenResponse TOKEN_RESPONSE =
            deserializeFromString(
                    "{\"token_type\": \"Bearer\","
                            + "\"access_token\": \"accessToken\","
                            + "\"expires_in\": 3599,"
                            + "\"refresh_token\": \"refreshToken\"}",
                    ArkeaTokenResponse.class);

    public static final ArkeaAccountResponse TRANSACTIONAL_ACCOUNTS_RESPONSE =
            deserializeFromString(
                    "{\n"
                            + "  \"accounts\": [\n"
                            + "    {\n"
                            + "      \"resourceId\": \"ALIAS!\",\n"
                            + "      \"bicFi\": \"BNKAFRPPXXX\",\n"
                            + "      \"accountId\": {\n"
                            + "        \"iban\": \"FR1212341234123412\",\n"
                            + "        \"currency\": \"EUR\"\n"
                            + "      },\n"
                            + "      \"name\": \"Compte de Mr et Mme Dupont\",\n"
                            + "      \"usage\": \"PRIV\",\n"
                            + "      \"cashAccountType\": \"CACC\",\n"
                            + "      \"currency\": \"EUR\",\n"
                            + "      \"psuStatus\": \"Co-account Holder\",\n"
                            + "      \"_links\": {\n"
                            + "        \"balances\": {\n"
                            + "          \"href\": \"v1/accounts/Alias1/balances\"\n"
                            + "        },\n"
                            + "        \"transactions\": {\n"
                            + "          \"href\": \"v1/accounts/Alias1/transactions\"\n"
                            + "        }\n"
                            + "      }\n"
                            + "    }\n"
                            + "  ],\n"
                            + "  \"_links\": {\n"
                            + "    \"self\": {\n"
                            + "      \"href\": \"v1/accounts?page=2\"\n"
                            + "    },\n"
                            + "    \"first\": {\n"
                            + "      \"href\": \"v1/accounts\"\n"
                            + "    },\n"
                            + "    \"last\": {\n"
                            + "      \"href\": \"v1/accounts?page=last\",\n"
                            + "      \"templated\": true\n"
                            + "    },\n"
                            + "    \"next\": {\n"
                            + "      \"href\": \"v1/accounts?page=3\",\n"
                            + "      \"templated\": true\n"
                            + "    },\n"
                            + "    \"prev\": {\n"
                            + "      \"href\": \"v1/accounts\",\n"
                            + "      \"templated\": true\n"
                            + "    }\n"
                            + "  }\n"
                            + "}",
                    ArkeaAccountResponse.class);

    public static final ArkeaAccountResponse NO_TRANSACTIONAL_ACCOUNTS_RESPONSE =
            deserializeFromString(
                    "{\n"
                            + "  \"accounts\": [],\n"
                            + "  \"_links\": {\n"
                            + "    \"self\": {\n"
                            + "      \"href\": \"v1/accounts?page=2\"\n"
                            + "    },\n"
                            + "    \"first\": {\n"
                            + "      \"href\": \"v1/accounts\"\n"
                            + "    },\n"
                            + "    \"last\": {\n"
                            + "      \"href\": \"v1/accounts?page=last\",\n"
                            + "      \"templated\": true\n"
                            + "    },\n"
                            + "    \"next\": {\n"
                            + "      \"href\": \"v1/accounts?page=3\",\n"
                            + "      \"templated\": true\n"
                            + "    },\n"
                            + "    \"prev\": {\n"
                            + "      \"href\": \"v1/accounts\",\n"
                            + "      \"templated\": true\n"
                            + "    }\n"
                            + "  }\n"
                            + "}",
                    ArkeaAccountResponse.class);

    public static final ArkeaBalanceResponse BALANCE_RESPONSE =
            deserializeFromString(
                    "{\n"
                            + "  \"balances\": [\n"
                            + "    {\n"
                            + "      \"name\": \"Solde comptable au 12/01/2017\",\n"
                            + "      \"balanceAmount\": {\n"
                            + "        \"currency\": \"EUR\",\n"
                            + "        \"amount\": \"123.45\"\n"
                            + "      },\n"
                            + "      \"balanceType\": \"CLBD\",\n"
                            + "      \"lastCommittedTransaction\": \"A452CH\"\n"
                            + "    }\n"
                            + "  ],\n"
                            + "  \"_links\": {\n"
                            + "    \"self\": {\n"
                            + "      \"href\": \"v1/accounts/Alias1/balances-report\"\n"
                            + "    },\n"
                            + "    \"parent-list\": {\n"
                            + "      \"href\": \"v1/accounts\"\n"
                            + "    },\n"
                            + "    \"transactions\": {\n"
                            + "      \"href\": \"v1/accounts/Alias1/transactions\"\n"
                            + "    }\n"
                            + "  }\n"
                            + "}",
                    ArkeaBalanceResponse.class);

    public static final ArkeaBalanceResponse NO_BALANCE_RESPONSE =
            deserializeFromString(
                    "{\n"
                            + "  \"balances\": [],\n"
                            + "  \"_links\": {\n"
                            + "    \"self\": {\n"
                            + "      \"href\": \"v1/accounts/Alias1/balances-report\"\n"
                            + "    },\n"
                            + "    \"parent-list\": {\n"
                            + "      \"href\": \"v1/accounts\"\n"
                            + "    },\n"
                            + "    \"transactions\": {\n"
                            + "      \"href\": \"v1/accounts/Alias1/transactions\"\n"
                            + "    }\n"
                            + "  }\n"
                            + "}",
                    ArkeaBalanceResponse.class);

    public static final ArkeaTransactionResponse
            TRANSACTION_RESPONSE_WITH_UNSTRUCTURED_REMITTANCE_INFORMATION =
                    deserializeFromString(
                            "{\n"
                                    + "  \"transactions\": [\n"
                                    + "    {\n"
                                    + "      \"entryReference\": \"AF5T2\",\n"
                                    + "      \"transactionAmount\": {\n"
                                    + "        \"currency\": \"EUR\",\n"
                                    + "        \"amount\": \"12.25\"\n"
                                    + "      },\n"
                                    + "      \"creditDebitIndicator\": \"CRDT\",\n"
                                    + "      \"status\": \"BOOK\",\n"
                                    + "      \"bookingDate\": \"2018-02-12\",\n"
                                    + "      \"remittanceInformation\": {"
                                    + "      \"unstructured\": [\n"
                                    + "        \"SEPA CREDIT TRANSFER from PSD2Company\"\n"
                                    + "      ]\n"
                                    + "    }\n"
                                    + "  ],\n"
                                    + "  \"_links\": {\n"
                                    + "    \"self\": {\n"
                                    + "      \"href\": \"v1/accounts/Alias1/transactions\"\n"
                                    + "    },\n"
                                    + "    \"parent-list\": {\n"
                                    + "      \"href\": \"v1/accounts\"\n"
                                    + "    },\n"
                                    + "    \"balances\": {\n"
                                    + "      \"href\": \"v1/accounts/Alias1/balances\"\n"
                                    + "    },\n"
                                    + "    \"last\": {\n"
                                    + "      \"href\": \"v1/accounts/sAlias1/transactions?page=last\"\n"
                                    + "    },\n"
                                    + "    \"next\": {\n"
                                    + "      \"href\": \"v1/accounts/Alias1/transactions?page=3\"\n"
                                    + "    }\n"
                                    + "  }\n"
                                    + "}",
                            ArkeaTransactionResponse.class);

    public static final ArkeaEndUserIdentityResponse END_USER_IDENTITY_RESPONSE =
            deserializeFromString(
                    "{\n"
                            + "  \"connectedPsu\": \"IMeMyself\",\n"
                            + "  \"_links\": {\n"
                            + "    \"self\": {\n"
                            + "      \"href\": \"v1/end-user-identity\"\n"
                            + "    },\n"
                            + "    \"parent-list\": {\n"
                            + "      \"href\": \"v1/accounts\"\n"
                            + "    }\n"
                            + "  }\n"
                            + "}",
                    ArkeaEndUserIdentityResponse.class);
}
