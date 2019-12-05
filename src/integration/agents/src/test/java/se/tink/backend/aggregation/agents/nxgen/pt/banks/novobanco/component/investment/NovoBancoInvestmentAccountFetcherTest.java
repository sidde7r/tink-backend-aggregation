package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.component.investment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.component.investment.detail.InvestmentTestData.FAILED_CALL;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.component.investment.detail.InvestmentTestData.INVESTMENTS_AVAILABLE;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoApiClient;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.component.investment.detail.InvestmentTestData;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.detail.InvestmentAccountDto;
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
                        Collections.singletonList(
                                InvestmentTestData.getResponse(INVESTMENTS_AVAILABLE)));
        NovoBancoInvestmentAccountFetcher fetcher =
                new NovoBancoInvestmentAccountFetcher(apiClient);

        // when & then
        assertFalse(fetcher.fetchAccounts().isEmpty());
    }

    @Test
    public void test_InvestmentsAvailable_AccountsMappedCorrectly() {
        // given
        NovoBancoApiClient apiClient = mock(NovoBancoApiClient.class);
        when(apiClient.getInvestments())
                .thenReturn(
                        Collections.singletonList(
                                InvestmentTestData.getResponse(INVESTMENTS_AVAILABLE)));
        NovoBancoInvestmentAccountFetcher fetcher =
                new NovoBancoInvestmentAccountFetcher(apiClient);

        // when
        Collection<InvestmentAccount> accounts = fetcher.fetchAccounts();

        // then
        assertEquals(1, accounts.size());
        accounts.forEach(this::accountEquals);
    }

    private void accountEquals(InvestmentAccount account) {
        InvestmentAccountDto expected =
                InvestmentTestData.getReferenceAccountDto(account.getAccountNumber());

        assertEquals(expected.getAccountNumber(), account.getAccountNumber());
        assertTrue(account.isUniqueIdentifierEqual(expected.getUniqueIdentifier()));
        assertEquals(expected.getDescription(), account.getName());
        assertEquals(expected.getPortfolios().size(), account.getSystemPortfolios().size());
        assertPortfoliosEqual(expected, account.getSystemPortfolios());
    }

    private void assertPortfoliosEqual(
            InvestmentAccountDto expected, List<Portfolio> systemPortfolios) {
        assertEquals(expected.getPortfolios().size(), systemPortfolios.size());
        expected.getPortfolios()
                .forEach(
                        expectedPortfolio -> {
                            assertPortfolioEquals(expectedPortfolio, systemPortfolios);
                        });
    }

    private void assertPortfolioEquals(
            InvestmentAccountDto.PortfolioDto expectedPortfolio, List<Portfolio> portfolios) {
        Portfolio portfolio =
                getMatchingPortfolio(expectedPortfolio.getUniqueIdentifier(), portfolios);
        assertNotNull(
                "Could not find Portfolio matching unique id of a reference portfolio", portfolio);
        assertEquals(expectedPortfolio.getCashValue(), portfolio.getCashValue());
        assertEquals(expectedPortfolio.getTotalProfit(), portfolio.getTotalProfit());
        assertEquals(expectedPortfolio.getTotalValue(), portfolio.getTotalValue());
    }

    private Portfolio getMatchingPortfolio(String id, List<Portfolio> portfolios) {
        return portfolios.stream()
                .filter(p -> id.equals(p.getUniqueIdentifier()))
                .findFirst()
                .orElse(null);
    }
}
