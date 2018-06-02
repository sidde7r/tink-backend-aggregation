package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.BancoPopularTestBase;

public class BancoPopularInvestmentFetcherTest extends BancoPopularTestBase {
    private BancoPopularInvestmentFetcher investmentFetcher;

    @Before
    public void setUp() throws Exception {
        setup();
        investmentFetcher = new BancoPopularInvestmentFetcher(bankClient, persistentStorage);
    }

    @Test
    public void fetchLoans() throws Exception {
        authenticate();
        investmentFetcher.fetchAccounts();
    }
}
