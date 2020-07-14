package se.tink.backend.aggregation.workers.operation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Account;

public class AccountDataCacheTest {
    private static final String DUMMY_ACCOUNT_ID_0 = "dummy0";
    private static final String DUMMY_ACCOUNT_ID_1 = "dummy1";

    @Test
    public void testNonFilteredCache() {
        AccountDataCache accountDataCache = new AccountDataCache();

        Account account0 = mockAccount(DUMMY_ACCOUNT_ID_0);
        Account account1 = mockAccount(DUMMY_ACCOUNT_ID_1);

        accountDataCache.cacheAccount(account0);
        accountDataCache.cacheAccount(account1);

        Assert.assertEquals(accountDataCache.getCurrentAccounts().size(), 2);

        List<Account> expectedCachedAccounts = Arrays.asList(account0, account1);

        Assert.assertEquals(expectedCachedAccounts, accountDataCache.getCurrentAccounts());
    }

    @Test
    public void testFilteredCache() {
        AccountDataCache accountDataCache = new AccountDataCache();

        accountDataCache.addFilter(this::restrictDummyAccount0);

        Account account0 = mockAccount(DUMMY_ACCOUNT_ID_0);
        Account account1 = mockAccount(DUMMY_ACCOUNT_ID_1);

        accountDataCache.cacheAccount(account0);
        accountDataCache.cacheAccount(account1);

        Assert.assertEquals(accountDataCache.getCurrentAccounts().size(), 1);

        List<Account> expectedCachedAccounts = Collections.singletonList(account1);

        Assert.assertEquals(expectedCachedAccounts, accountDataCache.getCurrentAccounts());
    }

    @Test
    public void testClearCache() {
        AccountDataCache accountDataCache = new AccountDataCache();

        accountDataCache.cacheAccount(mockAccount(DUMMY_ACCOUNT_ID_0));
        accountDataCache.cacheAccount(mockAccount(DUMMY_ACCOUNT_ID_1));

        Assert.assertEquals(accountDataCache.getCurrentAccounts().size(), 2);

        accountDataCache.clear();
        Assert.assertEquals(accountDataCache.getCurrentAccounts().size(), 0);
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
