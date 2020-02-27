package se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.product.account;

import com.google.common.collect.Lists;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.BPostBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.entity.BPostBankAuthContext;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class BPostBankTransactionsFetcherTest {

    private BPostBankApiClient apiClient;
    private BPostBankAuthContext authContext;
    private TransactionalAccount transactionalAccount;
    private Random random = new Random();
    private BPostBankTransactionsFetcher objectUnderTest;

    @Before
    public void init() {
        apiClient = Mockito.mock(BPostBankApiClient.class);
        authContext = Mockito.mock(BPostBankAuthContext.class);
        transactionalAccount = Mockito.mock(TransactionalAccount.class);
        objectUnderTest = new BPostBankTransactionsFetcher(apiClient, authContext);
    }

    @Test
    public void shouldReturnTransactionsWithCanFetchMoreSetToFalse() throws RequestException {
        // given
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        BPostBankTransactionDTO transactionDTO = createMockedDummyTransactionDTO();
        Mockito.when(
                        apiClient.fetchAccountTransactions(
                                transactionalAccount,
                                1,
                                BPostBankTransactionsFetcher.TRANSACTIONS_PAGE_SIZE,
                                authContext))
                .thenReturn(Lists.newArrayList(transactionDTO));
        // when
        PaginatorResponse result = objectUnderTest.getTransactionsFor(transactionalAccount, 1);
        // then
        Assert.assertEquals(1, result.getTinkTransactions().size());
        Assert.assertFalse(result.canFetchMore().get());
        Transaction transaction = result.getTinkTransactions().iterator().next();
        Assert.assertEquals(transactionDTO.categoryId, transaction.getDescription());
        Assert.assertEquals(
                transactionDTO.transactionAmount, transaction.getExactAmount().getExactValue());
        Assert.assertEquals(
                transactionDTO.transactionCurrency, transaction.getExactAmount().getCurrencyCode());
        Assert.assertEquals(
                transactionDTO.bookingDateTime.split("T")[0],
                dateFormatter.format(transaction.getDate()));
    }

    @Test
    public void shouldReturnTransactionsWithCanFetchMoreSetToTrue() throws RequestException {
        // given
        List<BPostBankTransactionDTO> transactionDTOs =
                new ArrayList<>(BPostBankTransactionsFetcher.TRANSACTIONS_PAGE_SIZE);
        for (int i = 0; i < BPostBankTransactionsFetcher.TRANSACTIONS_PAGE_SIZE; i++) {
            transactionDTOs.add(createMockedDummyTransactionDTO());
        }
        Mockito.when(
                        apiClient.fetchAccountTransactions(
                                transactionalAccount,
                                1,
                                BPostBankTransactionsFetcher.TRANSACTIONS_PAGE_SIZE,
                                authContext))
                .thenReturn(transactionDTOs);
        // when
        PaginatorResponse result = objectUnderTest.getTransactionsFor(transactionalAccount, 1);
        // then
        Assert.assertEquals(
                BPostBankTransactionsFetcher.TRANSACTIONS_PAGE_SIZE,
                result.getTinkTransactions().size());
        Assert.assertTrue(result.canFetchMore().get());
    }

    private BPostBankTransactionDTO createMockedDummyTransactionDTO() {
        BPostBankTransactionDTO transactionDTO = new BPostBankTransactionDTO();
        transactionDTO.bookingDateTime = "2019-12-11T00:00:00+0000";
        transactionDTO.categoryId = "categoryId";
        transactionDTO.counterpartyAccount = "12345678901";
        transactionDTO.counterpartyName = "TINK AB";
        transactionDTO.identifier = "20191211µB9L11REWUO57LMDVµBE92000450043523µ0000003";
        transactionDTO.transactionAmount = new BigDecimal(random.nextInt());
        transactionDTO.transactionCurrency = "EUR";
        return transactionDTO;
    }
}
