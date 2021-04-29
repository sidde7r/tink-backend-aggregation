package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.AvanzaInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.rpc.InvestmentAccountPortfolioResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.rpc.InvestmentTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.transactionalaccount.rpc.AccountsOverviewResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class AvanzaInvestmentFetcherTest {
    private static final String ACCOUNT_ID = "13371337";
    private static final String AUTH_SESSION_ID = "4AEF3BB1-09B5-4FA8-BF8D-555363CB3C4E";
    private static final String AUTH_SESSION_SECURITY_KEY = "318DB1E6-4477-4C27-8914-4501B724E956";
    private static final String TEST_DATA_PATH = "data/test/agents/avanza";

    private <T> T loadTestResponse(String path, Class<T> cls) {
        return SerializationUtils.deserializeFromString(
                Paths.get(TEST_DATA_PATH, path).toFile(), cls);
    }

    @Test
    @Ignore // TODO previously unmaintained -- should be fixed
    public void fetchInvestments() {
        SessionStorage sessionStorage = new SessionStorage();
        AuthSessionStorageHelper authSessionStorage = new AuthSessionStorageHelper(sessionStorage);
        sessionStorage.put(
                String.format(AvanzaConstants.StorageKeys.AUTH_SESSION_FORMAT, AUTH_SESSION_ID),
                AUTH_SESSION_SECURITY_KEY);

        AvanzaApiClient apiClient = mock(AvanzaApiClient.class);
        TemporaryStorage temporaryStorage = new TemporaryStorage();
        LocalDateTimeSource localDateTimeSource = mock(LocalDateTimeSource.class);
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
                new AvanzaInvestmentFetcher(
                        apiClient,
                        authSessionStorage,
                        temporaryStorage,
                        localDateTimeSource,
                        "oxford-preprod");
        InvestmentAccount account = fetcher.fetchAccounts().iterator().next();

        assertThat(account.getExactBalance()).isEqualTo(449894.55);

        assertThat(account.getSystemPortfolios()).hasSize(1);
        Portfolio portfolio = account.getSystemPortfolios().iterator().next();
        assertThat(portfolio.getCashValue()).isEqualTo(3719.61);
        assertThat(portfolio.getTotalValue()).isEqualTo(446175.0);
    }
}
