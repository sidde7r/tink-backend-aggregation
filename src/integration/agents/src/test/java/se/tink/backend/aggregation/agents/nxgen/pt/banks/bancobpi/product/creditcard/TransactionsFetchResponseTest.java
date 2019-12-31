package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product.creditcard;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.common.RequestException;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class TransactionsFetchResponseTest {

    private CreditCardAccount account;
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");

    @Before
    public void init() {
        account = Mockito.mock(CreditCardAccount.class);
    }

    @Test
    public void shouldParseIntermediatePageResponse() throws RequestException {
        // given
        final String rawResponse =
                "{\"versionInfo\": {\"hasModuleVersionChanged\": false,\"hasApiVersionChanged\": false},\"data\": {\"TransactionStatus\": {\"OperationStatusId\": 1,\"TransactionErrors\": {\"List\": [],\"EmptyListItem\": {\"TransactionError\": {\"Source\": \"\",\"Code\": \"\",\"Level\": 0,\"Description\": \"\"}}},\"AuthStatusReason\": {\"List\": [],\"EmptyListItem\": {\"Status\": \"\",\"Code\": \"\",\"Description\": \"\"}}},\"PaginacaoOut\": {\"uuid\": \"cf14916d-a7c2-411e-8cca-b4b035798412\",\"lastPage\": false,\"pageNumber\": 1,\"pageSize\": 10,\"currentPage\": \"1\",\"recordCount\": \"25\"},\"ListaMovimentos\": {\"List\": [{\"NumeroCartaoMasked\": \"\",\"DataTransaccao\": \"2019-12-17\",\"DataMovimento\": \"2019-12-17\",\"DescricaoTransaccao\": \"S/ENTREGA-PAGAMENTO\",\"MontanteTransaccao\": \"-242.00\",\"Pontos\": \"0.00\",\"PercentagemDesconto\": \"0.000\",\"MontanteDesconto\": \"0.00\",\"CodigoMoedaOrigem\": 0,\"MontanteOrigem\": \"0.00\",\"NumeroSequencia\": 1,\"NumeroContaCartaoMasked\": \"\",\"DataExtracto\": \"01-01-1900\",\"Extracto\": {\"indicador\": false,\"data\": \"1900-01-01T00:00:00\"}},{\"NumeroCartaoMasked\": \"\",\"DataTransaccao\": \"2019-12-13\",\"DataMovimento\": \"2019-12-13\",\"DescricaoTransaccao\": \"IMPOSTO DE SELO\",\"MontanteTransaccao\": \"0.42\",\"Pontos\": \"0.00\",\"PercentagemDesconto\": \"0.000\",\"MontanteDesconto\": \"0.00\",\"CodigoMoedaOrigem\": 0,\"MontanteOrigem\": \"0.00\",\"NumeroSequencia\": 2,\"NumeroContaCartaoMasked\": \"\",\"DataExtracto\": \"01-01-1900\",\"Extracto\": {\"indicador\": true,\"data\": \"1900-01-01T00:00:00\"}}]},\"RequestValid\": true}}";
        // when
        TransactionsFetchResponse objectUnderTest =
                new TransactionsFetchResponse(rawResponse, account);
        // then
        Assert.assertFalse(objectUnderTest.isLastPage());
        Assert.assertEquals(
                "cf14916d-a7c2-411e-8cca-b4b035798412", objectUnderTest.getBankFetchingUUID());
        Assert.assertEquals(2, objectUnderTest.getTransactions().size());
        Transaction transaction = objectUnderTest.getTransactions().get(0);
        Assert.assertEquals(
                new BigDecimal("-242.00"), transaction.getExactAmount().getExactValue());
        Assert.assertEquals("EUR", transaction.getExactAmount().getCurrencyCode());
        Assert.assertEquals("S/ENTREGA-PAGAMENTO", transaction.getDescription());
        Assert.assertEquals("2019-12-17", DATE_FORMATTER.format(transaction.getDate()));
    }

    @Test
    public void shouldParseLastPageResponse() throws RequestException {
        // given
        final String rawResponse =
                "{\"versionInfo\": {\"hasModuleVersionChanged\": false,\"hasApiVersionChanged\": false},\"data\": {\"TransactionStatus\": {\"OperationStatusId\": 1,\"TransactionErrors\": {\"List\": [],\"EmptyListItem\": {\"TransactionError\": {\"Source\": \"\",\"Code\": \"\",\"Level\": 0,\"Description\": \"\"}}},\"AuthStatusReason\": {\"List\": [],\"EmptyListItem\": {\"Status\": \"\",\"Code\": \"\",\"Description\": \"\"}}},\"PaginacaoOut\": {\"uuid\": \"cf14916d-a7c2-411e-8cca-b4b035798412\",\"lastPage\": true,\"pageNumber\": 1,\"pageSize\": 10,\"currentPage\": \"1\",\"recordCount\": \"25\"},\"ListaMovimentos\": {\"List\": [{\"NumeroCartaoMasked\": \"\",\"DataTransaccao\": \"2019-12-17\",\"DataMovimento\": \"2019-12-17\",\"DescricaoTransaccao\": \"S/ENTREGA-PAGAMENTO\",\"MontanteTransaccao\": \"-242.00\",\"Pontos\": \"0.00\",\"PercentagemDesconto\": \"0.000\",\"MontanteDesconto\": \"0.00\",\"CodigoMoedaOrigem\": 0,\"MontanteOrigem\": \"0.00\",\"NumeroSequencia\": 1,\"NumeroContaCartaoMasked\": \"\",\"DataExtracto\": \"01-01-1900\",\"Extracto\": {\"indicador\": false,\"data\": \"1900-01-01T00:00:00\"}},{\"NumeroCartaoMasked\": \"\",\"DataTransaccao\": \"2019-12-13\",\"DataMovimento\": \"2019-12-13\",\"DescricaoTransaccao\": \"IMPOSTO DE SELO\",\"MontanteTransaccao\": \"0.42\",\"Pontos\": \"0.00\",\"PercentagemDesconto\": \"0.000\",\"MontanteDesconto\": \"0.00\",\"CodigoMoedaOrigem\": 0,\"MontanteOrigem\": \"0.00\",\"NumeroSequencia\": 2,\"NumeroContaCartaoMasked\": \"\",\"DataExtracto\": \"01-01-1900\",\"Extracto\": {\"indicador\": true,\"data\": \"1900-01-01T00:00:00\"}}]},\"RequestValid\": true}}";
        // when
        TransactionsFetchResponse objectUnderTest =
                new TransactionsFetchResponse(rawResponse, account);
        // then
        Assert.assertTrue(objectUnderTest.isLastPage());
    }
}
