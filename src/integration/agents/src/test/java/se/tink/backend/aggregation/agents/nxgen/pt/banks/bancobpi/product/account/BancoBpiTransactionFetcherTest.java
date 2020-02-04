package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product.account;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.BancoBpiClientApi;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiAccountsContext;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.TransactionalAccountBaseInfo;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class BancoBpiTransactionFetcherTest {

    private static final String MODULE_VERSION = "testModuleVersion";
    private static final String ACCOUNT_ORDER = "testAccountOrder";
    private static final String ACCOUNT_TYPE = "testAccountType";
    private static final String ACCOUNT_INTERNAL_ID = "12345";

    private BancoBpiAccountsContext accountsContext;
    private TransactionalAccount transactionalAccount;
    private BancoBpiClientApi clientApi;
    private BancoBpiTransactionFetcher objectUnderTest;

    @Before
    public void init() {
        clientApi = Mockito.mock(BancoBpiClientApi.class);
        accountsContext = new BancoBpiAccountsContext();
        objectUnderTest = new BancoBpiTransactionFetcher(clientApi);
        initAccountObjects();
    }

    private void initAccountObjects() {
        transactionalAccount = Mockito.mock(TransactionalAccount.class);
        Mockito.when(transactionalAccount.getAccountNumber()).thenReturn(ACCOUNT_INTERNAL_ID);
        TransactionalAccountBaseInfo accountBaseInfo = new TransactionalAccountBaseInfo();
        accountBaseInfo.setInternalAccountId(ACCOUNT_INTERNAL_ID);
        accountBaseInfo.setOrder(ACCOUNT_ORDER);
        accountBaseInfo.setType(ACCOUNT_TYPE);
        accountsContext.getAccountInfo().add(accountBaseInfo);
    }

    @Test
    public void getTransactionsShouldReturnTransactionsWithMoreAvailable() throws RequestException {
        // given
        TransactionsFetchResponse response = Mockito.mock(TransactionsFetchResponse.class);
        Mockito.when(response.isLastPage()).thenReturn(false);
        Transaction transaction1 = Mockito.mock(Transaction.class);
        Mockito.when(response.getTransactions()).thenReturn(Lists.newArrayList(transaction1));
        Mockito.when(response.getBankFetchingUUID()).thenReturn("fetchingUUID");
        Mockito.when(clientApi.fetchAccountTransactions("", 1, transactionalAccount))
                .thenReturn(response);
        // when
        PaginatorResponse result = objectUnderTest.getTransactionsFor(transactionalAccount, 1);
        // then
        Assert.assertFalse(result.getTinkTransactions().isEmpty());
        Assert.assertEquals(1, result.getTinkTransactions().size());
        Assert.assertTrue(result.canFetchMore().get());
    }

    @Test
    public void getTransactionsForShouldReturnTransactionsWithNoMoreAvailable()
            throws RequestException {
        // given
        final String fetchingUUID = "fetchingUUID";
        TransactionsFetchResponse response = Mockito.mock(TransactionsFetchResponse.class);
        Mockito.when(response.isLastPage()).thenReturn(false).thenReturn(true);
        Transaction transaction1 = Mockito.mock(Transaction.class);
        Mockito.when(response.getTransactions()).thenReturn(Lists.newArrayList(transaction1));
        Mockito.when(response.getBankFetchingUUID()).thenReturn(fetchingUUID);
        Mockito.when(clientApi.fetchAccountTransactions("", 1, transactionalAccount))
                .thenReturn(response);
        Mockito.when(clientApi.fetchAccountTransactions(fetchingUUID, 2, transactionalAccount))
                .thenReturn(response);
        // when
        objectUnderTest.getTransactionsFor(transactionalAccount, 1);
        PaginatorResponse result = objectUnderTest.getTransactionsFor(transactionalAccount, 2);
        // then
        Assert.assertFalse(result.getTinkTransactions().isEmpty());
        Assert.assertEquals(1, result.getTinkTransactions().size());
        Assert.assertTrue(result.canFetchMore().get());
    }
}
