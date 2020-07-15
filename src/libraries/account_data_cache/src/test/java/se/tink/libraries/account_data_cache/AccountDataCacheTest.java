package se.tink.libraries.account_data_cache;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Account;

public class AccountDataCacheTest {
    private static final String DUMMY_ACCOUNT_ID_0 = "dummy0";
    private static final String DUMMY_ACCOUNT_ID_1 = "dummy1";

    @Test
    public void testCacheSameAccount() {
        AccountDataCache accountDataCache = new AccountDataCache();

        Account account0 = mockAccount(DUMMY_ACCOUNT_ID_0);
        accountDataCache.cacheAccount(account0);
        Assert.assertEquals(accountDataCache.getCurrentAccounts().size(), 1);

        accountDataCache.cacheAccount(account0);
        Assert.assertEquals(accountDataCache.getCurrentAccounts().size(), 1);
    }

    @Test
    public void testNonFilteredCache() {
        AccountDataCache accountDataCache = new AccountDataCache();

        Account account0 = mockAccount(DUMMY_ACCOUNT_ID_0);
        Account account1 = mockAccount(DUMMY_ACCOUNT_ID_1);

        accountDataCache.cacheAccount(account0);
        accountDataCache.cacheAccount(account1);

        Assert.assertEquals(accountDataCache.getCurrentAccounts().size(), 2);
        assertThat(accountDataCache.getCurrentAccounts())
                .containsExactlyInAnyOrder(account0, account1);
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
        assertThat(accountDataCache.getCurrentAccounts()).containsExactlyInAnyOrder(account1);
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
