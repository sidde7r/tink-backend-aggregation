package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.component.transactional.detail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.detail.TransactionDto;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.detail.TransactionalAccountDto;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.GetAccountsResponse;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class AccountsTestData {
    public static final String PAYLOAD_ACCOUNT_ID_1 = "330000123456";
    public static final String PAYLOAD_ACCOUNT_ID_2 = "1005967000002";
    public static final String PAYLOAD_ERRORED = "ERRORED";
    public static final String PAYLOAD_ALL_ACCOUNTS = "330000123456";

    public static final String ACCOUNT_1_BALANCE = "1580.19";
    public static final String ACCOUNT_1_CURRENCY = "EUR";
    public static final String ACCOUNT_2_BALANCE = "650.37";
    public static final String ACCOUNT_2_CURRENCY = "EUR";

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
                        + "\t\"Header\": {\n"
                        + "\t\t\"ResponseId\": \"35e47c740a174f34af98369ef64f4775\",\n"
                        + "\t\t\"OpToken\": \"ac1c6506542b415aa5e15af45416906a\",\n"
                        + "\t\t\"Time\": \"2019-11-25T11:32:37.4690702Z\",\n"
                        + "\t\t\"SessionTimeout\": 1800,\n"
                        + "\t\t\"Status\": {\n"
                        + "\t\t\t\"Severidade\": 0,\n"
                        + "\t\t\t\"Codigo\": 0\n"
                        + "\t\t},\n"
                        + "\t\t\"Contexto\": {\n"
                        + "\t\t\t\"Contas\": {\n"
                        + "\t\t\t\t\"Lista\": [{\n"
                        + "\t\t\t\t\t\"Id\": \""
                        + PAYLOAD_ACCOUNT_ID_1
                        + "\",\n"
                        + "\t\t\t\t\t\"Iban\": \"PT50000201231234567890154\",\n"
                        + "\t\t\t\t\t\"Desc\": \"Conta DO\"\n"
                        + "\t\t\t\t}, {\n"
                        + "\t\t\t\t\t\"Id\": \""
                        + PAYLOAD_ACCOUNT_ID_2
                        + "\",\n"
                        + "\t\t\t\t\t\"Desc\": \"Dep. Someone Fancy\"\n"
                        + "\t\t\t\t}],\n"
                        + "\t\t\t\t\"Selected\": \""
                        + PAYLOAD_ACCOUNT_ID_1
                        + "\"\n"
                        + "\t\t\t}\n"
                        + "\t\t}\n"
                        + "\t},\n"
                        + "\t\"Body\": {\n"
                        + "\t\t\"DataHoje\": \"2019-11-25\",\n"
                        + "\t\t\"Movimentos\": [{\n"
                        + "\t\t\t\"Saldo\": 1580.19,\n"
                        + "\t\t\t\"Tipo\": 0,\n"
                        + "\t\t\t\"Descricao\": \""
                        + TRANS_1_DESCR
                        + "\",\n"
                        + "\t\t\t\"Montante\": "
                        + TRANS_1_BALANCE
                        + ",\n"
                        + "\t\t\t\"DataValor\": \"2019-11-25\",\n"
                        + "\t\t\t\"DataOperacao\": \""
                        + TRANS_1_DATE
                        + "\",\n"
                        + "\t\t\t\"Numero\": 5882,\n"
                        + "\t\t\t\"NumPedido\": \"12121212\",\n"
                        + "\t\t\t\"Categoria\": \"-\",\n"
                        + "\t\t\t\"IdCategoria\": 0\n"
                        + "\t\t}, {\n"
                        + "\t\t\t\"Saldo\": 4080.19,\n"
                        + "\t\t\t\"Tipo\": 0,\n"
                        + "\t\t\t\"Descricao\": \""
                        + TRANS_2_DESCR
                        + "\",\n"
                        + "\t\t\t\"Montante\": "
                        + TRANS_2_BALANCE
                        + ",\n"
                        + "\t\t\t\"DataValor\": \"2019-11-25\",\n"
                        + "\t\t\t\"DataOperacao\": \""
                        + TRANS_2_DATE
                        + "\",\n"
                        + "\t\t\t\"Numero\": 5881,\n"
                        + "\t\t\t\"Categoria\": \"-\",\n"
                        + "\t\t\t\"IdCategoria\": 0\n"
                        + "\t\t}, {\n"
                        + "\t\t\t\"Saldo\": 4093.56,\n"
                        + "\t\t\t\"Tipo\": 0,\n"
                        + "\t\t\t\"Descricao\": \""
                        + TRANS_3_DESCR
                        + "\",\n"
                        + "\t\t\t\"Montante\": "
                        + TRANS_3_BALANCE
                        + ",\n"
                        + "\t\t\t\"DataValor\": \"2019-11-25\",\n"
                        + "\t\t\t\"DataOperacao\": \""
                        + TRANS_3_DATE
                        + "\",\n"
                        + "\t\t\t\"Numero\": 5880,\n"
                        + "\t\t\t\"Categoria\": \"-\",\n"
                        + "\t\t\t\"IdCategoria\": 0\n"
                        + "\t\t}],\n"
                        + "\t\t\"Saldo\": {\n"
                        + "\t\t\t\"Disponivel\": 1580.19,\n"
                        + "\t\t\t\"Cativo\": 0.0,\n"
                        + "\t\t\t\"Contabilistico\": "
                        + ACCOUNT_1_BALANCE
                        + ",\n"
                        + "\t\t\t\"Autorizado\": 1580.19,\n"
                        + "\t\t\t\"Descoberto\": 0.0,\n"
                        + "\t\t\t\"Moeda\": \""
                        + ACCOUNT_1_CURRENCY
                        + "\"\n"
                        + "\t\t},\n"
                        + "\t\t\"Moeda\": \"EUR\",\n"
                        + "\t\t\"TokenPaginacao\": \"20191023202622534427|20191023202622534427_BO3V|WSTBFY\",\n"
                        + "\t\t\"ComSCA\": false\n"
                        + "\t}\n"
                        + "}");
        requests.put(
                PAYLOAD_ACCOUNT_ID_2,
                "{\n"
                        + "\t\"Header\": {\n"
                        + "\t\t\"ResponseId\": \"f30741d442ff4fd2921dc2c7106928d2\",\n"
                        + "\t\t\"OpToken\": \"dd0145b55b1b44faa6923823e7cff8bd\",\n"
                        + "\t\t\"Time\": \"2019-11-25T11:32:36.7202614Z\",\n"
                        + "\t\t\"SessionTimeout\": 1800,\n"
                        + "\t\t\"Status\": {\n"
                        + "\t\t\t\"Severidade\": 0,\n"
                        + "\t\t\t\"Codigo\": 0\n"
                        + "\t\t},\n"
                        + "\t\t\"Contexto\": {\n"
                        + "\t\t\t\"Contas\": {\n"
                        + "\t\t\t\t\"Lista\": [{\n"
                        + "\t\t\t\t\t\"Id\": \""
                        + PAYLOAD_ACCOUNT_ID_1
                        + "\",\n"
                        + "\t\t\t\t\t\"Iban\": \"PT50000201231234567890154\",\n"
                        + "\t\t\t\t\t\"Desc\": \"Conta DO\"\n"
                        + "\t\t\t\t}, {\n"
                        + "\t\t\t\t\t\"Id\": \""
                        + PAYLOAD_ACCOUNT_ID_2
                        + "\",\n"
                        + "\t\t\t\t\t\"Desc\": \"Dep. Someone Fancy\"\n"
                        + "\t\t\t\t}],\n"
                        + "\t\t\t\t\"Selected\": \""
                        + PAYLOAD_ACCOUNT_ID_2
                        + "\"\n"
                        + "\t\t\t}\n"
                        + "\t\t}\n"
                        + "\t},\n"
                        + "\t\"Body\": {\n"
                        + "\t\t\"DataHoje\": \"2019-11-25\",\n"
                        + "\t\t\"Movimentos\": [{\n"
                        + "\t\t\t\"Saldo\": 650.37,\n"
                        + "\t\t\t\"Tipo\": 0,\n"
                        + "\t\t\t\"Descricao\": \"TRF ENTREGA PROGRAMADA MENSAL DE 1234\",\n"
                        + "\t\t\t\"Montante\": 10.0,\n"
                        + "\t\t\t\"DataValor\": \"2019-11-01\",\n"
                        + "\t\t\t\"DataOperacao\": \"2019-11-01\",\n"
                        + "\t\t\t\"Numero\": 21,\n"
                        + "\t\t\t\"Categoria\": \"Intrapatrimónio\",\n"
                        + "\t\t\t\"IdCategoria\": 359,\n"
                        + "\t\t\t\"OfValor\": 0.0,\n"
                        + "\t\t\t\"OfIdN1\": 13,\n"
                        + "\t\t\t\"OfIcon\": 13\n"
                        + "\t\t}, {\n"
                        + "\t\t\t\"Saldo\": 640.37,\n"
                        + "\t\t\t\"Tipo\": 0,\n"
                        + "\t\t\t\"Descricao\": \"TRF ENTREGA PROGRAMADA MENSAL DE 123456\",\n"
                        + "\t\t\t\"Montante\": 10.0,\n"
                        + "\t\t\t\"DataValor\": \"2019-10-01\",\n"
                        + "\t\t\t\"DataOperacao\": \"2019-10-01\",\n"
                        + "\t\t\t\"Numero\": 20,\n"
                        + "\t\t\t\"Categoria\": \"Intrapatrimónio\",\n"
                        + "\t\t\t\"IdCategoria\": 359,\n"
                        + "\t\t\t\"OfIdN1\": 13,\n"
                        + "\t\t\t\"OfIcon\": 13\n"
                        + "\t\t}, {\n"
                        + "\t\t\t\"Saldo\": 630.37,\n"
                        + "\t\t\t\"Tipo\": 0,\n"
                        + "\t\t\t\"Descricao\": \"TRF ENTREGA PROGRAMADA MENSAL DE 123456\",\n"
                        + "\t\t\t\"Montante\": 10.0,\n"
                        + "\t\t\t\"DataValor\": \"2019-09-01\",\n"
                        + "\t\t\t\"DataOperacao\": \"2019-09-01\",\n"
                        + "\t\t\t\"Numero\": 19,\n"
                        + "\t\t\t\"Categoria\": \"Intrapatrimónio\",\n"
                        + "\t\t\t\"IdCategoria\": 359,\n"
                        + "\t\t\t\"OfIdN1\": 13,\n"
                        + "\t\t\t\"OfIcon\": 13\n"
                        + "\t\t}, {\n"
                        + "\t\t\t\"Saldo\": 620.37,\n"
                        + "\t\t\t\"Tipo\": 0,\n"
                        + "\t\t\t\"Descricao\": \"IMPOSTO RENDIMENTO S/ JUROS CONTA POUPANÇA\",\n"
                        + "\t\t\t\"Montante\": -0.08,\n"
                        + "\t\t\t\"DataValor\": \"2019-08-29\",\n"
                        + "\t\t\t\"DataOperacao\": \"2019-08-29\",\n"
                        + "\t\t\t\"Numero\": 18,\n"
                        + "\t\t\t\"Categoria\": \"Impostos, Fundos de Pensões, outras Taxas\",\n"
                        + "\t\t\t\"IdCategoria\": 346,\n"
                        + "\t\t\t\"OfIcon\": 8\n"
                        + "\t\t}],\n"
                        + "\t\t\"Saldo\": {\n"
                        + "\t\t\t\"Disponivel\": 650.37,\n"
                        + "\t\t\t\"Cativo\": 0.0,\n"
                        + "\t\t\t\"Contabilistico\": "
                        + ACCOUNT_2_BALANCE
                        + ",\n"
                        + "\t\t\t\"Autorizado\": 650.37,\n"
                        + "\t\t\t\"Descoberto\": 0.0,\n"
                        + "\t\t\t\"Moeda\": \""
                        + ACCOUNT_2_CURRENCY
                        + "\"\n"
                        + "\t\t},\n"
                        + "\t\t\"Moeda\": \"EUR\",\n"
                        + "\t\t\"ComSCA\": false\n"
                        + "\t}\n"
                        + "}");

        requests.put(
                PAYLOAD_ERRORED,
                "{\n"
                        + "\t\"Header\": {\n"
                        + "\t\t\"ResponseId\": \"b7e8627cf9b64542aaf0a64723838950\",\n"
                        + "\t\t\"OpToken\": null,\n"
                        + "\t\t\"Time\": \"2019-11-15T14:53:06.8443181Z\",\n"
                        + "\t\t\"SessionTimeout\": 0,\n"
                        + "\t\t\"Status\": {\n"
                        + "\t\t\t\"Mensagem\": \"De momento não é possível processar a sua instrução. Por favor tente mais tarde.\",\n"
                        + "\t\t\t\"Severidade\": 3,\n"
                        + "\t\t\t\"Codigo\": 50\n"
                        + "\t\t}\n"
                        + "\t}\n"
                        + "}");
    }

    static {
        referenceTransactionalAccountDtos.put(
                PAYLOAD_ACCOUNT_ID_1,
                new TransactionalAccountDto(
                        PAYLOAD_ACCOUNT_ID_1,
                        "PT50000201231234567890154",
                        ExactCurrencyAmount.of(ACCOUNT_1_BALANCE, ACCOUNT_1_CURRENCY)));
        referenceTransactionalAccountDtos.put(
                PAYLOAD_ACCOUNT_ID_2,
                new TransactionalAccountDto(
                        PAYLOAD_ACCOUNT_ID_2,
                        PAYLOAD_ACCOUNT_ID_2,
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
