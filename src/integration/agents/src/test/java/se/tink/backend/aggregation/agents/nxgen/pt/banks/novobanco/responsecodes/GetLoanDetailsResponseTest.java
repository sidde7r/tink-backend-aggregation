package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.responsecodes;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.loan.GetLoanDetailsResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class GetLoanDetailsResponseTest {
    @Test
    public void shouldReportSuccessIfSuccessfulGetLoanDetailsResponse() {
        GetLoanDetailsResponse response =
                SerializationUtils.deserializeFromString(
                        getSuccessfulResponse(), GetLoanDetailsResponse.class);
        assertTrue(response.isSuccessful());
    }

    @Test
    public void shouldReportFailureIfUnsuccessfulGetLoanDetailsResponse() {
        GetLoanDetailsResponse response =
                SerializationUtils.deserializeFromString(
                        getUnsuccessfulResponse(), GetLoanDetailsResponse.class);
        assertFalse(response.isSuccessful());
    }

    private String getSuccessfulResponse() {
        return "{\n"
                + "\t\"Header\": {\n"
                + "\t\t\"ResponseId\": \"73526a81f758449d9764780e152b4779\",\n"
                + "\t\t\"OpToken\": \"49936e4baec2467d9454caf7dd47b1c4\",\n"
                + "\t\t\"Time\": \"2019-11-23T21:55:48.7681655Z\",\n"
                + "\t\t\"SessionTimeout\": 1800,\n"
                + "\t\t\"Status\": {\n"
                + "\t\t\t\"Severidade\": 0,\n"
                + "\t\t\t\"Codigo\": 0\n"
                + "\t\t}\n"
                + "\t},\n"
                + "\t\"Body\": {\n"
                + "\t\t\"Detalhe\": {\n"
                + "\t\t\t\"Header\": {\n"
                + "\t\t\t\t\"Titulo\": \"BHP - AQUISICAO DEFICIENTES\",\n"
                + "\t\t\t\t\"SubTitulo\": {\n"
                + "\t\t\t\t\t\"AM\": 1,\n"
                + "\t\t\t\t\t\"T\": 3,\n"
                + "\t\t\t\t\t\"L\": \"Próxima prestação - Nº 13\",\n"
                + "\t\t\t\t\t\"V\": \"EUR\",\n"
                + "\t\t\t\t\t\"DV\": 377.39\n"
                + "\t\t\t\t},\n"
                + "\t\t\t\t\"Linhas\": [{\n"
                + "\t\t\t\t\t\"L\": \"Data da próxima prestação\",\n"
                + "\t\t\t\t\t\"V\": \"02-12-2019\"\n"
                + "\t\t\t\t}, {\n"
                + "\t\t\t\t\t\"L\": \"Spread\",\n"
                + "\t\t\t\t\t\"V\": \"0,00%\"\n"
                + "\t\t\t\t}, {\n"
                + "\t\t\t\t\t\"L\": \"TAN\",\n"
                + "\t\t\t\t\t\"V\": \"0,00%\"\n"
                + "\t\t\t\t}, {\n"
                + "\t\t\t\t\t\"L\": \"Capital em divida\",\n"
                + "\t\t\t\t\t\"V\": \"142.025,79 EUR\"\n"
                + "\t\t\t\t}]\n"
                + "\t\t\t},\n"
                + "\t\t\t\"Linhas\": [{\n"
                + "\t\t\t\t\"T\": 30,\n"
                + "\t\t\t\t\"Linhas\": [{\n"
                + "\t\t\t\t\t\"T\": 32,\n"
                + "\t\t\t\t\t\"L\": \"Contrato\",\n"
                + "\t\t\t\t\t\"V\": \"1234567\",\n"
                + "\t\t\t\t\t\"Linhas\": [{\n"
                + "\t\t\t\t\t\t\"L\": \"Designação\",\n"
                + "\t\t\t\t\t\t\"V\": \"BHP - AQUISICAO DEFICIENTES\"\n"
                + "\t\t\t\t\t}, {\n"
                + "\t\t\t\t\t\t\"L\": \"Data Início do contrato\",\n"
                + "\t\t\t\t\t\t\"V\": \"02-11-2018\"\n"
                + "\t\t\t\t\t}, {\n"
                + "\t\t\t\t\t\t\"L\": \"Capital contratado\",\n"
                + "\t\t\t\t\t\t\"V\": \"146.510,79 EUR\"\n"
                + "\t\t\t\t\t}, {\n"
                + "\t\t\t\t\t\t\"L\": \"Capital utilizado\",\n"
                + "\t\t\t\t\t\t\"V\": \"146.510,79 EUR\"\n"
                + "\t\t\t\t\t}, {\n"
                + "\t\t\t\t\t\t\"L\": \"Someone contratado\",\n"
                + "\t\t\t\t\t\t\"V\": \"392\"\n"
                + "\t\t\t\t\t}, {\n"
                + "\t\t\t\t\t\t\"L\": \"Moeda\",\n"
                + "\t\t\t\t\t\t\"V\": \"EUR\"\n"
                + "\t\t\t\t\t}, {\n"
                + "\t\t\t\t\t\t\"L\": \"Regime\",\n"
                + "\t\t\t\t\t\t\"V\": \"Bonificado\"\n"
                + "\t\t\t\t\t}, {\n"
                + "\t\t\t\t\t\t\"L\": \"Tipo de taxa\",\n"
                + "\t\t\t\t\t\t\"V\": \"Variável\"\n"
                + "\t\t\t\t\t}, {\n"
                + "\t\t\t\t\t\t\"L\": \"Spread\",\n"
                + "\t\t\t\t\t\t\"V\": \"0,00%\"\n"
                + "\t\t\t\t\t}, {\n"
                + "\t\t\t\t\t\t\"L\": \"Bonificação ao spread\",\n"
                + "\t\t\t\t\t\t\"V\": \"0,00%\"\n"
                + "\t\t\t\t\t}, {\n"
                + "\t\t\t\t\t\t\"L\": \"Penalização ao spread\",\n"
                + "\t\t\t\t\t\t\"V\": \"0,00%\"\n"
                + "\t\t\t\t\t}, {\n"
                + "\t\t\t\t\t\t\"L\": \"Escalão de bonificação\",\n"
                + "\t\t\t\t\t\t\"V\": \"0\"\n"
                + "\t\t\t\t\t}]\n"
                + "\t\t\t\t}, {\n"
                + "\t\t\t\t\t\"T\": 32,\n"
                + "\t\t\t\t\t\"L\": \"Próxima prestação\",\n"
                + "\t\t\t\t\t\"V\": \"Nº 13\",\n"
                + "\t\t\t\t\t\"Linhas\": [{\n"
                + "\t\t\t\t\t\t\"L\": \"Data valor\",\n"
                + "\t\t\t\t\t\t\"V\": \"02-12-2019\"\n"
                + "\t\t\t\t\t}, {\n"
                + "\t\t\t\t\t\t\"L\": \"Montante da prestação\",\n"
                + "\t\t\t\t\t\t\"V\": \"377,39 EUR\"\n"
                + "\t\t\t\t\t}, {\n"
                + "\t\t\t\t\t\t\"L\": \"Capital amortizado\",\n"
                + "\t\t\t\t\t\t\"V\": \"373,75 EUR\"\n"
                + "\t\t\t\t\t}, {\n"
                + "\t\t\t\t\t\t\"L\": \"Capital em divida na data de emissão do aviso\",\n"
                + "\t\t\t\t\t\t\"V\": \"141.652,04 EUR\"\n"
                + "\t\t\t\t\t}, {\n"
                + "\t\t\t\t\t\t\"L\": \"Indexante\",\n"
                + "\t\t\t\t\t\t\"V\": \"0,00%\"\n"
                + "\t\t\t\t\t}, {\n"
                + "\t\t\t\t\t\t\"L\": \"TAN\",\n"
                + "\t\t\t\t\t\t\"V\": \"0,00%\"\n"
                + "\t\t\t\t\t}, {\n"
                + "\t\t\t\t\t\t\"L\": \"Juros\",\n"
                + "\t\t\t\t\t\t\"V\": \"0,00 EUR\"\n"
                + "\t\t\t\t\t}, {\n"
                + "\t\t\t\t\t\t\"L\": \"Bonificação de juros\",\n"
                + "\t\t\t\t\t\t\"V\": \"0,00 EUR\"\n"
                + "\t\t\t\t\t}, {\n"
                + "\t\t\t\t\t\t\"L\": \"Imposto de selo s/juros\",\n"
                + "\t\t\t\t\t\t\"V\": \"0,00 EUR\"\n"
                + "\t\t\t\t\t}, {\n"
                + "\t\t\t\t\t\t\"L\": \"Comissão de liquidação\",\n"
                + "\t\t\t\t\t\t\"V\": \"3,50 EUR\"\n"
                + "\t\t\t\t\t}, {\n"
                + "\t\t\t\t\t\t\"L\": \"Imposto de selo s/comissão de liquidação\",\n"
                + "\t\t\t\t\t\t\"V\": \"0,14 EUR\"\n"
                + "\t\t\t\t\t}]\n"
                + "\t\t\t\t}, {\n"
                + "\t\t\t\t\t\"T\": 32,\n"
                + "\t\t\t\t\t\"L\": \"Última prestação paga\",\n"
                + "\t\t\t\t\t\"V\": \"Nº 12\",\n"
                + "\t\t\t\t\t\"Linhas\": [{\n"
                + "\t\t\t\t\t\t\"L\": \"Data valor\",\n"
                + "\t\t\t\t\t\t\"V\": \"02-11-2019\"\n"
                + "\t\t\t\t\t}, {\n"
                + "\t\t\t\t\t\t\"L\": \"Montante da prestação\",\n"
                + "\t\t\t\t\t\t\"V\": \"377,39 EUR\"\n"
                + "\t\t\t\t\t}, {\n"
                + "\t\t\t\t\t\t\"L\": \"Capital amortizado\",\n"
                + "\t\t\t\t\t\t\"V\": \"373,75 EUR\"\n"
                + "\t\t\t\t\t}, {\n"
                + "\t\t\t\t\t\t\"L\": \"Capital em divida na data de emissão do aviso\",\n"
                + "\t\t\t\t\t\t\"V\": \"142.025,79 EUR\"\n"
                + "\t\t\t\t\t}, {\n"
                + "\t\t\t\t\t\t\"L\": \"Indexante\",\n"
                + "\t\t\t\t\t\t\"V\": \"0,00%\"\n"
                + "\t\t\t\t\t}, {\n"
                + "\t\t\t\t\t\t\"L\": \"TAN\",\n"
                + "\t\t\t\t\t\t\"V\": \"0,00%\"\n"
                + "\t\t\t\t\t}, {\n"
                + "\t\t\t\t\t\t\"L\": \"Juros\",\n"
                + "\t\t\t\t\t\t\"V\": \"0,00 EUR\"\n"
                + "\t\t\t\t\t}, {\n"
                + "\t\t\t\t\t\t\"L\": \"Bonificação de juros\",\n"
                + "\t\t\t\t\t\t\"V\": \"0,00 EUR\"\n"
                + "\t\t\t\t\t}, {\n"
                + "\t\t\t\t\t\t\"L\": \"Imposto de selo s/juros\",\n"
                + "\t\t\t\t\t\t\"V\": \"0,00 EUR\"\n"
                + "\t\t\t\t\t}, {\n"
                + "\t\t\t\t\t\t\"L\": \"Comissão de liquidação\",\n"
                + "\t\t\t\t\t\t\"V\": \"3,50 EUR\"\n"
                + "\t\t\t\t\t}, {\n"
                + "\t\t\t\t\t\t\"L\": \"Imposto de selo s/comissão de liquidação\",\n"
                + "\t\t\t\t\t\t\"V\": \"0,14 EUR\"\n"
                + "\t\t\t\t\t}, {\n"
                + "\t\t\t\t\t\t\"L\": \"Imposto s/comissão de atraso\",\n"
                + "\t\t\t\t\t\t\"V\": \"0,00 EUR\"\n"
                + "\t\t\t\t\t}, {\n"
                + "\t\t\t\t\t\t\"L\": \"Juros de Mora\",\n"
                + "\t\t\t\t\t\t\"V\": \"0,00 EUR\"\n"
                + "\t\t\t\t\t}, {\n"
                + "\t\t\t\t\t\t\"L\": \"Imposto s/juros de mora\",\n"
                + "\t\t\t\t\t\t\"V\": \"0,00 EUR\"\n"
                + "\t\t\t\t\t}]\n"
                + "\t\t\t\t}]\n"
                + "\t\t\t}]\n"
                + "\t\t}\n"
                + "\t}\n"
                + "}";
    }

    private String getUnsuccessfulResponse() {
        return "{\n"
                + "\t\"Header\": {\n"
                + "\t\t\"ResponseId\": \"28e913f5b57b42f793aff03bf7614b3f\",\n"
                + "\t\t\"OpToken\": null,\n"
                + "\t\t\"Time\": \"2019-11-25T11:04:39.3213808Z\",\n"
                + "\t\t\"SessionTimeout\": 1800,\n"
                + "\t\t\"Status\": {\n"
                + "\t\t\t\"Mensagem\": \"De momento não é possível processar a sua instrução. Por favor tente mais tarde.\",\n"
                + "\t\t\t\"Severidade\": 3,\n"
                + "\t\t\t\"Codigo\": 50\n"
                + "\t\t}\n"
                + "\t}\n"
                + "}";
    }
}
