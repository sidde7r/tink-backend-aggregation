package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.component.investment.detail;

import java.util.HashMap;
import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.investment.GetInvestmentsResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class InvestmentTestData {
    public static final String SUCCESSFUL_CALL = "SUCCESSFUL_CALL";
    public static final String FAILED_CALL = "FAILED_CALL";
    public static final Map<String, String> requests = new HashMap<>();

    public static GetInvestmentsResponse getResponse(String payloadLabel) {
        return SerializationUtils.deserializeFromString(
                requests.get(payloadLabel), GetInvestmentsResponse.class);
    }

    static {
        requests.put(
                FAILED_CALL,
                "{\n"
                        + "\t\"Header\": {\n"
                        + "\t\t\"ResponseId\": \"05775047810d431eb105a55d9d3aa109\",\n"
                        + "\t\t\"OpToken\": \"91525891157542f8ab2e9645cc124d34\",\n"
                        + "\t\t\"Time\": \"2019-11-23T22:01:21.049129Z\",\n"
                        + "\t\t\"SessionTimeout\": 1800,\n"
                        + "\t\t\"Status\": {\n"
                        + "\t\t\t\"Mensagem\": \"Não foi possível selecionar conta\",\n"
                        + "\t\t\t\"Severidade\": 3,\n"
                        + "\t\t\t\"Codigo\": 2004\n"
                        + "\t\t},\n"
                        + "\t\t\"Contexto\": {\n"
                        + "\t\t\t\"Contas\": {\n"
                        + "\t\t\t\t\"Lista\": [{\n"
                        + "\t\t\t\t\t\"Id\": \"12345678\",\n"
                        + "\t\t\t\t\t\"Iban\": \"PT5012345678\",\n"
                        + "\t\t\t\t\t\"Desc\": \"Conta DO\"\n"
                        + "\t\t\t\t}],\n"
                        + "\t\t\t\t\"Selected\": \"12345678\"\n"
                        + "\t\t\t}\n"
                        + "\t\t}\n"
                        + "\t},\n"
                        + "\t\"Body\": {},\n"
                        + "\t\"Login\": {}\n"
                        + "}");

        requests.put(
                SUCCESSFUL_CALL,
                "{\n"
                        + "\t\"Header\": {\n"
                        + "\t\t\"ResponseId\": \"50c195f871de4a2b955f2beee0c4a01e\",\n"
                        + "\t\t\"OpToken\": \"4524581eb2f84b59bd44af773d38e058\",\n"
                        + "\t\t\"Time\": \"2019-11-23T22:01:20.7166914Z\",\n"
                        + "\t\t\"SessionTimeout\": 1800,\n"
                        + "\t\t\"Status\": {\n"
                        + "\t\t\t\"Severidade\": 0,\n"
                        + "\t\t\t\"Codigo\": 0\n"
                        + "\t\t},\n"
                        + "\t\t\"Contexto\": {\n"
                        + "\t\t\t\"Contas\": {\n"
                        + "\t\t\t\t\"Lista\": [{\n"
                        + "\t\t\t\t\t\"Id\": \"123456789\",\n"
                        + "\t\t\t\t\t\"Iban\": \"PT500123456789\",\n"
                        + "\t\t\t\t\t\"Desc\": \"Conta DO\"\n"
                        + "\t\t\t\t}],\n"
                        + "\t\t\t\t\"Selected\": \"123456789\"\n"
                        + "\t\t\t}\n"
                        + "\t\t}\n"
                        + "\t},\n"
                        + "\t\"Body\": {\n"
                        + "\t\t\"Carteiras\": [{\n"
                        + "\t\t\t\"DescricaoFundo\": \"NB Capital Plus - OICVM Ab Obrig\",\n"
                        + "\t\t\t\"CodigoFundo\": \"FIMCZE\",\n"
                        + "\t\t\t\"Cotacao\": 7.9394,\n"
                        + "\t\t\t\"QuantidadeTotal\": 31.477,\n"
                        + "\t\t\t\"Valorizacao\": 249.91,\n"
                        + "\t\t\t\"QuantidadeDisponivel\": 31.477,\n"
                        + "\t\t\t\"QuantidadePendenteLiquidacao\": 0.0,\n"
                        + "\t\t\t\"DataCotacao\": \"2019-11-25T00:00:00Z\",\n"
                        + "\t\t\t\"Moeda\": \"EUR\"\n"
                        + "\t\t}],\n"
                        + "\t\t\"Dossiers\": [{\n"
                        + "\t\t\t\"Numero\": \"12345678\",\n"
                        + "\t\t\t\"Nome\": \"Soemone Fancy\"\n"
                        + "\t\t}],\n"
                        + "\t\t\"DossierSelecionado\": \"123456778\",\n"
                        + "\t\t\"ValorTotalDossier\": 249.91,\n"
                        + "\t\t\"Moeda\": \"EUR\"\n"
                        + "\t}\n"
                        + "}");
    }
}
