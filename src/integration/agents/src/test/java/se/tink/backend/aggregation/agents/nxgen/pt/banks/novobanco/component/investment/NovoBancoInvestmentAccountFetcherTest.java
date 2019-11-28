package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.component.investment;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.component.investment.detail.InvestmentTestData.FAILED_CALL;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.component.investment.detail.InvestmentTestData.SUCCESSFUL_CALL;

import java.util.Collection;
import java.util.Collections;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoApiClient;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.component.investment.detail.InvestmentTestData;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.NovoBancoInvestmentAccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;

public class NovoBancoInvestmentAccountFetcherTest {

    @Test
    public void shouldReturnEmptyCollectionIfNoInvestmentsAvailable() {
        // given
        NovoBancoApiClient apiClient = mock(NovoBancoApiClient.class);
        when(apiClient.getInvestments()).thenReturn(Collections.emptyList());
        NovoBancoInvestmentAccountFetcher fetcher =
                new NovoBancoInvestmentAccountFetcher(apiClient);

        // when
        Collection<InvestmentAccount> accounts = fetcher.fetchAccounts();

        // then
        assertTrue(accounts.isEmpty());
    }

    @Test
    public void shouldReturnEmptyCollectionIfErroredResponse() {
        NovoBancoApiClient apiClient = mock(NovoBancoApiClient.class);
        when(apiClient.getInvestments())
                .thenReturn(Collections.singletonList(InvestmentTestData.getResponse(FAILED_CALL)));
        NovoBancoInvestmentAccountFetcher fetcher =
                new NovoBancoInvestmentAccountFetcher(apiClient);

        // when & then
        assertTrue(fetcher.fetchAccounts().isEmpty());
    }

    @Test
    public void shouldReturnNonEmptyCollectionIfInvestmentsAvailable() {
        // given
        NovoBancoApiClient apiClient = mock(NovoBancoApiClient.class);
        when(apiClient.getInvestments())
                .thenReturn(
                        Collections.singletonList(InvestmentTestData.getResponse(SUCCESSFUL_CALL)));
        NovoBancoInvestmentAccountFetcher fetcher =
                new NovoBancoInvestmentAccountFetcher(apiClient);

        // when & then
        assertFalse(fetcher.fetchAccounts().isEmpty());
    }
}
