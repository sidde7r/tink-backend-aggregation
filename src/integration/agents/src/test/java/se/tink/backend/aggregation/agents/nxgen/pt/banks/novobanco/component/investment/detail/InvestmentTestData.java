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
                        + "  \"Header\": {\n"
                        + "    \"ResponseId\": \"05775047810d431eb105a55d9d3aa109\",\n"
                        + "    \"OpToken\": \"91525891157542f8ab2e9645cc124d34\",\n"
                        + "    \"Time\": \"2019-11-23T22:01:21.049129Z\",\n"
                        + "    \"SessionTimeout\": 1800,\n"
                        + "    \"Status\": {\n"
                        + "      \"Mensagem\": \"Não foi possível selecionar conta\",\n"
                        + "      \"Severidade\": 3,\n"
                        + "      \"Codigo\": 2004\n"
                        + "    },\n"
                        + "    \"Contexto\": {\n"
                        + "      \"Contas\": {\n"
                        + "        \"Lista\": [{\n"
                        + "          \"Id\": \"12345678\",\n"
                        + "          \"Iban\": \"PT5012345678\",\n"
                        + "          \"Desc\": \"Conta DO\"\n"
                        + "        }],\n"
                        + "        \"Selected\": \"12345678\"\n"
                        + "      }\n"
                        + "    }\n"
                        + "  },\n"
                        + "  \"Body\": {},\n"
                        + "  \"Login\": {}\n"
                        + "}");

        requests.put(
                SUCCESSFUL_CALL,
                "{\n"
                        + "  \"Header\": {\n"
                        + "    \"ResponseId\": \"50c195f871de4a2b955f2beee0c4a01e\",\n"
                        + "    \"OpToken\": \"4524581eb2f84b59bd44af773d38e058\",\n"
                        + "    \"Time\": \"2019-11-23T22:01:20.7166914Z\",\n"
                        + "    \"SessionTimeout\": 1800,\n"
                        + "    \"Status\": {\n"
                        + "      \"Severidade\": 0,\n"
                        + "      \"Codigo\": 0\n"
                        + "    },\n"
                        + "    \"Contexto\": {\n"
                        + "      \"Contas\": {\n"
                        + "        \"Lista\": [{\n"
                        + "          \"Id\": \"123456789\",\n"
                        + "          \"Iban\": \"PT500123456789\",\n"
                        + "          \"Desc\": \"Conta DO\"\n"
                        + "        }],\n"
                        + "        \"Selected\": \"123456789\"\n"
                        + "      }\n"
                        + "    }\n"
                        + "  },\n"
                        + "  \"Body\": {\n"
                        + "    \"Carteiras\": [{\n"
                        + "      \"DescricaoFundo\": \"NB Capital Plus - OICVM Ab Obrig\",\n"
                        + "      \"CodigoFundo\": \"FIMCZE\",\n"
                        + "      \"Cotacao\": 7.9394,\n"
                        + "      \"QuantidadeTotal\": 31.477,\n"
                        + "      \"Valorizacao\": 249.91,\n"
                        + "      \"QuantidadeDisponivel\": 31.477,\n"
                        + "      \"QuantidadePendenteLiquidacao\": 0.0,\n"
                        + "      \"DataCotacao\": \"2019-11-25T00:00:00Z\",\n"
                        + "      \"Moeda\": \"EUR\"\n"
                        + "    }],\n"
                        + "    \"Dossiers\": [{\n"
                        + "      \"Numero\": \"12345678\",\n"
                        + "      \"Nome\": \"Soemone Fancy\"\n"
                        + "    }],\n"
                        + "    \"DossierSelecionado\": \"123456778\",\n"
                        + "    \"ValorTotalDossier\": 249.91,\n"
                        + "    \"Moeda\": \"EUR\"\n"
                        + "  }\n"
                        + "}");
    }
}
