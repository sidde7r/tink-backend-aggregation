package se.tink.libraries.account_data_cache;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.models.Transaction;

public class AccountDataCacheTest {
    private static final String DUMMY_ACCOUNT_ID_0 = "dummy0";
    private static final String DUMMY_ACCOUNT_ID_1 = "dummy1";

    @Test
    public void testCacheSameAccount() {
        AccountDataCache accountDataCache = new AccountDataCache();

        Account account0 = mockAccount(DUMMY_ACCOUNT_ID_0);
        accountDataCache.cacheAccount(account0);
        Assert.assertEquals(accountDataCache.getFilteredAccounts().size(), 1);

        accountDataCache.cacheAccount(account0);
        Assert.assertEquals(accountDataCache.getFilteredAccounts().size(), 1);
    }

    @Test
    public void testNonFilteredCache() {
        AccountDataCache accountDataCache = new AccountDataCache();

        Account account0 = mockAccount(DUMMY_ACCOUNT_ID_0);
        Account account1 = mockAccount(DUMMY_ACCOUNT_ID_1);

        accountDataCache.cacheAccount(account0);
        accountDataCache.cacheAccount(account1);

        Assert.assertEquals(accountDataCache.getFilteredAccounts().size(), 2);
        assertThat(accountDataCache.getFilteredAccounts())
                .containsExactlyInAnyOrder(account0, account1);
    }

    @Test
    public void testFilteredCache() {
        AccountDataCache accountDataCache = new AccountDataCache();

        accountDataCache.addFilter(
                this::restrictDummyAccount0, FilterReason.DATA_FETCHING_RESTRICTIONS_ACCOUNT_TYPE);

        Account account0 = mockAccount(DUMMY_ACCOUNT_ID_0);
        Account account1 = mockAccount(DUMMY_ACCOUNT_ID_1);

        accountDataCache.cacheAccount(account0);
        accountDataCache.cacheAccount(account1);

        Assert.assertEquals(accountDataCache.getFilteredAccounts().size(), 1);
        assertThat(accountDataCache.getFilteredAccounts()).containsExactlyInAnyOrder(account1);
    }

    @Test
    public void testClearCache() {
        AccountDataCache accountDataCache = new AccountDataCache();

        accountDataCache.cacheAccount(mockAccount(DUMMY_ACCOUNT_ID_0));
        accountDataCache.cacheAccount(mockAccount(DUMMY_ACCOUNT_ID_1));

        Assert.assertEquals(accountDataCache.getFilteredAccounts().size(), 2);

        accountDataCache.clear();
        Assert.assertEquals(accountDataCache.getFilteredAccounts().size(), 0);
    }

    @Test
    public void testCacheTransactions() {
        AccountDataCache accountDataCache = new AccountDataCache();

        Account account0 = mockAccount(DUMMY_ACCOUNT_ID_0);
        Account account1 = mockAccount(DUMMY_ACCOUNT_ID_1);
        accountDataCache.cacheAccount(account0);
        accountDataCache.cacheAccount(account1);

        List<Transaction> account0Transactions =
                Arrays.asList(new Transaction(), new Transaction());
        List<Transaction> account1Transactions =
                Arrays.asList(new Transaction(), new Transaction());

        accountDataCache.cacheTransactions(DUMMY_ACCOUNT_ID_0, account0Transactions);
        accountDataCache.cacheTransactions(DUMMY_ACCOUNT_ID_1, account1Transactions);

        accountDataCache.setProcessedTinkAccountId(DUMMY_ACCOUNT_ID_0, DUMMY_ACCOUNT_ID_0);
        accountDataCache.setProcessedTinkAccountId(DUMMY_ACCOUNT_ID_1, DUMMY_ACCOUNT_ID_1);

        Map<Account, List<Transaction>> transactionsByAccountToBeProcessed =
                accountDataCache.getTransactionsByAccountToBeProcessed();

        assertThat(transactionsByAccountToBeProcessed.get(account0))
                .containsAll(account0Transactions);
        assertThat(transactionsByAccountToBeProcessed.get(account1))
                .containsAll(account1Transactions);

        List<Transaction> transactionsToBeProcessed =
                accountDataCache.getTransactionsToBeProcessed();

        List<Transaction> expectedTransactions = new ArrayList<>(account0Transactions);
        expectedTransactions.addAll(account1Transactions);

        assertThat(transactionsToBeProcessed).containsAll(expectedTransactions);
    }

    private boolean restrictDummyAccount0(Account account) {
        return !DUMMY_ACCOUNT_ID_0.equals(account.getBankId());
    }

    private Account mockAccount(String uniqueId) {
        Account account = Mockito.mock(Account.class);
        Mockito.when(account.getBankId()).thenReturn(uniqueId);
        return account;
    }
}
