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
                + "\t\"Header\": {\n"
                + "\t\t\"ResponseId\": \"43c12182751047738776c1ac23214800\",\n"
                + "\t\t\"OpToken\": \"d3617656e55a45d1b1b8b027c6355f65\",\n"
                + "\t\t\"Time\": \"2019-11-23T21:59:59.5640587Z\",\n"
                + "\t\t\"SessionTimeout\": 1800,\n"
                + "\t\t\"Status\": {\n"
                + "\t\t\t\"Severidade\": 0,\n"
                + "\t\t\t\"Codigo\": 0\n"
                + "\t\t},\n"
                + "\t\t\"Contexto\": {\n"
                + "\t\t\t\"Contas\": {\n"
                + "\t\t\t\t\"Lista\": [{\n"
                + "\t\t\t\t\t\"Id\": \"1234567\",\n"
                + "\t\t\t\t\t\"Iban\": \"PT50123456789\",\n"
                + "\t\t\t\t\t\"Desc\": \"Conta DO\"\n"
                + "\t\t\t\t}],\n"
                + "\t\t\t\t\"Selected\": \"1234567\"\n"
                + "\t\t\t}\n"
                + "\t\t}\n"
                + "\t},\n"
                + "\t\"Body\": {\n"
                + "\t\t\"Seccao\": {\n"
                + "\t\t\t\"Designacao\": \"Crédito Habitação\",\n"
                + "\t\t\t\"Valor\": 222166.13,\n"
                + "\t\t\t\"Estado\": 0,\n"
                + "\t\t\t\"Tipo\": 15,\n"
                + "\t\t\t\"Detalhes\": [{\n"
                + "\t\t\t\t\"Produto\": \"BHS SWAP - AQUISICAO C/ HIPO\",\n"
                + "\t\t\t\t\"Contrato\": \"12345\",\n"
                + "\t\t\t\t\"Saldo\": 80140.34\n"
                + "\t\t\t}, {\n"
                + "\t\t\t\t\"Produto\": \"BHP - AQUISICAO DEFICIENTES\",\n"
                + "\t\t\t\t\"Contrato\": \"12346\",\n"
                + "\t\t\t\t\"Saldo\": 142025.79\n"
                + "\t\t\t}]\n"
                + "\t\t}\n"
                + "\t}\n"
                + "}";
    }

    private String getUnsuccessfulResponse() {
        return "{\n"
                + "\t\"Header\": {\n"
                + "\t\t\"ResponseId\": \"09f05aa1386e4c81a5744febe25ce6db\",\n"
                + "\t\t\"OpToken\": \"6e737501756e4663a6e8710f72b0b256\",\n"
                + "\t\t\"Time\": \"2019-11-23T21:55:49.2870229Z\",\n"
                + "\t\t\"SessionTimeout\": 1800,\n"
                + "\t\t\"Status\": {\n"
                + "\t\t\t\"Mensagem\": \"Não foi possível selecionar conta\",\n"
                + "\t\t\t\"Severidade\": 3,\n"
                + "\t\t\t\"Codigo\": 2004\n"
                + "\t\t},\n"
                + "\t\t\"Contexto\": {\n"
                + "\t\t\t\"Contas\": {\n"
                + "\t\t\t\t\"Lista\": [{\n"
                + "\t\t\t\t\t\"Id\": \"1234567\",\n"
                + "\t\t\t\t\t\"Iban\": \"PT5012345678\",\n"
                + "\t\t\t\t\t\"Desc\": \"Conta DO\"\n"
                + "\t\t\t\t}],\n"
                + "\t\t\t\t\"Selected\": \"1234567\"\n"
                + "\t\t\t}\n"
                + "\t\t}\n"
                + "\t},\n"
                + "\t\"Body\": {},\n"
                + "\t\"Login\": {}\n"
                + "}";
    }
}
