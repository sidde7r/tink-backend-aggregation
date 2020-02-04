package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product.account;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class TransactionsFetchResponseTest {

    private static final String RESPONSE_EXPECTED =
            "{\"versionInfo\": {\"hasModuleVersionChanged\": false,\"hasApiVersionChanged\": false},\"data\": {\"MovimentosConta\": {\"List\": [{\"dataMovimento\": \"2019-12-09\",\"descricao\": \"09/12 COMPRA ELEC 6330968/10 REPSOL ALTO DO VALE 2070 CARTAX\",\"valorMoedaConta\": \"-28.72\",\"moedaOperacao\": \"EUR\",\"valorOperacao\": \"0.0\",\"saldoMoedaConta\": \"276.42\"}, {\"dataMovimento\": \"2019-12-09\",\"descricao\": \"09/12 COMPRA ELEC 6330968/09 OURIQUENSE LDAVILA CHA DE\",\"valorMoedaConta\": \"-16.6\",\"moedaOperacao\": \"EUR\",\"valorOperacao\": \"0.0\",\"saldoMoedaConta\": \"305.14\"}]},\"SaldoMoedaConta\": \"EUR\",\"PaginacaoOut\": {\"uuid\": \"193648e9-ddc6-4092-a3a5-e10b69fa2cb8\",\"lastPage\": true,\"pageNumber\": 4,\"pageSize\": 10,\"currentPage\": \"\",\"recordCount\": \"\"},\"TransactionStatus\": {\"OperationStatusId\": 1,\"TransactionErrors\": {\"List\": [],\"EmptyListItem\": {\"TransactionError\": {\"Source\": \"\",\"Code\": \"\",\"Level\": 0,\"Description\": \"\"}}},\"AuthStatusReason\": {\"List\": [],\"EmptyListItem\": {\"Status\": \"\",\"Code\": \"\",\"Description\": \"\"}}},\"RequestValid\": true,\"CanceledBecauseFromBack\": false}}";
    private static final String RESPONSE_UNEXPECTED =
            "{\"versionInfo\": {\"hasModuleVersionChanged\": true,\"hasApiVersionChanged\": false},\"data\": {},\"exception\": {\"name\": \"NotRegisteredException\",\"specificType\": \"CSM_PouparInvestir.NotR_PIN_Write2\",\"message\": \"R_PIN_Write2 role required\"},\"rolesInfo\": \",\"}";
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");

    @Test
    public void shouldParseTransactions() throws RequestException, ParseException {
        // given
        final int expectedTransactionAmount = 2;
        // when
        TransactionsFetchResponse objectUnderTest =
                new TransactionsFetchResponse(RESPONSE_EXPECTED);
        // then
        Assert.assertEquals(expectedTransactionAmount, objectUnderTest.getTransactions().size());
        Transaction transaction = objectUnderTest.getTransactions().get(0);
        Assert.assertEquals("2019-12-09", DATE_FORMATTER.format(transaction.getDate()));
        Assert.assertEquals(
                "09/12 COMPRA ELEC 6330968/10 REPSOL ALTO DO VALE 2070 CARTAX",
                transaction.getDescription());
        Assert.assertEquals(new BigDecimal("-28.72"), transaction.getExactAmount().getExactValue());
        Assert.assertEquals("EUR", transaction.getExactAmount().getCurrencyCode());
    }

    @Test
    public void shouldParseFetchingTransactionsUUID() throws RequestException, ParseException {
        // given
        final String expectedUUID = "193648e9-ddc6-4092-a3a5-e10b69fa2cb8";
        // when
        TransactionsFetchResponse objectUnderTest =
                new TransactionsFetchResponse(RESPONSE_EXPECTED);
        // then
        Assert.assertEquals(expectedUUID, objectUnderTest.getBankFetchingUUID());
    }

    @Test
    public void shouldDiscoverEndOfTransactionsFetching() throws RequestException, ParseException {
        // given
        // when
        TransactionsFetchResponse objectUnderTest =
                new TransactionsFetchResponse(RESPONSE_EXPECTED);
        // then
        Assert.assertTrue(objectUnderTest.isLastPage());
    }

    @Test(expected = RequestException.class)
    public void shouldThrowExceptionWhenResponseHasUnexpectedFormat() throws RequestException {
        // when
        TransactionsFetchResponse objectUnderTest =
                new TransactionsFetchResponse(RESPONSE_UNEXPECTED);
    }
}
