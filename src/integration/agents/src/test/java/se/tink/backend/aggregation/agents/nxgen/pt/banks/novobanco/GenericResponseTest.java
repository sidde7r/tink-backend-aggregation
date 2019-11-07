package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.rpc.GenericResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class GenericResponseTest {

    @Test
    public void testSessionExpired() {
        GenericResponse response = SerializationUtils.deserializeFromString(getUnsuccessfulResponse(), GenericResponse.class);
        assertTrue(response.isSessionExpired());
    }

    @Test
    public void testSessionAlive() {
        GenericResponse response = SerializationUtils.deserializeFromString(getSuccessfulResponse(), GenericResponse.class);
        assertFalse (response.isSessionExpired());
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
                "\t\t\"ResponseId\": \"4e93f499bed94cdca38fcae4da894ab7\",\n" +
                "\t\t\"OpToken\": \"5cba54d8e3d14102b2160c5855a4c079\",\n" +
                "\t\t\"Time\": \"2019-11-18T11:10:18.4796648Z\",\n" +
                "\t\t\"SessionTimeout\": 1800,\n" +
                "\t\t\"Status\": {\n" +
                "\t\t\t\"Severidade\": 0,\n" +
                "\t\t\t\"Codigo\": 0\n" +
                "\t\t},\n" +
                "\t\t\"Contexto\": {\n" +
                "\t\t\t\"Contas\": {\n" +
                "\t\t\t\t\"Lista\": [{\n" +
                "\t\t\t\t\t\"Id\": \"2222222222\",\n" +
                "\t\t\t\t\t\"Iban\": \"PT22222222222222222\",\n" +
                "\t\t\t\t\t\"Desc\": \"Conta DO\"\n" +
                "\t\t\t\t}, {\n" +
                "\t\t\t\t\t\"Id\": \"1111111111111111111\",\n" +
                "\t\t\t\t\t\"Iban\": \"PT111111111111111111\",\n" +
                "\t\t\t\t\t\"Desc\": \"Conta Serviço DO\"\n" +
                "\t\t\t\t}, {\n" +
                "\t\t\t\t\t\"Id\": \"33333333333\",\n" +
                "\t\t\t\t\t\"Desc\": \"Dep. Someone\"\n" +
                "\t\t\t\t}, {\n" +
                "\t\t\t\t\t\"Id\": \"444444444444444\",\n" +
                "\t\t\t\t\t\"Desc\": \"Dep. Someone Fancy\"\n" +
                "\t\t\t\t}],\n" +
                "\t\t\t\t\"Selected\": \"2222222222\"\n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\t},\n" +
                "\t\"Body\": {\n" +
                "\t\t\"DataHoje\": \"2019-11-18\",\n" +
                "\t\t\"Movimentos\": [{\n" +
                "\t\t\t\"Saldo\": 650.37,\n" +
                "\t\t\t\"Tipo\": 0,\n" +
                "\t\t\t\"Descricao\": \"TRF ENTREGA PROGRAMADA MENSAL DE 3300 4358 0018\",\n" +
                "\t\t\t\"Montante\": 10.0,\n" +
                "\t\t\t\"DataValor\": \"2019-11-01\",\n" +
                "\t\t\t\"DataOperacao\": \"2019-11-01\",\n" +
                "\t\t\t\"Numero\": 21,\n" +
                "\t\t\t\"Categoria\": \"Intrapatrimónio\",\n" +
                "\t\t\t\"IdCategoria\": 359,\n" +
                "\t\t\t\"OfValor\": 0.0,\n" +
                "\t\t\t\"OfIdN1\": 13,\n" +
                "\t\t\t\"OfIcon\": 13\n" +
                "\t\t}, {\n" +
                "\t\t\t\"Saldo\": 640.37,\n" +
                "\t\t\t\"Tipo\": 0,\n" +
                "\t\t\t\"Descricao\": \"TRF ENTREGA PROGRAMADA MENSAL DE 3300 4358 0018\",\n" +
                "\t\t\t\"Montante\": 10.0,\n" +
                "\t\t\t\"DataValor\": \"2019-10-01\",\n" +
                "\t\t\t\"DataOperacao\": \"2019-10-01\",\n" +
                "\t\t\t\"Numero\": 20,\n" +
                "\t\t\t\"Categoria\": \"Intrapatrimónio\",\n" +
                "\t\t\t\"IdCategoria\": 359,\n" +
                "\t\t\t\"OfIdN1\": 13,\n" +
                "\t\t\t\"OfIcon\": 13\n" +
                "\t\t}, {\n" +
                "\t\t\t\"Saldo\": 630.37,\n" +
                "\t\t\t\"Tipo\": 0,\n" +
                "\t\t\t\"Descricao\": \"TRF ENTREGA PROGRAMADA MENSAL DE 3300 4358 0018\",\n" +
                "\t\t\t\"Montante\": 10.0,\n" +
                "\t\t\t\"DataValor\": \"2019-09-01\",\n" +
                "\t\t\t\"DataOperacao\": \"2019-09-01\",\n" +
                "\t\t\t\"Numero\": 19,\n" +
                "\t\t\t\"Categoria\": \"Intrapatrimónio\",\n" +
                "\t\t\t\"IdCategoria\": 359,\n" +
                "\t\t\t\"OfIdN1\": 13,\n" +
                "\t\t\t\"OfIcon\": 13\n" +
                "\t\t}, {\n" +
                "\t\t\t\"Saldo\": 620.37,\n" +
                "\t\t\t\"Tipo\": 0,\n" +
                "\t\t\t\"Descricao\": \"IMPOSTO RENDIMENTO S/ JUROS CONTA POUPANÇA\",\n" +
                "\t\t\t\"Montante\": -0.08,\n" +
                "\t\t\t\"DataValor\": \"2019-08-29\",\n" +
                "\t\t\t\"DataOperacao\": \"2019-08-29\",\n" +
                "\t\t\t\"Numero\": 18,\n" +
                "\t\t\t\"Categoria\": \"Impostos, Fundos de Pensões, outras Taxas\",\n" +
                "\t\t\t\"IdCategoria\": 346,\n" +
                "\t\t\t\"OfIcon\": 8\n" +
                "\t\t}, {\n" +
                "\t\t\t\"Saldo\": 620.45,\n" +
                "\t\t\t\"Tipo\": 0,\n" +
                "\t\t\t\"Descricao\": \"JUROS 1111111111111 - 01/03/2019 A 28/08/2019 - 0,1000% A)\",\n" +
                "\t\t\t\"Montante\": 0.3,\n" +
                "\t\t\t\"DataValor\": \"2019-08-29\",\n" +
                "\t\t\t\"DataOperacao\": \"2019-08-29\",\n" +
                "\t\t\t\"Numero\": 17,\n" +
                "\t\t\t\"Categoria\": \"Juros e Dividendos\",\n" +
                "\t\t\t\"IdCategoria\": 355,\n" +
                "\t\t\t\"OfIdN1\": 12,\n" +
                "\t\t\t\"OfIcon\": 10\n" +
                "\t\t}, {\n" +
                "\t\t\t\"Saldo\": 620.15,\n" +
                "\t\t\t\"Tipo\": 0,\n" +
                "\t\t\t\"Descricao\": \"TRF ENTREGA PROGRAMADA MENSAL DE 12 34 56\",\n" +
                "\t\t\t\"Montante\": 10.0,\n" +
                "\t\t\t\"DataValor\": \"2019-08-01\",\n" +
                "\t\t\t\"DataOperacao\": \"2019-08-01\",\n" +
                "\t\t\t\"Numero\": 16,\n" +
                "\t\t\t\"Categoria\": \"Intrapatrimónio\",\n" +
                "\t\t\t\"IdCategoria\": 359,\n" +
                "\t\t\t\"OfIdN1\": 13,\n" +
                "\t\t\t\"OfIcon\": 13\n" +
                "\t\t}, {\n" +
                "\t\t\t\"Saldo\": 610.15,\n" +
                "\t\t\t\"Tipo\": 0,\n" +
                "\t\t\t\"Descricao\": \"TRF ENTREGA PROGRAMADA MENSAL DE 12 34 56\",\n" +
                "\t\t\t\"Montante\": 10.0,\n" +
                "\t\t\t\"DataValor\": \"2019-07-01\",\n" +
                "\t\t\t\"DataOperacao\": \"2019-07-01\",\n" +
                "\t\t\t\"Numero\": 15,\n" +
                "\t\t\t\"Categoria\": \"Intrapatrimónio\",\n" +
                "\t\t\t\"IdCategoria\": 359,\n" +
                "\t\t\t\"OfIdN1\": 13,\n" +
                "\t\t\t\"OfIcon\": 13\n" +
                "\t\t}, {\n" +
                "\t\t\t\"Saldo\": 600.15,\n" +
                "\t\t\t\"Tipo\": 0,\n" +
                "\t\t\t\"Descricao\": \"TRF ENTREGA PROGRAMADA MENSAL DE 12 34 56\",\n" +
                "\t\t\t\"Montante\": 10.0,\n" +
                "\t\t\t\"DataValor\": \"2019-06-01\",\n" +
                "\t\t\t\"DataOperacao\": \"2019-06-01\",\n" +
                "\t\t\t\"Numero\": 14,\n" +
                "\t\t\t\"Categoria\": \"Intrapatrimónio\",\n" +
                "\t\t\t\"IdCategoria\": 359,\n" +
                "\t\t\t\"OfIdN1\": 13,\n" +
                "\t\t\t\"OfIcon\": 13\n" +
                "\t\t}, {\n" +
                "\t\t\t\"Saldo\": 590.15,\n" +
                "\t\t\t\"Tipo\": 0,\n" +
                "\t\t\t\"Descricao\": \"TRF ENTREGA PROGRAMADA MENSAL DE 3300 4358 0018\",\n" +
                "\t\t\t\"Montante\": 10.0,\n" +
                "\t\t\t\"DataValor\": \"2019-05-01\",\n" +
                "\t\t\t\"DataOperacao\": \"2019-05-01\",\n" +
                "\t\t\t\"Numero\": 13,\n" +
                "\t\t\t\"Categoria\": \"Intrapatrimónio\",\n" +
                "\t\t\t\"IdCategoria\": 359,\n" +
                "\t\t\t\"OfIdN1\": 13,\n" +
                "\t\t\t\"OfIcon\": 13\n" +
                "\t\t}, {\n" +
                "\t\t\t\"Saldo\": 580.15,\n" +
                "\t\t\t\"Tipo\": 0,\n" +
                "\t\t\t\"Descricao\": \"TRF ENTREGA PROGRAMADA MENSAL DE 12 34 56\",\n" +
                "\t\t\t\"Montante\": 10.0,\n" +
                "\t\t\t\"DataValor\": \"2019-04-01\",\n" +
                "\t\t\t\"DataOperacao\": \"2019-04-01\",\n" +
                "\t\t\t\"Numero\": 12,\n" +
                "\t\t\t\"Categoria\": \"Intrapatrimónio\",\n" +
                "\t\t\t\"IdCategoria\": 359,\n" +
                "\t\t\t\"OfIdN1\": 13,\n" +
                "\t\t\t\"OfIcon\": 13\n" +
                "\t\t}, {\n" +
                "\t\t\t\"Saldo\": 570.15,\n" +
                "\t\t\t\"Tipo\": 0,\n" +
                "\t\t\t\"Descricao\": \"TRF ENTREGA PROGRAMADA MENSAL DE 12 34 56\",\n" +
                "\t\t\t\"Montante\": 10.0,\n" +
                "\t\t\t\"DataValor\": \"2019-03-01\",\n" +
                "\t\t\t\"DataOperacao\": \"2019-03-01\",\n" +
                "\t\t\t\"Numero\": 11,\n" +
                "\t\t\t\"Categoria\": \"Intrapatrimónio\",\n" +
                "\t\t\t\"IdCategoria\": 359,\n" +
                "\t\t\t\"OfIdN1\": 13,\n" +
                "\t\t\t\"OfIcon\": 13\n" +
                "\t\t}, {\n" +
                "\t\t\t\"Saldo\": 560.15,\n" +
                "\t\t\t\"Tipo\": 0,\n" +
                "\t\t\t\"Descricao\": \"IMPOSTO RENDIMENTO S/ JUROS CONTA POUPANÇA\",\n" +
                "\t\t\t\"Montante\": -0.06,\n" +
                "\t\t\t\"DataValor\": \"2019-03-01\",\n" +
                "\t\t\t\"DataOperacao\": \"2019-03-01\",\n" +
                "\t\t\t\"Numero\": 10,\n" +
                "\t\t\t\"Categoria\": \"Impostos, Fundos de Pensões, outras Taxas\",\n" +
                "\t\t\t\"IdCategoria\": 346,\n" +
                "\t\t\t\"OfIcon\": 8\n" +
                "\t\t}, {\n" +
                "\t\t\t\"Saldo\": 560.21,\n" +
                "\t\t\t\"Tipo\": 0,\n" +
                "\t\t\t\"Descricao\": \"JUROS 11111111111 - 29/08/2018 A 28/02/2019 - 0,1000% A)\",\n" +
                "\t\t\t\"Montante\": 0.21,\n" +
                "\t\t\t\"DataValor\": \"2019-03-01\",\n" +
                "\t\t\t\"DataOperacao\": \"2019-03-01\",\n" +
                "\t\t\t\"Numero\": 9,\n" +
                "\t\t\t\"Categoria\": \"Juros e Dividendos\",\n" +
                "\t\t\t\"IdCategoria\": 355,\n" +
                "\t\t\t\"OfIdN1\": 12,\n" +
                "\t\t\t\"OfIcon\": 10\n" +
                "\t\t}, {\n" +
                "\t\t\t\"Saldo\": 560.0,\n" +
                "\t\t\t\"Tipo\": 24,\n" +
                "\t\t\t\"Descricao\": \"DE SOMEONE FANCY\",\n" +
                "\t\t\t\"Montante\": 150.0,\n" +
                "\t\t\t\"DataValor\": \"2019-02-03\",\n" +
                "\t\t\t\"DataOperacao\": \"2019-02-03\",\n" +
                "\t\t\t\"Numero\": 8,\n" +
                "\t\t\t\"Categoria\": \"Intrapatrimónio\",\n" +
                "\t\t\t\"IdCategoria\": 359,\n" +
                "\t\t\t\"OfIdN1\": 13,\n" +
                "\t\t\t\"OfIcon\": 13\n" +
                "\t\t}, {\n" +
                "\t\t\t\"Saldo\": 410.0,\n" +
                "\t\t\t\"Tipo\": 0,\n" +
                "\t\t\t\"Descricao\": \"TRF ENTREGA PROGRAMADA MENSAL DE 12 34 56\",\n" +
                "\t\t\t\"Montante\": 10.0,\n" +
                "\t\t\t\"DataValor\": \"2019-02-01\",\n" +
                "\t\t\t\"DataOperacao\": \"2019-02-01\",\n" +
                "\t\t\t\"Numero\": 7,\n" +
                "\t\t\t\"Categoria\": \"Intrapatrimónio\",\n" +
                "\t\t\t\"IdCategoria\": 359,\n" +
                "\t\t\t\"OfIdN1\": 13,\n" +
                "\t\t\t\"OfIcon\": 13\n" +
                "\t\t}, {\n" +
                "\t\t\t\"Saldo\": 400.0,\n" +
                "\t\t\t\"Tipo\": 0,\n" +
                "\t\t\t\"Descricao\": \"TRF ENTREGA PROGRAMADA MENSAL DE 12 34 56\",\n" +
                "\t\t\t\"Montante\": 10.0,\n" +
                "\t\t\t\"DataValor\": \"2019-01-01\",\n" +
                "\t\t\t\"DataOperacao\": \"2019-01-01\",\n" +
                "\t\t\t\"Numero\": 6,\n" +
                "\t\t\t\"Categoria\": \"Intrapatrimónio\",\n" +
                "\t\t\t\"IdCategoria\": 359,\n" +
                "\t\t\t\"OfIdN1\": 13,\n" +
                "\t\t\t\"OfIcon\": 13\n" +
                "\t\t}, {\n" +
                "\t\t\t\"Saldo\": 390.0,\n" +
                "\t\t\t\"Tipo\": 0,\n" +
                "\t\t\t\"Descricao\": \"TRF ENTREGA PROGRAMADA MENSAL DE 12 34 56\",\n" +
                "\t\t\t\"Montante\": 10.0,\n" +
                "\t\t\t\"DataValor\": \"2018-12-01\",\n" +
                "\t\t\t\"DataOperacao\": \"2018-12-01\",\n" +
                "\t\t\t\"Numero\": 5,\n" +
                "\t\t\t\"Categoria\": \"Intrapatrimónio\",\n" +
                "\t\t\t\"IdCategoria\": 359,\n" +
                "\t\t\t\"OfIdN1\": 13,\n" +
                "\t\t\t\"OfIcon\": 13\n" +
                "\t\t}, {\n" +
                "\t\t\t\"Saldo\": 380.0,\n" +
                "\t\t\t\"Tipo\": 0,\n" +
                "\t\t\t\"Descricao\": \"TRF ENTREGA PROGRAMADA MENSAL DE 12 34 56\",\n" +
                "\t\t\t\"Montante\": 10.0,\n" +
                "\t\t\t\"DataValor\": \"2018-11-01\",\n" +
                "\t\t\t\"DataOperacao\": \"2018-11-01\",\n" +
                "\t\t\t\"Numero\": 4,\n" +
                "\t\t\t\"Categoria\": \"Intrapatrimónio\",\n" +
                "\t\t\t\"IdCategoria\": 359,\n" +
                "\t\t\t\"OfIdN1\": 13,\n" +
                "\t\t\t\"OfIcon\": 13\n" +
                "\t\t}, {\n" +
                "\t\t\t\"Saldo\": 370.0,\n" +
                "\t\t\t\"Tipo\": 0,\n" +
                "\t\t\t\"Descricao\": \"TRF ENTREGA PROGRAMADA MENSAL DE 12 34 56\",\n" +
                "\t\t\t\"Montante\": 10.0,\n" +
                "\t\t\t\"DataValor\": \"2018-10-01\",\n" +
                "\t\t\t\"DataOperacao\": \"2018-10-01\",\n" +
                "\t\t\t\"Numero\": 3,\n" +
                "\t\t\t\"Categoria\": \"Intrapatrimónio\",\n" +
                "\t\t\t\"IdCategoria\": 359,\n" +
                "\t\t\t\"OfIdN1\": 13,\n" +
                "\t\t\t\"OfIcon\": 13\n" +
                "\t\t}, {\n" +
                "\t\t\t\"Saldo\": 360.0,\n" +
                "\t\t\t\"Tipo\": 0,\n" +
                "\t\t\t\"Descricao\": \"TRF ENTREGA PROGRAMADA MENSAL DE 12 34 56\",\n" +
                "\t\t\t\"Montante\": 10.0,\n" +
                "\t\t\t\"DataValor\": \"2018-09-01\",\n" +
                "\t\t\t\"DataOperacao\": \"2018-09-01\",\n" +
                "\t\t\t\"Numero\": 2,\n" +
                "\t\t\t\"Categoria\": \"Intrapatrimónio\",\n" +
                "\t\t\t\"IdCategoria\": 359,\n" +
                "\t\t\t\"OfIdN1\": 13,\n" +
                "\t\t\t\"OfIcon\": 13\n" +
                "\t\t}, {\n" +
                "\t\t\t\"Saldo\": 350.0,\n" +
                "\t\t\t\"Tipo\": 0,\n" +
                "\t\t\t\"Descricao\": \"CONST DEP SOMEONE NBnet 275464657 DE 111111111\",\n" +
                "\t\t\t\"Montante\": 350.0,\n" +
                "\t\t\t\"DataValor\": \"2018-08-29\",\n" +
                "\t\t\t\"DataOperacao\": \"2018-08-28\",\n" +
                "\t\t\t\"Numero\": 1,\n" +
                "\t\t\t\"NumPedido\": \"123456\",\n" +
                "\t\t\t\"Categoria\": \"Poupança e Investimento\",\n" +
                "\t\t\t\"IdCategoria\": 357,\n" +
                "\t\t\t\"OfIdN1\": 14,\n" +
                "\t\t\t\"OfIcon\": 11\n" +
                "\t\t}],\n" +
                "\t\t\"Saldo\": {\n" +
                "\t\t\t\"Disponivel\": 650.37,\n" +
                "\t\t\t\"Cativo\": 0.0,\n" +
                "\t\t\t\"Contabilistico\": 650.37,\n" +
                "\t\t\t\"Autorizado\": 650.37,\n" +
                "\t\t\t\"Descoberto\": 0.0,\n" +
                "\t\t\t\"Moeda\": \"EUR\"\n" +
                "\t\t},\n" +
                "\t\t\"Moeda\": \"EUR\",\n" +
                "\t\t\"ComSCA\": false\n" +
                "\t}\n" +
                "}";
    }
}
