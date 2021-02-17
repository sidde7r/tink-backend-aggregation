package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.component.investment.detail;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.detail.InvestmentAccountDto;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.investment.GetInvestmentsResponse;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class InvestmentTestData {
    public static final String INVESTMENTS_AVAILABLE = "INVESTMENTS_AVAILABLE";
    public static final String FAILED_CALL = "FAILED_CALL";

    public static final String INV_UNIQUE_ID = "12345678";
    public static final String INV_ACCOUNT_ID = "123456789999";
    public static final String INV_ACCOUNT_DESCR = "Conta DO";
    public static final String INV_BALANCE = "249.91";
    public static final String INV_CURRENCY = "EUR";

    public static final String PORTFOLIO_ID = "FIMCZE";
    public static final String PORTFOLIO_TOTAL_VALUE = "31.477";
    public static final String PORTFOLIO_TOTAL_PROFIT = "249.91";
    public static final String PORTFOLIO_CASH_VALUE = "7.9394";

    public static final Map<String, String> requests = new HashMap<>();
    private static final Map<String, InvestmentAccountDto> referenceInvestmentAccountDtos =
            new HashMap<>();

    public static GetInvestmentsResponse getResponse(String payloadLabel) {
        return SerializationUtils.deserializeFromString(
                requests.get(payloadLabel), GetInvestmentsResponse.class);
    }

    public static InvestmentAccountDto getReferenceAccountDto(String id) {
        return referenceInvestmentAccountDtos.get(id);
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
                INVESTMENTS_AVAILABLE,
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
                        + "          \"Id\": \""
                        + INV_ACCOUNT_ID
                        + "\",\n"
                        + "          \"Iban\": \"PT500123456789999\",\n"
                        + "          \"Desc\": \""
                        + INV_ACCOUNT_DESCR
                        + "\"\n"
                        + "        }],\n"
                        + "        \"Selected\": \""
                        + INV_ACCOUNT_ID
                        + "\"\n"
                        + "      }\n"
                        + "    }\n"
                        + "  },\n"
                        + "  \"Body\": {\n"
                        + "    \"Carteiras\": [{\n"
                        + "      \"DescricaoFundo\": \"NB Capital Plus - OICVM Ab Obrig\",\n"
                        + "      \"CodigoFundo\": \""
                        + PORTFOLIO_ID
                        + "\",\n"
                        + "      \"Cotacao\": "
                        + PORTFOLIO_CASH_VALUE
                        + ",\n"
                        + "      \"QuantidadeTotal\": "
                        + PORTFOLIO_TOTAL_VALUE
                        + ",\n"
                        + "      \"Valorizacao\": "
                        + PORTFOLIO_TOTAL_PROFIT
                        + ",\n"
                        + "      \"QuantidadeDisponivel\": 31.477,\n"
                        + "      \"QuantidadePendenteLiquidacao\": 0.0,\n"
                        + "      \"DataCotacao\": \"2019-11-25T00:00:00Z\",\n"
                        + "      \"Moeda\": \"EUR\"\n"
                        + "    }],\n"
                        + "    \"Dossiers\": [{\n"
                        + "      \"Numero\": \""
                        + INV_UNIQUE_ID
                        + "\",\n"
                        + "      \"Nome\": \"Soemone Fancy\"\n"
                        + "    }],\n"
                        + "    \"DossierSelecionado\": \""
                        + INV_UNIQUE_ID
                        + "\",\n"
                        + "    \"ValorTotalDossier\": "
                        + INV_BALANCE
                        + ",\n"
                        + "    \"Moeda\": \""
                        + INV_CURRENCY
                        + "\"\n"
                        + "  }\n"
                        + "}");
    }

    static {
        referenceInvestmentAccountDtos.put(
                INV_ACCOUNT_ID,
                new InvestmentAccountDto(
                        INV_ACCOUNT_ID,
                        INV_UNIQUE_ID,
                        INV_ACCOUNT_DESCR,
                        ExactCurrencyAmount.of(INV_BALANCE, INV_CURRENCY),
                        Collections.singletonList(
                                createPortfolioDto(
                                        PORTFOLIO_ID,
                                        PORTFOLIO_CASH_VALUE,
                                        PORTFOLIO_TOTAL_PROFIT,
                                        PORTFOLIO_TOTAL_VALUE))));
    }

    private static InvestmentAccountDto.PortfolioDto createPortfolioDto(
            String uniqueIdentifier, String cashValue, String totalProfit, String totalValue) {
        return new InvestmentAccountDto.PortfolioDto(
                PORTFOLIO_ID,
                new BigDecimal(PORTFOLIO_CASH_VALUE),
                new BigDecimal(PORTFOLIO_TOTAL_PROFIT),
                new BigDecimal(PORTFOLIO_TOTAL_VALUE));
    }
}
