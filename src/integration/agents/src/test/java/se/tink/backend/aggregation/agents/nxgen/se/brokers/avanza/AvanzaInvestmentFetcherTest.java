package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import org.junit.Test;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.AvanzaInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.rpc.InvestmentAccountPortfolioResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.rpc.InvestmentTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.transactionalaccount.rpc.AccountsOverviewResponse;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class AvanzaInvestmentFetcherTest {
    private static String ACCOUNT_ID = "13371337";
    private static String AUTH_SESSION_ID = "4AEF3BB1-09B5-4FA8-BF8D-555363CB3C4E";
    private static String AUTH_SESSION_SECURITY_KEY = "318DB1E6-4477-4C27-8914-4501B724E956";
    final String TEST_DATA_PATH = "data/test/agents/avanza";

    private <T> T loadTestResponse(String path, Class<T> cls) {
        return SerializationUtils.deserializeFromString(
                Paths.get(TEST_DATA_PATH, path).toFile(), cls);
    }

    @Test
    public void fetchInvestments() {
        AvanzaAuthSessionStorage authSessionStorage = new AvanzaAuthSessionStorage();
        authSessionStorage.put(AUTH_SESSION_ID, AUTH_SESSION_SECURITY_KEY);

        AvanzaApiClient apiClient = mock(AvanzaApiClient.class);
        TemporaryStorage temporaryStorage = new TemporaryStorage();
        when(apiClient.fetchAccounts(eq(AUTH_SESSION_ID)))
                .thenReturn(
                        loadTestResponse("account-overview.json", AccountsOverviewResponse.class));
        when(apiClient.fetchInvestmentAccountPortfolio(eq(ACCOUNT_ID), eq(AUTH_SESSION_ID)))
                .thenReturn(
                        loadTestResponse(
                                "positions.json", InvestmentAccountPortfolioResponse.class));
        when(apiClient.fetchInvestmentTransactions(
                        eq(ACCOUNT_ID), any(String.class), eq(AUTH_SESSION_ID)))
                .thenReturn(new InvestmentTransactionsResponse());

        AvanzaInvestmentFetcher fetcher =
                new AvanzaInvestmentFetcher(apiClient, authSessionStorage, temporaryStorage);
        InvestmentAccount account = fetcher.fetchAccounts().iterator().next();

        assertEquals(Amount.inSEK(449894.55), account.getBalance());

        assertEquals(1, account.getPortfolios().size());
        Portfolio portfolio = account.getPortfolios().iterator().next();
        assertEquals(new Double(3719.61), portfolio.getCashValue());
        assertEquals(new Double(446175.0), portfolio.getTotalValue());
    }
}
