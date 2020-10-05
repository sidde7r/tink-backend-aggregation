package se.tink.backend.aggregation.agents.nxgen.it.bancoposta.fetcher;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.rpc.SavingAccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.rpc.SavingAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.rpc.SavingTransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.rpc.TransactionsResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class FetcherTestData {
    public static AccountsResponse getAccountResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"data\": [\n"
                        + "        {\n"
                        + "            \"id\": 841243,\n"
                        + "            \"accountIdentifier\": \"001043581196\",\n"
                        + "            \"realmIdentifier\": \"posteitaliane\",\n"
                        + "            \"accountTypeId\": 45,\n"
                        + "            \"name\": \"CONTO\",\n"
                        + "            \"balance\": 0.0000,\n"
                        + "            \"limit\": 0.0000,\n"
                        + "            \"accountClass\": null,\n"
                        + "            \"organizationName\": null,\n"
                        + "            \"organizationIdentifier\": \"posteitaliane\",\n"
                        + "            \"realmCredentialsId\": 348385,\n"
                        + "            \"accountAuthorizationType\": \"External\",\n"
                        + "            \"orderId\": 0,\n"
                        + "            \"isImportAccount\": false,\n"
                        + "            \"lastUpdate\": \"2019-01-11T21:23:31.7332123\",\n"
                        + "            \"personId\": 20348385,\n"
                        + "            \"userEmail\": \"\",\n"
                        + "            \"createDate\": \"2018-12-06T13:19:18.4503954\",\n"
                        + "            \"accountCategory\": \"Current\",\n"
                        + "            \"emergencyFundBalanceLimit\": null,\n"
                        + "            \"inactive\": false,\n"
                        + "            \"attachedToUserDate\": null,\n"
                        + "            \"isHidden\": false,\n"
                        + "            \"isDisabled\": false,\n"
                        + "            \"metadata\": [\n"
                        + "                {\n"
                        + "                    \"name\": \"DTPFM\",\n"
                        + "                    \"value\": \"13-11-2018\"\n"
                        + "                },\n"
                        + "                {\n"
                        + "                    \"name\": \"DTPRIMAMOV\",\n"
                        + "                    \"value\": \"18-09-2018\"\n"
                        + "                },\n"
                        + "                {\n"
                        + "                    \"name\": \"FLGSTO\",\n"
                        + "                    \"value\": \"S\"\n"
                        + "                }\n"
                        + "            ],\n"
                        + "            \"currencyCode\": \"EUR\",\n"
                        + "            \"accountRoleExternal\": \"MONOINTESTATO\",\n"
                        + "            \"accountStatusExternal\": \"APERTO\",\n"
                        + "            \"balanceInUserCurrency\": 0.0,\n"
                        + "            \"limitInUserCurrency\": 0.0\n"
                        + "        },\n"
                        + "        {\n"
                        + "            \"id\": 841243,\n"
                        + "            \"accountIdentifier\": \"001043581196\",\n"
                        + "            \"realmIdentifier\": \"posteitaliane\",\n"
                        + "            \"accountTypeId\": 45,\n"
                        + "            \"name\": \"CONTO\",\n"
                        + "            \"balance\": 0.0000,\n"
                        + "            \"limit\": 0.0000,\n"
                        + "            \"accountClass\": null,\n"
                        + "            \"organizationName\": null,\n"
                        + "            \"organizationIdentifier\": \"posteitaliane\",\n"
                        + "            \"realmCredentialsId\": 348385,\n"
                        + "            \"accountAuthorizationType\": \"External\",\n"
                        + "            \"orderId\": 0,\n"
                        + "            \"isImportAccount\": false,\n"
                        + "            \"lastUpdate\": \"2019-01-11T21:23:31.7332123\",\n"
                        + "            \"personId\": 20348385,\n"
                        + "            \"userEmail\": \"\",\n"
                        + "            \"createDate\": \"2018-12-06T13:19:18.4503954\",\n"
                        + "            \"accountCategory\": \"Current\",\n"
                        + "            \"emergencyFundBalanceLimit\": null,\n"
                        + "            \"inactive\": false,\n"
                        + "            \"attachedToUserDate\": null,\n"
                        + "            \"isHidden\": false,\n"
                        + "            \"isDisabled\": false,\n"
                        + "            \"metadata\": [\n"
                        + "                {\n"
                        + "                    \"name\": \"DTPFM\",\n"
                        + "                    \"value\": \"13-11-2018\"\n"
                        + "                },\n"
                        + "                {\n"
                        + "                    \"name\": \"DTPRIMAMOV\",\n"
                        + "                    \"value\": \"18-09-2018\"\n"
                        + "                },\n"
                        + "                {\n"
                        + "                    \"name\": \"FLGSTO\",\n"
                        + "                    \"value\": \"S\"\n"
                        + "                }\n"
                        + "            ],\n"
                        + "            \"currencyCode\": \"EUR\",\n"
                        + "            \"accountRoleExternal\": \"MONOINTESTATO\",\n"
                        + "            \"accountStatusExternal\": \"APERTO\",\n"
                        + "            \"balanceInUserCurrency\": 0.0,\n"
                        + "            \"limitInUserCurrency\": 0.0\n"
                        + "        }\n"
                        + "    ]\n"
                        + "}",
                AccountsResponse.class);
    }

    public static AccountDetailsResponse getAccountDetailsResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"header\": {\n"
                        + "    \"command-result\": \"0\",\n"
                        + "    \"clientId\": \"75d20ad6-94f4-47ba-88a3-79bea47fa2a8\",\n"
                        + "    \"command-result-description\": \"\",\n"
                        + "    \"requestId\": \"\",\n"
                        + "    \"requestid\": \"cc624779-d6cd-4580-931d-028c9eaee0b6:node-3\",\n"
                        + "    \"command-result-details\": \"\",\n"
                        + "    \"command-request-version\": \"v1\",\n"
                        + "    \"command-request-service\": \"bancoposta\",\n"
                        + "    \"command-request-command\": \"ricercaListaMovimentiConto\",\n"
                        + "    \"command-result-reason\": \"\",\n"
                        + "    \"status\": \"COMPLETED\"\n"
                        + "  },\n"
                        + "  \"body\": {\n"
                        + "    \"saldoH24\": true,\n"
                        + "    \"iban\": \"dummyIban\",\n"
                        + "    \"presenzaPrenotate\": false,\n"
                        + "    \"movimenti\": [\n"
                        + "      {\n"
                        + "        \"descrizioneCausale\": \"CANONE MENSILE CONTO BANCOPOSTA\",\n"
                        + "        \"segno\": \"-\",\n"
                        + "        \"header2\": \"ADDEBITO RELATIVO AL PERIODO DI AGOSTO 2020\",\n"
                        + "        \"header1\": \"CANONE CONTO \",\n"
                        + "        \"dataValuta\": \"31/08/2020\",\n"
                        + "        \"dataContabile\": \"10/09/2020\",\n"
                        + "        \"importo\": 400,\n"
                        + "        \"transactionID\": \"0010435811962020091000130053208500000\"\n"
                        + "      },\n"
                        + "      {\n"
                        + "        \"descrizioneCausale\": \"CANONE MENSILE CONTO BANCOPOSTA\",\n"
                        + "        \"segno\": \"-\",\n"
                        + "        \"header2\": \"ADDEBITO RELATIVO AL PERIODO DI LUGLIO 2020\",\n"
                        + "        \"header1\": \"CANONE CONTO \",\n"
                        + "        \"dataValuta\": \"31/07/2020\",\n"
                        + "        \"dataContabile\": \"19/08/2020\",\n"
                        + "        \"importo\": 400,\n"
                        + "        \"transactionID\": \"0010435811962020081900130058781600000\"\n"
                        + "      },\n"
                        + "      {\n"
                        + "        \"descrizioneCausale\": \"CANONE MENSILE CONTO BANCOPOSTA\",\n"
                        + "        \"segno\": \"+\",\n"
                        + "        \"header2\": \"ADDEBITO RELATIVO AL PERIODO DI SETTEMBRE 2019\",\n"
                        + "        \"header1\": \"CANONE CONTO \",\n"
                        + "        \"dataValuta\": \"30/09/2019\",\n"
                        + "        \"dataContabile\": \"10/10/2019\",\n"
                        + "        \"importo\": 400,\n"
                        + "        \"transactionID\": \"0010435811962019101000130045166000000\"\n"
                        + "      }\n"
                        + "    ],\n"
                        + "    \"saldo\": {\n"
                        + "      \"saldoDisponibile\": 370,\n"
                        + "      \"saldoContabile\": 370,\n"
                        + "      \"segnoSaldoContabile\": \"-\",\n"
                        + "      \"segnoSaldoDisponibile\": \"-\",\n"
                        + "      \"dataSaldo\": \"29/09/2020\"\n"
                        + "    },\n"
                        + "    \"bic\": \"BPPIITRRXXX\"\n"
                        + "  }\n"
                        + "}",
                AccountDetailsResponse.class);
    }

    public static TransactionsResponse getCheckingTransactionResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"header\": {\n"
                        + "    \"command-result\": \"0\",\n"
                        + "    \"clientId\": \"75d20ad6-94f4-47ba-88a3-79bea47fa2a8\",\n"
                        + "    \"command-result-description\": \"\",\n"
                        + "    \"requestId\": \"\",\n"
                        + "    \"requestid\": \"cc624779-d6cd-4580-931d-028c9eaee0b6:node-3\",\n"
                        + "    \"command-result-details\": \"\",\n"
                        + "    \"command-request-version\": \"v1\",\n"
                        + "    \"command-request-service\": \"bancoposta\",\n"
                        + "    \"command-request-command\": \"ricercaListaMovimentiConto\",\n"
                        + "    \"command-result-reason\": \"\",\n"
                        + "    \"status\": \"COMPLETED\"\n"
                        + "  },\n"
                        + "  \"body\": {\n"
                        + "    \"saldoH24\": true,\n"
                        + "    \"iban\": \"dummyIban\",\n"
                        + "    \"presenzaPrenotate\": false,\n"
                        + "    \"movimenti\": [\n"
                        + "      {\n"
                        + "        \"descrizioneCausale\": \"CANONE MENSILE CONTO BANCOPOSTA\",\n"
                        + "        \"segno\": \"-\",\n"
                        + "        \"header2\": \"ADDEBITO RELATIVO AL PERIODO DI AGOSTO 2020\",\n"
                        + "        \"header1\": \"CANONE CONTO \",\n"
                        + "        \"dataValuta\": \"31/08/2020\",\n"
                        + "        \"dataContabile\": \"10/09/2020\",\n"
                        + "        \"importo\": 400,\n"
                        + "        \"transactionID\": \"0010435811962020091000130053208500000\"\n"
                        + "      },\n"
                        + "      {\n"
                        + "        \"descrizioneCausale\": \"CANONE MENSILE CONTO BANCOPOSTA\",\n"
                        + "        \"segno\": \"-\",\n"
                        + "        \"header2\": \"ADDEBITO RELATIVO AL PERIODO DI LUGLIO 2020\",\n"
                        + "        \"header1\": \"CANONE CONTO \",\n"
                        + "        \"dataValuta\": \"31/07/2020\",\n"
                        + "        \"dataContabile\": \"19/08/2020\",\n"
                        + "        \"importo\": 400,\n"
                        + "        \"transactionID\": \"0010435811962020081900130058781600000\"\n"
                        + "      },\n"
                        + "      {\n"
                        + "        \"descrizioneCausale\": \"CANONE MENSILE CONTO BANCOPOSTA\",\n"
                        + "        \"segno\": \"+\",\n"
                        + "        \"header2\": \"ADDEBITO RELATIVO AL PERIODO DI SETTEMBRE 2019\",\n"
                        + "        \"header1\": \"CANONE CONTO \",\n"
                        + "        \"dataValuta\": \"30/09/2019\",\n"
                        + "        \"dataContabile\": \"10/10/2019\",\n"
                        + "        \"importo\": 400,\n"
                        + "        \"transactionID\": \"0010435811962019101000130045166000000\"\n"
                        + "      }\n"
                        + "    ],\n"
                        + "    \"saldo\": {\n"
                        + "      \"saldoDisponibile\": 370,\n"
                        + "      \"saldoContabile\": 370,\n"
                        + "      \"segnoSaldoContabile\": \"-\",\n"
                        + "      \"segnoSaldoDisponibile\": \"-\",\n"
                        + "      \"dataSaldo\": \"29/09/2020\"\n"
                        + "    },\n"
                        + "    \"bic\": \"BPPIITRRXXX\"\n"
                        + "  }\n"
                        + "}",
                TransactionsResponse.class);
    }

    public static TransactionsResponse getEmptyCheckingTransactionResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"header\": {\n"
                        + "    \"command-result\": \"0\",\n"
                        + "    \"clientId\": \"75d20ad6-94f4-47ba-88a3-79bea47fa2a8\",\n"
                        + "    \"command-result-description\": \"\",\n"
                        + "    \"requestId\": \"\",\n"
                        + "    \"requestid\": \"cc624779-d6cd-4580-931d-028c9eaee0b6:node-3\",\n"
                        + "    \"command-result-details\": \"\",\n"
                        + "    \"command-request-version\": \"v1\",\n"
                        + "    \"command-request-service\": \"bancoposta\",\n"
                        + "    \"command-request-command\": \"ricercaListaMovimentiConto\",\n"
                        + "    \"command-result-reason\": \"\",\n"
                        + "    \"status\": \"COMPLETED\"\n"
                        + "  },\n"
                        + "  \"body\": {\n"
                        + "    \"saldoH24\": true,\n"
                        + "    \"iban\": \"dummyIban\",\n"
                        + "    \"presenzaPrenotate\": false,\n"
                        + "    \"movimenti\": ["
                        + "    ],\n"
                        + "    \"saldo\": {\n"
                        + "      \"saldoDisponibile\": 370,\n"
                        + "      \"saldoContabile\": 370,\n"
                        + "      \"segnoSaldoContabile\": \"-\",\n"
                        + "      \"segnoSaldoDisponibile\": \"-\",\n"
                        + "      \"dataSaldo\": \"29/09/2020\"\n"
                        + "    },\n"
                        + "    \"bic\": \"BPPIITRRXXX\"\n"
                        + "  }\n"
                        + "}",
                TransactionsResponse.class);
    }

    public static SavingAccountResponse getSavingAccountsResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"header\": {\n"
                        + "        \"command-result\": \"0\",\n"
                        + "        \"clientId\": \"f358a2ee-eb26-4726-8282-16aa65df8043\",\n"
                        + "        \"command-result-description\": \"\",\n"
                        + "        \"requestId\": \"\",\n"
                        + "        \"requestid\": \"7681efa9-bbcd-40b7-8be6-730055eca35f:node-2\",\n"
                        + "        \"command-result-details\": \"\",\n"
                        + "        \"command-request-version\": \"v2\",\n"
                        + "        \"command-request-service\": \"bancoposta\",\n"
                        + "        \"command-request-command\": \"listaLibrettiH24\",\n"
                        + "        \"command-result-reason\": \"\",\n"
                        + "        \"status\": \"COMPLETED\"\n"
                        + "    },\n"
                        + "    \"body\": {\n"
                        + "        \"statoSemaforoBfp\": \"standard\",\n"
                        + "        \"listaLibretti\": [\n"
                        + "            {\n"
                        + "                \"onBoardable\": true,\n"
                        + "                \"presenzaBfp\": false,\n"
                        + "                \"presenzaPvr\": false,\n"
                        + "                \"monointestato\": true,\n"
                        + "                \"iban\": \"IT10U0760103384000050444884\",\n"
                        + "                \"numeroRapporto\": \"000050444884\",\n"
                        + "                \"librettoSmart\": true\n"
                        + "            }\n"
                        + "        ],\n"
                        + "        \"statoSemaforoDr\": \"standard\"\n"
                        + "    }\n"
                        + "}",
                SavingAccountResponse.class);
    }

    public static SavingAccountDetailsResponse getSavingAccountsDetailsResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"header\": {\n"
                        + "        \"command-result\": \"0\",\n"
                        + "        \"clientId\": \"3786256d-d177-40c1-b0ad-5395ffde614f\",\n"
                        + "        \"command-result-description\": \"\",\n"
                        + "        \"requestId\": \"\",\n"
                        + "        \"requestid\": \"f0be1897-2cb7-46b4-97f1-ba6f11537d3c:node-2\",\n"
                        + "        \"command-result-details\": \"\",\n"
                        + "        \"command-request-version\": \"v2\",\n"
                        + "        \"command-request-service\": \"bancoposta\",\n"
                        + "        \"command-request-command\": \"listaMovimentiLibrettoH24\",\n"
                        + "        \"command-result-reason\": \"\",\n"
                        + "        \"status\": \"COMPLETED\"\n"
                        + "    },\n"
                        + "    \"body\": {\n"
                        + "        \"listaMovimentoRisparmioPostale\": [\n"
                        + "            {\n"
                        + "                \"segno\": \"A\",\n"
                        + "                \"descrizione\": \"ACCREDITO GIRO CONTO ALTRO INTERMEDIARIO\",\n"
                        + "                \"divisa\": \"EUR\",\n"
                        + "                \"dataValuta\": \"04/02/2020\",\n"
                        + "                \"dataContabile\": \"04/02/2020\",\n"
                        + "                \"descrizioneEstesa\": \"ACCREDITO GIRO CONTO ALTRO INT\",\n"
                        + "                \"importo\": 500,\n"
                        + "                \"causale\": \"BO79\"\n"
                        + "            }\n"
                        + "        ],\n"
                        + "        \"statoSemaforoBfp\": \"standard\",\n"
                        + "        \"saldo\": {\n"
                        + "            \"saldoDisponibile\": 500,\n"
                        + "            \"saldoContabile\": 500\n"
                        + "        },\n"
                        + "        \"statoSemaforoDr\": \"standard\"\n"
                        + "    }\n"
                        + "}",
                SavingAccountDetailsResponse.class);
    }

    public static SavingTransactionResponse getSavingTransactionsResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"header\": {\n"
                        + "        \"command-result\": \"0\",\n"
                        + "        \"clientId\": \"3786256d-d177-40c1-b0ad-5395ffde614f\",\n"
                        + "        \"command-result-description\": \"\",\n"
                        + "        \"requestId\": \"\",\n"
                        + "        \"requestid\": \"f0be1897-2cb7-46b4-97f1-ba6f11537d3c:node-2\",\n"
                        + "        \"command-result-details\": \"\",\n"
                        + "        \"command-request-version\": \"v2\",\n"
                        + "        \"command-request-service\": \"bancoposta\",\n"
                        + "        \"command-request-command\": \"listaMovimentiLibrettoH24\",\n"
                        + "        \"command-result-reason\": \"\",\n"
                        + "        \"status\": \"COMPLETED\"\n"
                        + "    },\n"
                        + "    \"body\": {\n"
                        + "        \"listaMovimentoRisparmioPostale\": [\n"
                        + "            {\n"
                        + "                \"segno\": \"A\",\n"
                        + "                \"descrizione\": \"ACCREDITO GIRO CONTO ALTRO INTERMEDIARIO\",\n"
                        + "                \"divisa\": \"EUR\",\n"
                        + "                \"dataValuta\": \"04/02/2020\",\n"
                        + "                \"dataContabile\": \"04/02/2020\",\n"
                        + "                \"descrizioneEstesa\": \"ACCREDITO GIRO CONTO ALTRO INT\",\n"
                        + "                \"importo\": 500,\n"
                        + "                \"causale\": \"BO79\"\n"
                        + "            }\n"
                        + "        ],\n"
                        + "        \"statoSemaforoBfp\": \"standard\",\n"
                        + "        \"saldo\": {\n"
                        + "            \"saldoDisponibile\": 500,\n"
                        + "            \"saldoContabile\": 500\n"
                        + "        },\n"
                        + "        \"statoSemaforoDr\": \"standard\"\n"
                        + "    }\n"
                        + "}",
                SavingTransactionResponse.class);
    }
}
