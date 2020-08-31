package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.fetcher;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.fetcher.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.fetcher.rpc.TransactionsResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class FetchersTestData {

    static AccountsResponse loanAccountResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"exitCode\": \"OK\",\n"
                        + "  \"payload\": {\n"
                        + "    \"elencoViste\": [\n"
                        + "      {\n"
                        + "        \"elencoRapporti\": [\n"
                        + "          {\n"
                        + "            \"coordinateDaVisualizzare\": {\n"
                        + "              \"iban\": \"IT14X0300203280334787988525\"\n"
                        + "            },\n"
                        + "            \"descrizione\": \"Conto\",\n"
                        + "            \"descrizioneCommerciale\": \"Conto per Merito\",\n"
                        + "            \"divisa\": \"EUR\",\n"
                        + "            \"id\": \"123456789\",\n"
                        + "            \"listaIntestatariCompleta\": [\n"
                        + "              {\n"
                        + "                \"cognome\": \"TESTNAME\",\n"
                        + "                \"denominazione\": \"TESTSURNAME TESTNAME\",\n"
                        + "                \"id\": \"1\",\n"
                        + "                \"nome\": \"TESTSURNAME\"\n"
                        + "              }\n"
                        + "            ],\n"
                        + "            \"operativo\": true,\n"
                        + "            \"saldo\": {\n"
                        + "              \"fido\": 7500.00,\n"
                        + "              \"saldoContabile\": -7500.00,\n"
                        + "              \"saldoDisponibile\": 0.00,\n"
                        + "              \"saldoDisponibileFidoEscluso\": -7500.00\n"
                        + "            }\n"
                        + "          }\n"
                        + "        ]\n"
                        + "      }\n"
                        + "    ]\n"
                        + "  }\n"
                        + "}",
                AccountsResponse.class);
    }

    static AccountsResponse checkingAccountResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"exitCode\": \"OK\",\n"
                        + "  \"payload\": {\n"
                        + "    \"elencoViste\": [\n"
                        + "      {\n"
                        + "        \"elencoRapporti\": [\n"
                        + "          {\n"
                        + "            \"coordinateDaVisualizzare\": {\n"
                        + "              \"iban\": \"IT58F0300203280166615394326\"\n"
                        + "            },\n"
                        + "            \"descrizione\": \"Conto\",\n"
                        + "            \"descrizioneCommerciale\": \"XME Conto\",\n"
                        + "            \"divisa\": \"EUR\",\n"
                        + "            \"id\": \"987654321\",\n"
                        + "            \"listaIntestatariCompleta\": [\n"
                        + "              {\n"
                        + "                \"cognome\": \"TESTNAME\",\n"
                        + "                \"denominazione\": \"TESTSURNAME TESTNAME\",\n"
                        + "                \"id\": \"1\",\n"
                        + "                \"nome\": \"TESTSURNAME\"\n"
                        + "              }\n"
                        + "            ],\n"
                        + "            \"operativo\": true,\n"
                        + "            \"saldo\": {\n"
                        + "              \"fido\": 0.00,\n"
                        + "              \"saldoContabile\": 500.00,\n"
                        + "              \"saldoDisponibile\": 500.00,\n"
                        + "              \"saldoDisponibileFidoEscluso\": 500.00\n"
                        + "            }\n"
                        + "          }\n"
                        + "        ]\n"
                        + "      }\n"
                        + "    ]\n"
                        + "  }\n"
                        + "}",
                AccountsResponse.class);
    }

    static TransactionsResponse fetchTransactionsResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"exitCode\": \"OK\",\n"
                        + "  \"payload\": {\n"
                        + "    \"operazioni\": [\n"
                        + "      {\n"
                        + "        \"contabilizzato\": false,\n"
                        + "        \"dataContabilizzazione\": 1569448800000,\n"
                        + "        \"dataValuta\": 1569448800000,\n"
                        + "        \"descrizioneBreve\": \"Bonifico disposto da: NAME TEST\",\n"
                        + "        \"descrizioneEstesa\": \"very long description\",\n"
                        + "        \"detectedCategories\": [\n"
                        + "          {\n"
                        + "            \"categoryId\": 815,\n"
                        + "            \"categoryName\": \"Erogazione Finanziamento\",\n"
                        + "            \"icon\": \"815\",\n"
                        + "            \"score\": 0.6667\n"
                        + "          },\n"
                        + "          {\n"
                        + "            \"categoryId\": 805,\n"
                        + "            \"categoryName\": \"Bonifici ricevuti\",\n"
                        + "            \"icon\": \"805\",\n"
                        + "            \"score\": 0.3333\n"
                        + "          }\n"
                        + "        ],\n"
                        + "        \"divisa\": \"EUR\",\n"
                        + "        \"importo\": 709.8000\n"
                        + "      },\n"
                        + "      {\n"
                        + "        \"contabilizzato\": true,\n"
                        + "        \"dataContabilizzazione\": 1567375200000,\n"
                        + "        \"dataValuta\": 1567375200000,\n"
                        + "        \"descrizioneBreve\": \"Pag.finanziamento Rateale\",\n"
                        + "        \"descrizioneEstesa\": \"very very long description\",\n"
                        + "        \"detectedCategories\": [\n"
                        + "          {\n"
                        + "            \"categoryId\": 27,\n"
                        + "            \"categoryName\": \"Rate Mutuo e Finanziamento\",\n"
                        + "            \"icon\": \"27\",\n"
                        + "            \"score\": 1.0\n"
                        + "          }\n"
                        + "        ],\n"
                        + "        \"divisa\": \"EUR\",\n"
                        + "        \"importo\": -709.7600\n"
                        + "      }\n"
                        + "    ]\n"
                        + "  }\n"
                        + "}",
                TransactionsResponse.class);
    }

    static TransactionsResponse emptyTransactionsResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"exitCode\": \"OK\",\n"
                        + "  \"payload\": {\n"
                        + "    \"operazioni\": []\n"
                        + "  }\n"
                        + "}",
                TransactionsResponse.class);
    }
}
