package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.BancoPopularTestBase;

public class BancoPopularLoanFetcherTest extends BancoPopularTestBase {
    private BancoPopularLoanFetcher loanFetcher;

    @Before
    public void setUp() throws Exception {
        setup();
        loanFetcher = new BancoPopularLoanFetcher(bankClient, persistentStorage);
    }

    @Test
    public void fetchLoans() throws Exception {
        authenticate();
        loanFetcher.fetchAccounts();
    }
}
