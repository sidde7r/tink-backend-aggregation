package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.investment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Collection;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.JyskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.JyskeBankApiClientMockWrapper;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.JyskeBankTestData.InvestmentTestData;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;

public class InvestmentFetcherTest {
    private JyskeBankApiClientMockWrapper apiClientMockWrapper;
    private JyskeBankInvestmentFetcher fetcher;

    @Before
    public void before() {
        JyskeBankApiClient apiClient = mock(JyskeBankApiClient.class);
        apiClientMockWrapper = new JyskeBankApiClientMockWrapper(apiClient);
        fetcher = new JyskeBankInvestmentFetcher(apiClient);
    }

    @Test
    public void shouldFetchInvestments() {
        // given
        apiClientMockWrapper.mockFetchInvestmentsUsingFile(
                InvestmentTestData.INVESTMENT_ACCOUNTS_FILE);

        // when
        Collection<InvestmentAccount> investments = fetcher.fetchAccounts();

        // then
        validateFirstInvestment(investments);
    }

    private void validateFirstInvestment(Collection<InvestmentAccount> investments) {
        Optional<InvestmentAccount> investment1 =
                investments.stream()
                        .filter(
                                i ->
                                        InvestmentTestData.INVESTMENT_1_ID.equals(
                                                i.getIdModule().getUniqueId()))
                        .findAny();
        assertThat(investment1.isPresent()).isTrue();
        assertThat(investment1.get().getIdModule().getAccountNumber()).isEqualTo("87123");
        assertThat(investment1.get().getName()).isEqualTo("Åbent Depot");
    }
}
