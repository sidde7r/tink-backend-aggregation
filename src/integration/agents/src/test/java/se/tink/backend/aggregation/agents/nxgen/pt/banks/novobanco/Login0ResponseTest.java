package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.rpc.Login0Response;
import se.tink.libraries.serialization.utils.SerializationUtils;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class Login0ResponseTest {

    @Test
    public void testFailedLogin() {
        Login0Response response = SerializationUtils.deserializeFromString(getFailedLoginResponse(), Login0Response.class);
        assertFalse (response.isValidCredentials());
    }

    @Test
    public void testSuccessfulLogin() {
        Login0Response response = SerializationUtils.deserializeFromString(getSuccessfulLoginResponse(), Login0Response.class);
        assertTrue (response.isValidCredentials());
    }

    private String getFailedLoginResponse() {
        return "{\n" +
                "  \"Header\": {\n" +
                "    \"ResponseId\": \"821079cc56c740fdb445737147c0452a\",\n" +
                "    \"OpToken\": null,\n" +
                "    \"Time\": \"2019-11-07T11:46:25.7849391Z\",\n" +
                "    \"SessionTimeout\": 0,\n" +
                "    \"Status\": {\n" +
                "      \"Mensagem\": \"Não foi possível efectuar o acesso ao serviço. Por favor tente mais tarde.\",\n" +
                "      \"Severidade\": 3,\n" +
                "      \"Codigo\": 50\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }


    private String getSuccessfulLoginResponse() {
        return "{\n" +
                "\t\"Header\": {\n" +
                "\t\t\"ResponseId\": \"7e0238425d264366b84fe41457f01956\",\n" +
                "\t\t\"OpToken\": null,\n" +
                "\t\t\"Time\": \"2019-11-07T08:51:02.4442377Z\",\n" +
                "\t\t\"SessionTimeout\": 0,\n" +
                "\t\t\"Status\": {\n" +
                "\t\t\t\"Severidade\": 0,\n" +
                "\t\t\t\"Codigo\": 0\n" +
                "\t\t}\n" +
                "\t},\n" +
                "\t\"Conteudos\": {\n" +
                "\t\t\"ConteudoTeaser\": {\n" +
                "\t\t\t\"Teaser\": \"<a href=\\\"https://apps.apple.com/pt/app/nb-smart-app/id1011901080\\\">Classifique a NB smart app. Clique aqui.</a>\",\n" +
                "\t\t\t\"Chave\": \"86909666cee143779a996a47ff8b295e\",\n" +
                "\t\t\t\"DataInicio\": \"0001-01-01T00:00:00Z\",\n" +
                "\t\t\t\"DataFim\": \"9999-12-31T23:59:59.9999999Z\"\n" +
                "\t\t},\n" +
                "\t\t\"Campanhas\": [{\n" +
                "\t\t\t\"Campanha\": \"tudoverde\",\n" +
                "\t\t\t\"Img\": \"Img.axd?k=zyuG9j0qMEf3wiJ3ac7bhuc75ByhKd121IM9n006y2s%3d&r=1\",\n" +
                "\t\t\t\"Chave\": \"c281b7add8554490805ee8b87ba1cdf6\",\n" +
                "\t\t\t\"DataInicio\": \"0001-01-01T00:00:00Z\",\n" +
                "\t\t\t\"DataFim\": \"9999-12-31T23:59:59.9999999Z\"\n" +
                "\t\t}],\n" +
                "\t\t\"ConteudoLogout\": {\n" +
                "\t\t\t\"Conteudo\": \"<a href=\\\"http://onelink.to/ts3byu\\\"> <img src=\\\"https://www.novobanco.pt/site/images/crm_mobile/LogOut_AvaliacaoStores.png\\\" alt=\\\"fish\\\" width=\\\"255\\\"></a>\"\n" +
                "\t\t}\n" +
                "\t},\n" +
                "\t\"Body\": {\n" +
                "\t\t\"Session\": {\n" +
                "\t\t\t\"Application\": 1,\n" +
                "\t\t\t\"Media\": 18,\n" +
                "\t\t\t\"AuthCookie\": \"AA9DB35F05622871B3A25293CDB9B2DA5B8EEFFFC8BC79AEB50DE53594ADA8CFCA7E5B95E0D71F5ECFE4A3BAFDF14E462C6B31E6A7747ED799BCDEF98BFFEA3DD7D3D2CC55B2556D636795A5\",\n" +
                "\t\t\t\"SessionCookie\": \"jyamcgqb0j4tpy1ms3dvzm\"\n" +
                "\t\t},\n" +
                "\t\t\"Client\": {\n" +
                "\t\t\t\"IDPN\": \"04311342\",\n" +
                "\t\t\t\"Img\": \"Img.axd?k=IiL9hqlNCwYL8obC1P6BJ5puyAp5FEp2PM0c9Sgi4ag%3d&r=1\",\n" +
                "\t\t\t\"Nome\": \"SOMEONE FANCY\",\n" +
                "\t\t\t\"NomeAbrev\": \"SOMEONE FANCY\",\n" +
                "\t\t\t\"Sexo\": \"Masculino\",\n" +
                "\t\t\t\"Telemovel\": \"1212121212\",\n" +
                "\t\t\t\"Email\": \"someonefancy_tink@fancy_tink.com\",\n" +
                "\t\t\t\"Segmento\": \"3\",\n" +
                "\t\t\t\"Categoria\": \"36\",\n" +
                "\t\t\t\"Adesao\": \"1111111\",\n" +
                "\t\t\t\"UltimoLogin\": \"2019-11-07T08:48:39.453Z\",\n" +
                "\t\t\t\"NumeroMensagensNaoLidas\": 1\n" +
                "\t\t},\n" +
                "\t\t\"Device\": {\n" +
                "\t\t\t\"Id\": \"a1d0c6a741b14eb3a19850035f75e79f\"\n" +
                "\t\t},\n" +
                "\t\t\"Env\": {\n" +
                "\t\t\t\"MenuVersion\": \"{418d57ae-203d-47eb-be94-028638d4a387}\",\n" +
                "\t\t\t\"FavVersion\": \"637087134634738701\",\n" +
                "\t\t\t\"Language\": \"PT\",\n" +
                "\t\t\t\"Menu\": {\n" +
                "\t\t\t\t\"Itens\": [{\n" +
                "\t\t\t\t\t\"Tipo\": 1,\n" +
                "\t\t\t\t\t\"Id\": \"6000\",\n" +
                "\t\t\t\t\t\"Descricao\": \"Consultas\",\n" +
                "\t\t\t\t\t\"Filhos\": [{\n" +
                "\t\t\t\t\t\t\"Id\": \"194\",\n" +
                "\t\t\t\t\t\t\"Descricao\": \"Posição Integrada\"\n" +
                "\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\"Id\": \"3396\",\n" +
                "\t\t\t\t\t\t\"Descricao\": \"Saldos e Movimentos\"\n" +
                "\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\"Id\": \"6080\",\n" +
                "\t\t\t\t\t\t\"Descricao\": \"Extratos\",\n" +
                "\t\t\t\t\t\t\"Filhos\": [{\n" +
                "\t\t\t\t\t\t\t\"Id\": \"6079\",\n" +
                "\t\t\t\t\t\t\t\"Descricao\": \"Extratos de Conta\"\n" +
                "\t\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\t\"Id\": \"1443\",\n" +
                "\t\t\t\t\t\t\t\"Descricao\": \"Extratos do Cartão de Crédito\"\n" +
                "\t\t\t\t\t\t}]\n" +
                "\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\"Id\": \"6085\",\n" +
                "\t\t\t\t\t\t\"Descricao\": \"Débitos Diretos\",\n" +
                "\t\t\t\t\t\t\"Filhos\": [{\n" +
                "\t\t\t\t\t\t\t\"Id\": \"1648\",\n" +
                "\t\t\t\t\t\t\t\"Descricao\": \"Autorizações\"\n" +
                "\t\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\t\"Id\": \"1716\",\n" +
                "\t\t\t\t\t\t\t\"Descricao\": \"Movimentos \"\n" +
                "\t\t\t\t\t\t}]\n" +
                "\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\"Id\": \"40\",\n" +
                "\t\t\t\t\t\t\"Descricao\": \"NIB, IBAN e SWIFT\"\n" +
                "\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\"Id\": \"874\",\n" +
                "\t\t\t\t\t\t\"Descricao\": \"Pagamentos de Baixo Valor\"\n" +
                "\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\"Id\": \"2540\",\n" +
                "\t\t\t\t\t\t\"Descricao\": \"Assinatura de Documentos\"\n" +
                "\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\"Id\": \"25\",\n" +
                "\t\t\t\t\t\t\"Descricao\": \"Histórico de Operações\"\n" +
                "\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\"Id\": \"176\",\n" +
                "\t\t\t\t\t\t\"Descricao\": \"Operações Agendadas\"\n" +
                "\t\t\t\t\t}]\n" +
                "\t\t\t\t}, {\n" +
                "\t\t\t\t\t\"Tipo\": 1,\n" +
                "\t\t\t\t\t\"Id\": \"6028\",\n" +
                "\t\t\t\t\t\"Descricao\": \"Poupanças\",\n" +
                "\t\t\t\t\t\"Filhos\": [{\n" +
                "\t\t\t\t\t\t\"Id\": \"5955\",\n" +
                "\t\t\t\t\t\t\"Descricao\": \"Constituição de Poupanças\"\n" +
                "\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\"Id\": \"158\",\n" +
                "\t\t\t\t\t\t\"Descricao\": \"Dossier de Fundos\",\n" +
                "\t\t\t\t\t\t\"Novo\": true\n" +
                "\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\"Id\": \"6062\",\n" +
                "\t\t\t\t\t\t\"Descricao\": \"Poupança por Objetivos\"\n" +
                "\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\"Id\": \"2559\",\n" +
                "\t\t\t\t\t\t\"Descricao\": \"Gestão de Poupanças\"\n" +
                "\t\t\t\t\t}]\n" +
                "\t\t\t\t}, {\n" +
                "\t\t\t\t\t\"Tipo\": 1,\n" +
                "\t\t\t\t\t\"Id\": \"6090\",\n" +
                "\t\t\t\t\t\"Descricao\": \"Orçamento Familiar\",\n" +
                "\t\t\t\t\t\"Filhos\": [{\n" +
                "\t\t\t\t\t\t\"Id\": \"6093\",\n" +
                "\t\t\t\t\t\t\"Descricao\": \"O meu Orçamento\"\n" +
                "\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\"Id\": \"6091\",\n" +
                "\t\t\t\t\t\t\"Descricao\": \"Lista de Movimentos\"\n" +
                "\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\"Id\": \"6092\",\n" +
                "\t\t\t\t\t\t\"Descricao\": \"Movimentos não Classificados\"\n" +
                "\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\"Id\": \"6094\",\n" +
                "\t\t\t\t\t\t\"Descricao\": \"Ativar / Desativar\"\n" +
                "\t\t\t\t\t}]\n" +
                "\t\t\t\t}, {\n" +
                "\t\t\t\t\t\"Tipo\": 1,\n" +
                "\t\t\t\t\t\"Id\": \"6001\",\n" +
                "\t\t\t\t\t\"Descricao\": \"Cartões\",\n" +
                "\t\t\t\t\t\"Filhos\": [{\n" +
                "\t\t\t\t\t\t\"Id\": \"3393\",\n" +
                "\t\t\t\t\t\t\"Descricao\": \"Saldos de Cartões\"\n" +
                "\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\"Id\": \"2048\",\n" +
                "\t\t\t\t\t\t\"Descricao\": \"Movimentos Cartões\"\n" +
                "\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\"Id\": \"405\",\n" +
                "\t\t\t\t\t\t\"Descricao\": \"Cash-Advance\"\n" +
                "\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\"Id\": \"563\",\n" +
                "\t\t\t\t\t\t\"Descricao\": \"Pagamentos a Prestações\"\n" +
                "\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\"Id\": \"6081\",\n" +
                "\t\t\t\t\t\t\"Descricao\": \"Pagar Cartão de Crédito\"\n" +
                "\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\"Id\": \"6033\",\n" +
                "\t\t\t\t\t\t\"Descricao\": \"Cartão Virtual MB NET\",\n" +
                "\t\t\t\t\t\t\"Filhos\": [{\n" +
                "\t\t\t\t\t\t\t\"Id\": \"6026\",\n" +
                "\t\t\t\t\t\t\t\"Descricao\": \"Cartão Virtual MB NET\"\n" +
                "\t\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\t\"Id\": \"6027\",\n" +
                "\t\t\t\t\t\t\t\"Descricao\": \"Gestão de Cartões Virtuais\"\n" +
                "\t\t\t\t\t\t}]\n" +
                "\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\"Id\": \"3471\",\n" +
                "\t\t\t\t\t\t\"Descricao\": \"3D Secure - Compras Online\"\n" +
                "\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\"Tipo\": 2,\n" +
                "\t\t\t\t\t\t\"Descricao\": \"Cancelar Cartão\",\n" +
                "\t\t\t\t\t\t\"Url\": \"https://www.novobanco.pt/site/cms.aspx?labelid=cancelamentocartoesmobile\"\n" +
                "\t\t\t\t\t}]\n" +
                "\t\t\t\t}, {\n" +
                "\t\t\t\t\t\"Tipo\": 1,\n" +
                "\t\t\t\t\t\"Id\": \"3459\",\n" +
                "\t\t\t\t\t\"Descricao\": \"Transferências\",\n" +
                "\t\t\t\t\t\"Filhos\": [{\n" +
                "\t\t\t\t\t\t\"Id\": \"6070\",\n" +
                "\t\t\t\t\t\t\"Descricao\": \"Executar Transferência\",\n" +
                "\t\t\t\t\t\t\"Filhos\": [{\n" +
                "\t\t\t\t\t\t\t\"Id\": \"3029\",\n" +
                "\t\t\t\t\t\t\t\"Descricao\": \"Nacional ou Zona Euro\"\n" +
                "\t\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\t\"Id\": \"3526\",\n" +
                "\t\t\t\t\t\t\t\"Descricao\": \"Entre Contas Próprias\"\n" +
                "\t\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\t\"Id\": \"6072\",\n" +
                "\t\t\t\t\t\t\t\"Descricao\": \"Para Beneficiário\"\n" +
                "\t\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\t\"Id\": \"3731\",\n" +
                "\t\t\t\t\t\t\t\"Descricao\": \"Para Telemóvel\"\n" +
                "\t\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\t\"Id\": \"6071\",\n" +
                "\t\t\t\t\t\t\t\"Descricao\": \"Reutilizar Transferência\"\n" +
                "\t\t\t\t\t\t}]\n" +
                "\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\"Id\": \"6097\",\n" +
                "\t\t\t\t\t\t\"Descricao\": \"Ordens Permanentes\",\n" +
                "\t\t\t\t\t\t\"Filhos\": [{\n" +
                "\t\t\t\t\t\t\t\"Id\": \"219\",\n" +
                "\t\t\t\t\t\t\t\"Descricao\": \"Criar Ordens Permanentes\"\n" +
                "\t\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\t\"Id\": \"218\",\n" +
                "\t\t\t\t\t\t\t\"Descricao\": \"Gestão de Ordens Permanentes\"\n" +
                "\t\t\t\t\t\t}]\n" +
                "\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\"Tipo\": 2,\n" +
                "\t\t\t\t\t\t\"Descricao\": \"NBChatPay\",\n" +
                "\t\t\t\t\t\t\"Url\": \"https://www.novobanco.pt/site/cms.aspx?labelid=nbchatpay_mobile\"\n" +
                "\t\t\t\t\t}]\n" +
                "\t\t\t\t}, {\n" +
                "\t\t\t\t\t\"Tipo\": 1,\n" +
                "\t\t\t\t\t\"Id\": \"3458\",\n" +
                "\t\t\t\t\t\"Descricao\": \"Pagamentos\",\n" +
                "\t\t\t\t\t\"Filhos\": [{\n" +
                "\t\t\t\t\t\t\"Id\": \"6073\",\n" +
                "\t\t\t\t\t\t\"Descricao\": \"Serviços ou Compras\",\n" +
                "\t\t\t\t\t\t\"Filhos\": [{\n" +
                "\t\t\t\t\t\t\t\"Id\": \"11\",\n" +
                "\t\t\t\t\t\t\t\"Descricao\": \"Pagamento de Serviços\"\n" +
                "\t\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\t\"Id\": \"482\",\n" +
                "\t\t\t\t\t\t\t\"Descricao\": \"Pagamento de Compras\"\n" +
                "\t\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\t\"Id\": \"6074\",\n" +
                "\t\t\t\t\t\t\t\"Descricao\": \"Reutilizar Pagamento\"\n" +
                "\t\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\t\"Id\": \"6075\",\n" +
                "\t\t\t\t\t\t\t\"Descricao\": \"Pagamento PhotoScan\"\n" +
                "\t\t\t\t\t\t}]\n" +
                "\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\"Id\": \"6041\",\n" +
                "\t\t\t\t\t\t\"Descricao\": \"Carregamentos\",\n" +
                "\t\t\t\t\t\t\"Filhos\": [{\n" +
                "\t\t\t\t\t\t\t\"Id\": \"6036\",\n" +
                "\t\t\t\t\t\t\t\"Descricao\": \"Novo Carregamento\"\n" +
                "\t\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\t\"Id\": \"6076\",\n" +
                "\t\t\t\t\t\t\t\"Descricao\": \"Reutilizar Carregamento\"\n" +
                "\t\t\t\t\t\t}]\n" +
                "\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\"Id\": \"6077\",\n" +
                "\t\t\t\t\t\t\"Descricao\": \"Estado e Setor Público\",\n" +
                "\t\t\t\t\t\t\"Filhos\": [{\n" +
                "\t\t\t\t\t\t\t\"Id\": \"668\",\n" +
                "\t\t\t\t\t\t\t\"Descricao\": \"Pagamento ao Estado\"\n" +
                "\t\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\t\"Id\": \"995\",\n" +
                "\t\t\t\t\t\t\t\"Descricao\": \"Pagamento da Seg. Social\"\n" +
                "\t\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\t\"Id\": \"460\",\n" +
                "\t\t\t\t\t\t\t\"Descricao\": \"Pagamento da TSU\"\n" +
                "\t\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\t\"Id\": \"6087\",\n" +
                "\t\t\t\t\t\t\t\"Descricao\": \"Reutilizar Pagamento\"\n" +
                "\t\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\t\"Id\": \"6078\",\n" +
                "\t\t\t\t\t\t\t\"Descricao\": \"Pagamento PhotoScan\"\n" +
                "\t\t\t\t\t\t}]\n" +
                "\t\t\t\t\t}]\n" +
                "\t\t\t\t}, {\n" +
                "\t\t\t\t\t\"Tipo\": 1,\n" +
                "\t\t\t\t\t\"Id\": \"6018\",\n" +
                "\t\t\t\t\t\"Descricao\": \"Ações e Mercados\",\n" +
                "\t\t\t\t\t\"Filhos\": [{\n" +
                "\t\t\t\t\t\t\"Id\": \"3467\",\n" +
                "\t\t\t\t\t\t\"Descricao\": \"Bolsa\",\n" +
                "\t\t\t\t\t\t\"Filhos\": [{\n" +
                "\t\t\t\t\t\t\t\"Id\": \"1759\",\n" +
                "\t\t\t\t\t\t\t\"Descricao\": \"Informação de Mercados\"\n" +
                "\t\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\t\"Id\": \"1517\",\n" +
                "\t\t\t\t\t\t\t\"Descricao\": \"Consulta de Carteira\"\n" +
                "\t\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\t\"Id\": \"1647\",\n" +
                "\t\t\t\t\t\t\t\"Descricao\": \"Ordens de bolsa\"\n" +
                "\t\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\t\"Id\": \"61\",\n" +
                "\t\t\t\t\t\t\t\"Descricao\": \"Ordens ativas\"\n" +
                "\t\t\t\t\t\t}]\n" +
                "\t\t\t\t\t}]\n" +
                "\t\t\t\t}, {\n" +
                "\t\t\t\t\t\"Tipo\": 1,\n" +
                "\t\t\t\t\t\"Id\": \"6046\",\n" +
                "\t\t\t\t\t\"Descricao\": \"Crédito\",\n" +
                "\t\t\t\t\t\"Filhos\": [{\n" +
                "\t\t\t\t\t\t\"Id\": \"3618\",\n" +
                "\t\t\t\t\t\t\"Descricao\": \"Solução NB Ordenado\"\n" +
                "\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\"Id\": \"3052\",\n" +
                "\t\t\t\t\t\t\"Descricao\": \"O meu Crédito Habitação\"\n" +
                "\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\"Id\": \"3053\",\n" +
                "\t\t\t\t\t\t\"Descricao\": \"O meu Crédito Pessoal\"\n" +
                "\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\"Descricao\": \"Simuladores\",\n" +
                "\t\t\t\t\t\t\"Filhos\": [{\n" +
                "\t\t\t\t\t\t\t\"Tipo\": 2,\n" +
                "\t\t\t\t\t\t\t\"Id\": \"6054\",\n" +
                "\t\t\t\t\t\t\t\"Descricao\": \"Crédito Pessoal\",\n" +
                "\t\t\t\t\t\t\t\"Url\": \"https://www.novobanco.pt/site/cms.aspx?labelid=pedir_credito_pessoal_nbapp&source=nbsmartapp\"\n" +
                "\t\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\t\"Tipo\": 2,\n" +
                "\t\t\t\t\t\t\t\"Id\": \"6054\",\n" +
                "\t\t\t\t\t\t\t\"Descricao\": \"Crédito Habitação\",\n" +
                "\t\t\t\t\t\t\t\"Url\": \"https://cliente.novobanco.pt/simulador-credito-habitacao/?utm_source=novobanco&utm_medium=mobile&utm_campaign=1906.ch.na.na&utm_content=opcao_menu.generico.na\"\n" +
                "\t\t\t\t\t\t}]\n" +
                "\t\t\t\t\t}]\n" +
                "\t\t\t\t}, {\n" +
                "\t\t\t\t\t\"Tipo\": 1,\n" +
                "\t\t\t\t\t\"Id\": \"3466\",\n" +
                "\t\t\t\t\t\"Descricao\": \"Outras Opções\",\n" +
                "\t\t\t\t\t\"Filhos\": [{\n" +
                "\t\t\t\t\t\t\"Id\": \"6262\",\n" +
                "\t\t\t\t\t\t\"Descricao\": \"Definições\"\n" +
                "\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\"Id\": \"7021\",\n" +
                "\t\t\t\t\t\t\"Descricao\": \"Deixar o Cartão Matriz em Casa\",\n" +
                "\t\t\t\t\t\t\"Novo\": true\n" +
                "\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\"Id\": \"1774\",\n" +
                "\t\t\t\t\t\t\"Descricao\": \"Ativação de opções MBWAY\",\n" +
                "\t\t\t\t\t\t\"Novo\": true\n" +
                "\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\"Id\": \"6029\",\n" +
                "\t\t\t\t\t\t\"Descricao\": \"Notificações\",\n" +
                "\t\t\t\t\t\t\"Filhos\": [{\n" +
                "\t\t\t\t\t\t\t\"Id\": \"2193\",\n" +
                "\t\t\t\t\t\t\t\"Descricao\": \"As minhas notificações\"\n" +
                "\t\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\t\"Id\": \"2209\",\n" +
                "\t\t\t\t\t\t\t\"Descricao\": \"Criar notificação\"\n" +
                "\t\t\t\t\t\t}]\n" +
                "\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\"Id\": \"142\",\n" +
                "\t\t\t\t\t\t\"Descricao\": \"Gestão de Beneficiários\"\n" +
                "\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\"Id\": \"3756\",\n" +
                "\t\t\t\t\t\t\"Descricao\": \"Operações Certificadas\"\n" +
                "\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\"Id\": \"6088\",\n" +
                "\t\t\t\t\t\t\"Descricao\": \"Gestão de Privacidade\"\n" +
                "\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\"Id\": \"7014\",\n" +
                "\t\t\t\t\t\t\"Descricao\": \"Ativar Cartão Matriz\"\n" +
                "\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\"Id\": \"6054\",\n" +
                "\t\t\t\t\t\t\"Descricao\": \"Contactos NB\",\n" +
                "\t\t\t\t\t\t\"Filhos\": [{\n" +
                "\t\t\t\t\t\t\t\"Tipo\": 2,\n" +
                "\t\t\t\t\t\t\t\"Id\": \"3468\",\n" +
                "\t\t\t\t\t\t\t\"Descricao\": \"Localizar  Agências e ATMs\",\n" +
                "\t\t\t\t\t\t\t\"Url\": \"https://novobanco.pt/agenciasmobile\"\n" +
                "\t\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\t\"Id\": \"662\",\n" +
                "\t\t\t\t\t\t\t\"Descricao\": \"Gestor\"\n" +
                "\t\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\t\"Id\": \"3074\",\n" +
                "\t\t\t\t\t\t\t\"Descricao\": \"Pedir Contacto/Reunião\"\n" +
                "\t\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\t\"Tipo\": 2,\n" +
                "\t\t\t\t\t\t\t\"Id\": \"6054\",\n" +
                "\t\t\t\t\t\t\t\"Descricao\": \"Contactos Gerais NB\",\n" +
                "\t\t\t\t\t\t\t\"Url\": \"https://www.novobanco.pt/SITE/cms.aspx?labelid=contactosnbapp\"\n" +
                "\t\t\t\t\t\t}]\n" +
                "\t\t\t\t\t}, {\n" +
                "\t\t\t\t\t\t\"Id\": \"6086\",\n" +
                "\t\t\t\t\t\t\"Descricao\": \"Sessão Partilhada\"\n" +
                "\t\t\t\t\t}]\n" +
                "\t\t\t\t}, {\n" +
                "\t\t\t\t\t\"Tipo\": 1,\n" +
                "\t\t\t\t\t\"Id\": \"3462\",\n" +
                "\t\t\t\t\t\"Descricao\": \"Outras Aplicações\",\n" +
                "\t\t\t\t\t\"Filhos\": [{\n" +
                "\t\t\t\t\t\t\"Tipo\": 4,\n" +
                "\t\t\t\t\t\t\"Id\": \"3464\",\n" +
                "\t\t\t\t\t\t\"Descricao\": \"NB1Click\"\n" +
                "\t\t\t\t\t}]\n" +
                "\t\t\t\t}],\n" +
                "\t\t\t\t\"WelcomeId\": \"3398\"\n" +
                "\t\t\t},\n" +
                "\t\t\t\"Favoritos\": [{\n" +
                "\t\t\t\t\"IdServico\": \"3396\",\n" +
                "\t\t\t\t\"Nome\": \"Saldos e Movimentos\"\n" +
                "\t\t\t}]\n" +
                "\t\t},\n" +
                "\t\t\"Configuration\": {\n" +
                "\t\t\t\"toBackGround\": 30,\n" +
                "\t\t\t\"toWeb\": 60\n" +
                "\t\t},\n" +
                "\t\t\"RememberMeToken\": \"15316857577f4d3bbca5e730522222\",\n" +
                "\t\t\"EstadoNotificacao\": {\n" +
                "\t\t\t\"Ativo\": true,\n" +
                "\t\t\t\"DeviceName\": \"Redmi\",\n" +
                "\t\t\t\"DeviceModel\": \"Xiaomi Redmi 4X\",\n" +
                "\t\t\t\"DeviceId\": \"805139dc90244a5c9e61084a5cb6abcd\"\n" +
                "\t\t},\n" +
                "\t\t\"OfAtivo\": true\n" +
                "\t}\n" +
                "}";
    }
}
