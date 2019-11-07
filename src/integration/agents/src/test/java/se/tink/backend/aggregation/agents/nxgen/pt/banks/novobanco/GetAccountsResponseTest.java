package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.GetAccountsResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class GetAccountsResponseTest {

    @Test
    public void testSuccessfulGetAccounts() {
        GetAccountsResponse response = SerializationUtils.deserializeFromString(getSuccessfulResponse(), GetAccountsResponse.class);
        assertTrue (response.isSuccessful());
    }

    @Test
    public void testUnsuccessfulGetAccounts() {
        GetAccountsResponse response = SerializationUtils.deserializeFromString(getUnsuccessfulResponse(), GetAccountsResponse.class);
        assertFalse (response.isSuccessful());
    }

    private String getUnsuccessfulResponse() {
        return "{\n" +
                "\t\"Header\": {\n" +
                "\t\t\"ResponseId\": \"b7e8627cf9b64542aaf0a64723838950\",\n" +
                "\t\t\"OpToken\": null,\n" +
                "\t\t\"Time\": \"2019-11-15T14:53:06.8443181Z\",\n" +
                "\t\t\"SessionTimeout\": 0,\n" +
                "\t\t\"Status\": {\n" +
                "\t\t\t\"Mensagem\": \"De momento não é possível processar a sua instrução. Por favor tente mais tarde.\",\n" +
                "\t\t\t\"Severidade\": 3,\n" +
                "\t\t\t\"Codigo\": 50\n" +
                "\t\t}\n" +
                "\t}\n" +
                "}";
    }


    private String getSuccessfulResponse() {
        return "{\n" +
                "\t\"Header\": {\n" +
                "\t\t\"ResponseId\": \"725a4e8393034c67bf9b6d198e3bc279\",\n" +
                "\t\t\"OpToken\": \"2d034c8125144157895c665eca02919e\",\n" +
                "\t\t\"Time\": \"2019-11-15T14:53:08.6188743Z\",\n" +
                "\t\t\"SessionTimeout\": 1800,\n" +
                "\t\t\"Status\": {\n" +
                "\t\t\t\"Severidade\": 0,\n" +
                "\t\t\t\"Codigo\": 0\n" +
                "\t\t},\n" +
                "\t\t\"Contexto\": {\n" +
                "\t\t\t\"Contas\": {\n" +
                "\t\t\t\t\"Lista\": [{\n" +
                "\t\t\t\t\t\"Id\": \"000000000000001\",\n" +
                "\t\t\t\t\t\"Iban\": \"PT500000000000000001\",\n" +
                "\t\t\t\t\t\"Desc\": \"Conta DO\"\n" +
                "\t\t\t\t}, {\n" +
                "\t\t\t\t\t\"Id\": \"10101010010101\",\n" +
                "\t\t\t\t\t\"Iban\": \"PT50010101010010101\",\n" +
                "\t\t\t\t\t\"Desc\": \"Conta Serviço DO\"\n" +
                "\t\t\t\t}, {\n" +
                "\t\t\t\t\t\"Id\": \"1111111111111111\",\n" +
                "\t\t\t\t\t\"Desc\": \"Dep. Someone\"\n" +
                "\t\t\t\t}, {\n" +
                "\t\t\t\t\t\"Id\": \"2222222222222\",\n" +
                "\t\t\t\t\t\"Desc\": \"Dep. Someone Fancy\"\n" +
                "\t\t\t\t}],\n" +
                "\t\t\t\t\"Selected\": \"1111111111111111\"\n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\t},\n" +
                "\t\"Body\": {\n" +
                "\t\t\"DataHoje\": \"2019-11-15\",\n" +
                "\t\t\"Movimentos\": [{\n" +
                "\t\t\t\"Saldo\": 2000.0,\n" +
                "\t\t\t\"Tipo\": 0,\n" +
                "\t\t\t\"Descricao\": \"CONSTITUICAO DEP. Someone - NBnet 1234567\",\n" +
                "\t\t\t\"Montante\": 2000.0,\n" +
                "\t\t\t\"DataValor\": \"2016-12-30\",\n" +
                "\t\t\t\"DataOperacao\": \"2016-12-30\",\n" +
                "\t\t\t\"Numero\": 1,\n" +
                "\t\t\t\"NumPedido\": \"1234567\",\n" +
                "\t\t\t\"Categoria\": \"Receitas não classificadas\",\n" +
                "\t\t\t\"IdCategoria\": 354,\n" +
                "\t\t\t\"OfIdN1\": 12,\n" +
                "\t\t\t\"OfIcon\": 16\n" +
                "\t\t}],\n" +
                "\t\t\"Saldo\": {\n" +
                "\t\t\t\"Disponivel\": 2000.0,\n" +
                "\t\t\t\"Cativo\": 0.0,\n" +
                "\t\t\t\"Contabilistico\": 2000.0,\n" +
                "\t\t\t\"Autorizado\": 2000.0,\n" +
                "\t\t\t\"Descoberto\": 0.0,\n" +
                "\t\t\t\"Moeda\": \"EUR\"\n" +
                "\t\t},\n" +
                "\t\t\"Moeda\": \"EUR\",\n" +
                "\t\t\"ComSCA\": false\n" +
                "\t}\n" +
                "}";
    }
}
