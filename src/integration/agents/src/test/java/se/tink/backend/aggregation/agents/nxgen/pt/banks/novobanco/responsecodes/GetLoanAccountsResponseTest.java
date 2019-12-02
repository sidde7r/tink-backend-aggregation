package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.responsecodes;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.loan.GetLoanAccountsResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class GetLoanAccountsResponseTest {

    @Test
    public void shouldReportSuccessIfSuccessfulGetLoanAccountsResponse() {
        GetLoanAccountsResponse response =
                SerializationUtils.deserializeFromString(
                        getSuccessfulResponse(), GetLoanAccountsResponse.class);
        assertTrue(response.isSuccessful());
    }

    @Test
    public void shouldReportFailureIfUnsuccessfulGetLoanAccountsResponse() {
        GetLoanAccountsResponse response =
                SerializationUtils.deserializeFromString(
                        getUnsuccessfulResponse(), GetLoanAccountsResponse.class);
        assertFalse(response.isSuccessful());
    }

    private String getSuccessfulResponse() {
        return "{\n"
                + "  \"Header\": {\n"
                + "    \"ResponseId\": \"43c12182751047738776c1ac23214800\",\n"
                + "    \"OpToken\": \"d3617656e55a45d1b1b8b027c6355f65\",\n"
                + "    \"Time\": \"2019-11-23T21:59:59.5640587Z\",\n"
                + "    \"SessionTimeout\": 1800,\n"
                + "    \"Status\": {\n"
                + "      \"Severidade\": 0,\n"
                + "      \"Codigo\": 0\n"
                + "    },\n"
                + "    \"Contexto\": {\n"
                + "      \"Contas\": {\n"
                + "        \"Lista\": [{\n"
                + "          \"Id\": \"1234567\",\n"
                + "          \"Iban\": \"PT50123456789\",\n"
                + "          \"Desc\": \"Conta DO\"\n"
                + "        }],\n"
                + "        \"Selected\": \"1234567\"\n"
                + "      }\n"
                + "    }\n"
                + "  },\n"
                + "  \"Body\": {\n"
                + "    \"Seccao\": {\n"
                + "      \"Designacao\": \"Crédito Habitação\",\n"
                + "      \"Valor\": 222166.13,\n"
                + "      \"Estado\": 0,\n"
                + "      \"Tipo\": 15,\n"
                + "      \"Detalhes\": [{\n"
                + "        \"Produto\": \"BHS SWAP - AQUISICAO C/ HIPO\",\n"
                + "        \"Contrato\": \"12345\",\n"
                + "        \"Saldo\": 80140.34\n"
                + "      }, {\n"
                + "        \"Produto\": \"BHP - AQUISICAO DEFICIENTES\",\n"
                + "        \"Contrato\": \"12346\",\n"
                + "        \"Saldo\": 142025.79\n"
                + "      }]\n"
                + "    }\n"
                + "  }\n"
                + "}";
    }

    private String getUnsuccessfulResponse() {
        return "{\n"
                + "  \"Header\": {\n"
                + "    \"ResponseId\": \"09f05aa1386e4c81a5744febe25ce6db\",\n"
                + "    \"OpToken\": \"6e737501756e4663a6e8710f72b0b256\",\n"
                + "    \"Time\": \"2019-11-23T21:55:49.2870229Z\",\n"
                + "    \"SessionTimeout\": 1800,\n"
                + "    \"Status\": {\n"
                + "      \"Mensagem\": \"Não foi possível selecionar conta\",\n"
                + "      \"Severidade\": 3,\n"
                + "      \"Codigo\": 2004\n"
                + "    },\n"
                + "    \"Contexto\": {\n"
                + "      \"Contas\": {\n"
                + "        \"Lista\": [{\n"
                + "          \"Id\": \"1234567\",\n"
                + "          \"Iban\": \"PT5012345678\",\n"
                + "          \"Desc\": \"Conta DO\"\n"
                + "        }],\n"
                + "        \"Selected\": \"1234567\"\n"
                + "      }\n"
                + "    }\n"
                + "  },\n"
                + "  \"Body\": {},\n"
                + "  \"Login\": {}\n"
                + "}";
    }
}
