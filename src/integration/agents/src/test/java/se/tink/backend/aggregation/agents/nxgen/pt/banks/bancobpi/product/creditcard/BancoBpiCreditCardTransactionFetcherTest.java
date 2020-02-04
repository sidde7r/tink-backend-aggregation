package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product.creditcard;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.BancoBpiClientApi;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class BancoBpiCreditCardTransactionFetcherTest {

    private BancoBpiClientApi clientApi;
    private BancoBpiCreditCardTransactionFetcher objectUnderTest;
    private CreditCardAccount creditCardAccount;
    TransactionsFetchResponse transactionsFetchResponse;

    @Before
    public void init() {
        clientApi = Mockito.mock(BancoBpiClientApi.class);
        objectUnderTest = new BancoBpiCreditCardTransactionFetcher(clientApi);
        creditCardAccount = Mockito.mock(CreditCardAccount.class);
        Mockito.when(creditCardAccount.getAccountNumber()).thenReturn("123345");
        transactionsFetchResponse = Mockito.mock(TransactionsFetchResponse.class);
        objectUnderTest = new BancoBpiCreditCardTransactionFetcher(clientApi);
    }

    @Test
    public void shouldReturnFirstAndNotLastTransactionsPage() throws RequestException {
        // given
        Transaction transaction = Mockito.mock(Transaction.class);
        Mockito.when(transactionsFetchResponse.getBankFetchingUUID()).thenReturn("uuid");
        Mockito.when(transactionsFetchResponse.getTransactions())
                .thenReturn(Lists.newArrayList(transaction));
        Mockito.when(transactionsFetchResponse.isLastPage()).thenReturn(false);
        Mockito.when(clientApi.fetchCreditCardTransactions(creditCardAccount, 1, ""))
                .thenReturn(transactionsFetchResponse);
        // when
        PaginatorResponse result = objectUnderTest.getTransactionsFor(creditCardAccount, 1);
        // then
        Assert.assertEquals(1, result.getTinkTransactions().size());
        Assert.assertEquals(transaction, result.getTinkTransactions().iterator().next());
        Assert.assertTrue(result.canFetchMore().get());
    }

    @Test
    public void shouldReturnFirstAndLastTransactionsPage() throws RequestException {
        // given
        final String fetchingUUID = "uuid";
        Transaction transaction1 = Mockito.mock(Transaction.class);
        Transaction transaction2 = Mockito.mock(Transaction.class);
        Mockito.when(transactionsFetchResponse.getBankFetchingUUID()).thenReturn(fetchingUUID);
        Mockito.when(transactionsFetchResponse.getTransactions())
                .thenReturn(Lists.newArrayList(transaction1));
        Mockito.when(transactionsFetchResponse.isLastPage()).thenReturn(false);
        TransactionsFetchResponse transactionsFetchResponse2 =
                Mockito.mock(TransactionsFetchResponse.class);
        Mockito.when(transactionsFetchResponse2.getBankFetchingUUID()).thenReturn(fetchingUUID);
        Mockito.when(transactionsFetchResponse2.getTransactions())
                .thenReturn(Lists.newArrayList(transaction2));
        Mockito.when(transactionsFetchResponse2.isLastPage()).thenReturn(true);
        Mockito.when(clientApi.fetchCreditCardTransactions(creditCardAccount, 1, ""))
                .thenReturn(transactionsFetchResponse);
        Mockito.when(clientApi.fetchCreditCardTransactions(creditCardAccount, 2, fetchingUUID))
                .thenReturn(transactionsFetchResponse2);
        // when
        PaginatorResponse result1 = objectUnderTest.getTransactionsFor(creditCardAccount, 1);
        PaginatorResponse result2 = objectUnderTest.getTransactionsFor(creditCardAccount, 2);
        // then
        Assert.assertEquals(1, result1.getTinkTransactions().size());
        Assert.assertEquals(transaction1, result1.getTinkTransactions().iterator().next());
        Assert.assertTrue(result1.canFetchMore().get());
        Assert.assertEquals(1, result2.getTinkTransactions().size());
        Assert.assertEquals(transaction2, result2.getTinkTransactions().iterator().next());
        Assert.assertFalse(result2.canFetchMore().get());
    }

    @Test(expected = BankServiceException.class)
    public void shouldThrowBankServiceException() throws RequestException {
        // given
        Mockito.when(clientApi.fetchCreditCardTransactions(creditCardAccount, 1, ""))
                .thenThrow(new RequestException(""));
        // when
        objectUnderTest.getTransactionsFor(creditCardAccount, 1);
    }
}
