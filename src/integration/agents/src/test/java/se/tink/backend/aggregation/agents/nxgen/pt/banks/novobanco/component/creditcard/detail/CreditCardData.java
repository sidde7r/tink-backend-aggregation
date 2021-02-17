package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.component.creditcard.detail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.assertj.core.util.Lists;
import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.response.AccountDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.detail.CreditCardDto;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.creditcard.GetCreditCardDetailsResponse;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class CreditCardData {
    public static final String PAYLOAD_ACCOUNT_ID_1 = "330000123456";
    public static final String PAYLOAD_ACCOUNT_ID_2 = "1005967000002";

    public static final String CARD_1_ID = "4035411234";
    public static final String CARD_1_NUMBER = "4035 41** **** 1234";
    public static final String CARD_1_BALANCE = "500.00";
    public static final String CARD_1_AVAILABLE_CREDIT = "500.00";
    public static final String CARD_1_CURRENCY = "EUR";
    public static final String ACCOUNT_1_DESCR = "Conta DO";

    private static Map<String, String> creditCardPayload = new HashMap<>();
    private static Collection<CreditCardDto> referenceCreditCardDtos = new ArrayList<>();

    public static Collection<AccountDetailsEntity> getAccounts() {
        GetAccountsResponse response =
                SerializationUtils.deserializeFromString(
                        getAccountsResponse(), GetAccountsResponse.class);

        return Optional.of(response.getAccountDetailsEntities())
                .map(Collection::stream)
                .orElse(Stream.empty())
                .collect(Collectors.toList());
    }

    public static Collection<GetCreditCardDetailsResponse> getCreditCards() {
        return Lists.newArrayList(
                createGetCreditCardDetailsResponse(PAYLOAD_ACCOUNT_ID_1),
                createGetCreditCardDetailsResponse(PAYLOAD_ACCOUNT_ID_2));
    }

    public static Collection<CreditCardDto> getReferenceCreditCardDtos() {
        return referenceCreditCardDtos;
    }

    private static GetCreditCardDetailsResponse createGetCreditCardDetailsResponse(
            String payloadId) {
        return SerializationUtils.deserializeFromString(
                creditCardPayload.get(payloadId), GetCreditCardDetailsResponse.class);
    }

    private static String getAccountsResponse() {
        return "{\n"
                + "  \"Header\": {\n"
                + "    \"ResponseId\": \"508a719f7c994e4f96d479b76ebd7bf3\",\n"
                + "    \"OpToken\": \"f83d3575887b46f18e339c83303a93d1\",\n"
                + "    \"Time\": \"2019-11-15T12:58:15.0733009Z\",\n"
                + "    \"SessionTimeout\": 1800,\n"
                + "    \"Status\": {\n"
                + "      \"Severidade\": 0,\n"
                + "      \"Codigo\": 0\n"
                + "    },\n"
                + "    \"Contexto\": {\n"
                + "      \"Contas\": {\n"
                + "        \"Lista\": [{\n"
                + "          \"Id\": \""
                + PAYLOAD_ACCOUNT_ID_1
                + "\",\n"
                + "          \"Iban\": \"PT50000201231234567890154\",\n"
                + "          \"Desc\": \"Conta DO\"\n"
                + "        }, {\n"
                + "          \"Id\": \""
                + PAYLOAD_ACCOUNT_ID_2
                + "\",\n"
                + "          \"Iban\": \"PT50000201231234567890154\",\n"
                + "          \"Desc\": \"Conta Serviço DO\"\n"
                + "        }],\n"
                + "        \"Selected\": \""
                + PAYLOAD_ACCOUNT_ID_1
                + "\"\n"
                + "      }\n"
                + "    }\n"
                + "  },\n"
                + "  \"Body\": {\n"
                + "    \"DataHoje\": \"2019-11-15\",\n"
                + "    \"Movimentos\": null,\n"
                + "    \"Saldo\": {\n"
                + "      \"Disponivel\": 650.37,\n"
                + "      \"Cativo\": 0.0,\n"
                + "      \"Contabilistico\": 650.37,\n"
                + "      \"Autorizado\": 650.37,\n"
                + "      \"Descoberto\": 0.0,\n"
                + "      \"Moeda\": \"EUR\"\n"
                + "    },\n"
                + "    \"Moeda\": \"EUR\",\n"
                + "    \"ComSCA\": false\n"
                + "  }\n"
                + "}";
    }

    static {
        creditCardPayload.put(
                PAYLOAD_ACCOUNT_ID_2,
                "{\n"
                        + "  \"Header\": {\n"
                        + "    \"ResponseId\": \"c3b053d0156e4d49953015ec1fe552c5\",\n"
                        + "    \"OpToken\": \"3112aa53cdcd48bdae3a4251faf49d3b\",\n"
                        + "    \"Time\": \"2019-11-22T08:29:55.7797126Z\",\n"
                        + "    \"SessionTimeout\": 1800,\n"
                        + "    \"Status\": {\n"
                        + "      \"Mensagem\": \"Não foi possível seleccionar cartão\",\n"
                        + "      \"Severidade\": 3,\n"
                        + "      \"Codigo\": 2005\n"
                        + "    },\n"
                        + "    \"Contexto\": {\n"
                        + "      \"Contas\": null\n"
                        + "    }\n"
                        + "  },\n"
                        + "  \"Body\": {},\n"
                        + "  \"Login\": {}\n"
                        + "}");
        creditCardPayload.put(
                PAYLOAD_ACCOUNT_ID_1,
                "{\n"
                        + "  \"Header\": {\n"
                        + "    \"ResponseId\": \"d2f5594673a54cceb155a60514415589\",\n"
                        + "    \"OpToken\": \"e74be8fc57ed478284f885cb09d2fffb\",\n"
                        + "    \"Time\": \"2019-11-22T08:25:08.1950799Z\",\n"
                        + "    \"SessionTimeout\": 1800,\n"
                        + "    \"Status\": {\n"
                        + "      \"Severidade\": 0,\n"
                        + "      \"Codigo\": 0\n"
                        + "    }\n"
                        + "  },\n"
                        + "  \"Body\": {\n"
                        + "    \"DataHoje\": \"2019-11-22\",\n"
                        + "    \"Movimentos\": null,\n"
                        + "    \"ContextoCartoes\": {\n"
                        + "      \"ContasCartao\": {\n"
                        + "        \"Lista\": [{\n"
                        + "          \"Id\": \"403541000691234\",\n"
                        + "          \"Desc\": \"0694123\",\n"
                        + "          \"Cartoes\": [{\n"
                        + "            \"Id\": \"qFwLA3O+lvn+ejL4QEjbKpKEiOCgEG1tDET+F30=\",\n"
                        + "            \"NomeNoCartao\": \"Card Owner\",\n"
                        + "            \"Marca\": \"NB VERDE VISA\",\n"
                        + "            \"Tipo\": \"M\",\n"
                        + "            \"NumeroCartao\": \""
                        + CARD_1_NUMBER
                        + "\",\n"
                        + "            \"IdEstado\": 2,\n"
                        + "            \"DataValidade\": \"2023-11-30\",\n"
                        + "            \"LimiteCredito\": "
                        + CARD_1_BALANCE
                        + ",\n"
                        + "            \"SaldoUtilizado\": 0.00,\n"
                        + "            \"SaldoDisponivel\": "
                        + CARD_1_AVAILABLE_CREDIT
                        + ",\n"
                        + "            \"CodigoProduto\": \"000341\",\n"
                        + "            \"Estado\": \"BLA BLA BLA\",\n"
                        + "            \"Url\": \"Img.axd?k=BA%2bziTe0WSRf4gy3o6Vmb4zf%2fylNUoXwNX8BGP3lHDc%3d&r=1\",\n"
                        + "            \"Validade\": \"11/2023\"\n"
                        + "          }],\n"
                        + "          \"LimiteCredito\": 500.00,\n"
                        + "          \"CreditoDisponivel\": 500.00,\n"
                        + "          \"SaldoUtilizado\": 0.00,\n"
                        + "          \"IdFormaPagamento\": \"01\",\n"
                        + "          \"FormaPagamento\": \"Percentagem 100%\",\n"
                        + "          \"NumeroConta\": \"24007553071\",\n"
                        + "          \"ContaDO\": \""
                        + PAYLOAD_ACCOUNT_ID_1
                        + "\",\n"
                        + "          \"IdModalidadeExtrato\": \"05\",\n"
                        + "          \"ModalidadeExtrato\": \"NBnet\",\n"
                        + "          \"PercentagemPagamento\": 0.00,\n"
                        + "          \"Moeda\": \""
                        + CARD_1_CURRENCY
                        + "\"\n"
                        + "        }],\n"
                        + "        \"ContaCartaoSelected\": \"403541000691234\",\n"
                        + "        \"CartaoSelected\": \"qFwLA3O+lvn+ejL4QEjbKpKEiOCgEG1tDET+F30=\"\n"
                        + "      }\n"
                        + "    },\n"
                        + "    \"ComSCA\": false\n"
                        + "  }\n"
                        + "}");
    }

    static {
        referenceCreditCardDtos.add(
                new CreditCardDto(
                        PAYLOAD_ACCOUNT_ID_1,
                        CARD_1_ID,
                        ACCOUNT_1_DESCR,
                        ExactCurrencyAmount.of(CARD_1_BALANCE, CARD_1_CURRENCY),
                        ExactCurrencyAmount.of(CARD_1_BALANCE, CARD_1_CURRENCY)));
    }
}
