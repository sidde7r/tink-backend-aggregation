package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher;

import java.util.Collection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.BancoPopularTestBase;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.agents.rpc.AccountTypes;

public class BancoPopularAccountFetcherTest extends BancoPopularTestBase {
    private BancoPopularAccountFetcher accountFetcher;

    @Before
    public void setUp() throws Exception {
        super.setup();
        accountFetcher = new BancoPopularAccountFetcher(bankClient, persistentStorage);
    }

    @Test
    public void fetchAccounts() throws Exception {
        authenticate();
        Collection<TransactionalAccount> accounts = accountFetcher.fetchAccounts();

        Assert.assertEquals(1, accounts.size());
        Assert.assertEquals(AccountTypes.CHECKING, accounts.iterator().next().getType());
    }
}
