package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.component.transactional.detail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.detail.TransactionDto;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.detail.TransactionalAccountDto;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.GetAccountsResponse;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class AccountsTestData {
    public static final String PAYLOAD_ACCOUNT_ID_1 = "330000123456";
    public static final String PAYLOAD_ACCOUNT_ID_2 = "1005967000002";
    public static final String PAYLOAD_ERRORED = "ERRORED";
    public static final String PAYLOAD_ALL_ACCOUNTS = "330000123456";

    public static final String ACCOUNT_1_BALANCE = "1580.19";
    public static final String ACCOUNT_1_CURRENCY = "EUR";
    public static final String ACCOUNT_1_DESCR = "Conta DO";
    public static final String ACCOUNT_1_IBAN = "PT50000201231234567890154";

    public static final String ACCOUNT_2_BALANCE = "650.37";
    public static final String ACCOUNT_2_CURRENCY = "EUR";
    public static final String ACCOUNT_2_DESCR = "Dep. Someone Fancy";

    public static final String TRANS_1_BALANCE = "-2500.0";
    public static final String TRANS_1_DESCR = "TRF NBapp 1234567 P/ Ricardo BEST";
    public static final String TRANS_1_DATE = "2019-11-25";

    public static final String TRANS_2_BALANCE = "-13.37";
    public static final String TRANS_2_DESCR = "COBRANCA SDD GNB - COMPANHIA DE ADC 1234567";
    public static final String TRANS_2_DATE = "2019-11-25";

    public static final String TRANS_3_BALANCE = "3900.35";
    public static final String TRANS_3_DESCR = "TRANSF. CREDITO SEPA DE Tagusgas - Empresa";
    public static final String TRANS_3_DATE = "2019-11-25";

    private static final Map<String, String> requests = new HashMap<>();
    private static final Map<String, TransactionalAccountDto> referenceTransactionalAccountDtos =
            new HashMap<>();
    private static final List<TransactionDto> referenceTransactionDtos = new ArrayList<>();

    public static GetAccountsResponse getResponse(String payloadLabel) {
        return SerializationUtils.deserializeFromString(
                requests.get(payloadLabel), GetAccountsResponse.class);
    }

    public static TransactionalAccountDto getReferenceAccountDto(String id) {
        return referenceTransactionalAccountDtos.get(id);
    }

    public static Collection<TransactionDto> getReferenceTransactionDtos() {
        return referenceTransactionDtos;
    }

    static {
        requests.put(
                PAYLOAD_ACCOUNT_ID_1,
                "{\n"
                        + "  \"Header\": {\n"
                        + "    \"ResponseId\": \"35e47c740a174f34af98369ef64f4775\",\n"
                        + "    \"OpToken\": \"ac1c6506542b415aa5e15af45416906a\",\n"
                        + "    \"Time\": \"2019-11-25T11:32:37.4690702Z\",\n"
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
                        + "          \"Iban\": \""
                        + ACCOUNT_1_IBAN
                        + "\",\n"
                        + "          \"Desc\": \""
                        + ACCOUNT_1_DESCR
                        + "\"\n"
                        + "        }, {\n"
                        + "          \"Id\": \""
                        + PAYLOAD_ACCOUNT_ID_2
                        + "\",\n"
                        + "          \"Desc\": \""
                        + ACCOUNT_2_DESCR
                        + "\"\n"
                        + "        }],\n"
                        + "        \"Selected\": \""
                        + PAYLOAD_ACCOUNT_ID_1
                        + "\"\n"
                        + "      }\n"
                        + "    }\n"
                        + "  },\n"
                        + "  \"Body\": {\n"
                        + "    \"DataHoje\": \"2019-11-25\",\n"
                        + "    \"Movimentos\": [{\n"
                        + "      \"Saldo\": 1580.19,\n"
                        + "      \"Tipo\": 0,\n"
                        + "      \"Descricao\": \""
                        + TRANS_1_DESCR
                        + "\",\n"
                        + "      \"Montante\": "
                        + TRANS_1_BALANCE
                        + ",\n"
                        + "      \"DataValor\": \"2019-11-25\",\n"
                        + "      \"DataOperacao\": \""
                        + TRANS_1_DATE
                        + "\",\n"
                        + "      \"Numero\": 5882,\n"
                        + "      \"NumPedido\": \"12121212\",\n"
                        + "      \"Categoria\": \"-\",\n"
                        + "      \"IdCategoria\": 0\n"
                        + "    }, {\n"
                        + "      \"Saldo\": 4080.19,\n"
                        + "      \"Tipo\": 0,\n"
                        + "      \"Descricao\": \""
                        + TRANS_2_DESCR
                        + "\",\n"
                        + "      \"Montante\": "
                        + TRANS_2_BALANCE
                        + ",\n"
                        + "      \"DataValor\": \"2019-11-25\",\n"
                        + "      \"DataOperacao\": \""
                        + TRANS_2_DATE
                        + "\",\n"
                        + "      \"Numero\": 5881,\n"
                        + "      \"Categoria\": \"-\",\n"
                        + "      \"IdCategoria\": 0\n"
                        + "    }, {\n"
                        + "      \"Saldo\": 4093.56,\n"
                        + "      \"Tipo\": 0,\n"
                        + "      \"Descricao\": \""
                        + TRANS_3_DESCR
                        + "\",\n"
                        + "      \"Montante\": "
                        + TRANS_3_BALANCE
                        + ",\n"
                        + "      \"DataValor\": \"2019-11-25\",\n"
                        + "      \"DataOperacao\": \""
                        + TRANS_3_DATE
                        + "\",\n"
                        + "      \"Numero\": 5880,\n"
                        + "      \"Categoria\": \"-\",\n"
                        + "      \"IdCategoria\": 0\n"
                        + "    }],\n"
                        + "    \"Saldo\": {\n"
                        + "      \"Disponivel\": 1580.19,\n"
                        + "      \"Cativo\": 0.0,\n"
                        + "      \"Contabilistico\": "
                        + ACCOUNT_1_BALANCE
                        + ",\n"
                        + "      \"Autorizado\": 1580.19,\n"
                        + "      \"Descoberto\": 0.0,\n"
                        + "      \"Moeda\": \""
                        + ACCOUNT_1_CURRENCY
                        + "\"\n"
                        + "    },\n"
                        + "    \"Moeda\": \"EUR\",\n"
                        + "    \"TokenPaginacao\": \"20191023202622534427|20191023202622534427_BO3V|WSTBFY\",\n"
                        + "    \"ComSCA\": false\n"
                        + "  }\n"
                        + "}");
        requests.put(
                PAYLOAD_ACCOUNT_ID_2,
                "{\n"
                        + "  \"Header\": {\n"
                        + "    \"ResponseId\": \"f30741d442ff4fd2921dc2c7106928d2\",\n"
                        + "    \"OpToken\": \"dd0145b55b1b44faa6923823e7cff8bd\",\n"
                        + "    \"Time\": \"2019-11-25T11:32:36.7202614Z\",\n"
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
                        + "          \"Iban\": \""
                        + ACCOUNT_1_IBAN
                        + "\",\n"
                        + "          \"Desc\": \""
                        + ACCOUNT_1_DESCR
                        + "\"\n"
                        + "        }, {\n"
                        + "          \"Id\": \""
                        + PAYLOAD_ACCOUNT_ID_2
                        + "\",\n"
                        + "          \"Desc\": \""
                        + ACCOUNT_2_DESCR
                        + "\"\n"
                        + "        }],\n"
                        + "        \"Selected\": \""
                        + PAYLOAD_ACCOUNT_ID_2
                        + "\"\n"
                        + "      }\n"
                        + "    }\n"
                        + "  },\n"
                        + "  \"Body\": {\n"
                        + "    \"DataHoje\": \"2019-11-25\",\n"
                        + "    \"Movimentos\": [{\n"
                        + "      \"Saldo\": 650.37,\n"
                        + "      \"Tipo\": 0,\n"
                        + "      \"Descricao\": \"TRF ENTREGA PROGRAMADA MENSAL DE 1234\",\n"
                        + "      \"Montante\": 10.0,\n"
                        + "      \"DataValor\": \"2019-11-01\",\n"
                        + "      \"DataOperacao\": \"2019-11-01\",\n"
                        + "      \"Numero\": 21,\n"
                        + "      \"Categoria\": \"Intrapatrimónio\",\n"
                        + "      \"IdCategoria\": 359,\n"
                        + "      \"OfValor\": 0.0,\n"
                        + "      \"OfIdN1\": 13,\n"
                        + "      \"OfIcon\": 13\n"
                        + "    }, {\n"
                        + "      \"Saldo\": 640.37,\n"
                        + "      \"Tipo\": 0,\n"
                        + "      \"Descricao\": \"TRF ENTREGA PROGRAMADA MENSAL DE 123456\",\n"
                        + "      \"Montante\": 10.0,\n"
                        + "      \"DataValor\": \"2019-10-01\",\n"
                        + "      \"DataOperacao\": \"2019-10-01\",\n"
                        + "      \"Numero\": 20,\n"
                        + "      \"Categoria\": \"Intrapatrimónio\",\n"
                        + "      \"IdCategoria\": 359,\n"
                        + "      \"OfIdN1\": 13,\n"
                        + "      \"OfIcon\": 13\n"
                        + "    }, {\n"
                        + "      \"Saldo\": 630.37,\n"
                        + "      \"Tipo\": 0,\n"
                        + "      \"Descricao\": \"TRF ENTREGA PROGRAMADA MENSAL DE 123456\",\n"
                        + "      \"Montante\": 10.0,\n"
                        + "      \"DataValor\": \"2019-09-01\",\n"
                        + "      \"DataOperacao\": \"2019-09-01\",\n"
                        + "      \"Numero\": 19,\n"
                        + "      \"Categoria\": \"Intrapatrimónio\",\n"
                        + "      \"IdCategoria\": 359,\n"
                        + "      \"OfIdN1\": 13,\n"
                        + "      \"OfIcon\": 13\n"
                        + "    }, {\n"
                        + "      \"Saldo\": 620.37,\n"
                        + "      \"Tipo\": 0,\n"
                        + "      \"Descricao\": \"IMPOSTO RENDIMENTO S/ JUROS CONTA POUPANÇA\",\n"
                        + "      \"Montante\": -0.08,\n"
                        + "      \"DataValor\": \"2019-08-29\",\n"
                        + "      \"DataOperacao\": \"2019-08-29\",\n"
                        + "      \"Numero\": 18,\n"
                        + "      \"Categoria\": \"Impostos, Fundos de Pensões, outras Taxas\",\n"
                        + "      \"IdCategoria\": 346,\n"
                        + "      \"OfIcon\": 8\n"
                        + "    }],\n"
                        + "    \"Saldo\": {\n"
                        + "      \"Disponivel\": 650.37,\n"
                        + "      \"Cativo\": 0.0,\n"
                        + "      \"Contabilistico\": "
                        + ACCOUNT_2_BALANCE
                        + ",\n"
                        + "      \"Autorizado\": 650.37,\n"
                        + "      \"Descoberto\": 0.0,\n"
                        + "      \"Moeda\": \""
                        + ACCOUNT_2_CURRENCY
                        + "\"\n"
                        + "    },\n"
                        + "    \"Moeda\": \"EUR\",\n"
                        + "    \"ComSCA\": false\n"
                        + "  }\n"
                        + "}");

        requests.put(
                PAYLOAD_ERRORED,
                "{\n"
                        + "  \"Header\": {\n"
                        + "    \"ResponseId\": \"b7e8627cf9b64542aaf0a64723838950\",\n"
                        + "    \"OpToken\": null,\n"
                        + "    \"Time\": \"2019-11-15T14:53:06.8443181Z\",\n"
                        + "    \"SessionTimeout\": 0,\n"
                        + "    \"Status\": {\n"
                        + "      \"Mensagem\": \"De momento não é possível processar a sua instrução. Por favor tente mais tarde.\",\n"
                        + "      \"Severidade\": 3,\n"
                        + "      \"Codigo\": 50\n"
                        + "    }\n"
                        + "  }\n"
                        + "}");
    }

    static {
        referenceTransactionalAccountDtos.put(
                PAYLOAD_ACCOUNT_ID_1,
                new TransactionalAccountDto(
                        PAYLOAD_ACCOUNT_ID_1,
                        ACCOUNT_1_IBAN,
                        ACCOUNT_1_DESCR,
                        ExactCurrencyAmount.of(ACCOUNT_1_BALANCE, ACCOUNT_1_CURRENCY)));
        referenceTransactionalAccountDtos.put(
                PAYLOAD_ACCOUNT_ID_2,
                new TransactionalAccountDto(
                        PAYLOAD_ACCOUNT_ID_2,
                        PAYLOAD_ACCOUNT_ID_2,
                        ACCOUNT_2_DESCR,
                        ExactCurrencyAmount.of(ACCOUNT_2_BALANCE, ACCOUNT_2_CURRENCY)));
    }

    static {
        referenceTransactionDtos.add(
                new TransactionDto(
                        TRANS_1_BALANCE, ACCOUNT_1_CURRENCY, TRANS_1_DATE, TRANS_1_DESCR));
        referenceTransactionDtos.add(
                new TransactionDto(
                        TRANS_2_BALANCE, ACCOUNT_1_CURRENCY, TRANS_2_DATE, TRANS_2_DESCR));
        referenceTransactionDtos.add(
                new TransactionDto(
                        TRANS_3_BALANCE, ACCOUNT_1_CURRENCY, TRANS_3_DATE, TRANS_3_DESCR));
    }
}
