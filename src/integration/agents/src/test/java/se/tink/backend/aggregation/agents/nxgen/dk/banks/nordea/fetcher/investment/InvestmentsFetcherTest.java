package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.investment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Collection;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkApiClientMockWrapper;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaTestData.InvestmentTestData;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;

public class InvestmentsFetcherTest {

    private NordeaDkApiClientMockWrapper apiClientMockWrapper;
    private NordeaInvestmentFetcher fetcher;

    @Before
    public void before() {
        NordeaDkApiClient apiClient = mock(NordeaDkApiClient.class);
        apiClientMockWrapper = new NordeaDkApiClientMockWrapper(apiClient);
        fetcher = new NordeaInvestmentFetcher(apiClient);
    }

    @Test
    public void shouldFetchInvestments() {
        // given
        apiClientMockWrapper.mockFetchInvestmentsUsingFile(
                InvestmentTestData.INVESTMENT_ACCOUNTS_FILE);
        // when
        Collection<InvestmentAccount> investments = fetcher.fetchAccounts();
        // then
        assertThat(investments).hasSize(3);
        // and
        validateFirstInvestment(investments);
        validatePension(investments);
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
        assertThat(investment1.get().getIdModule().getAccountNumber()).isEqualTo("1234 123 456");
        assertThat(investment1.get().getName()).isEqualTo("NORA depot");
        assertThat(investment1.get().getSystemPortfolios()).hasSize(1);
        assertThat(investment1.get().getSystemPortfolios().get(0).getInstruments()).hasSize(1);
        Instrument instrument =
                investment1.get().getSystemPortfolios().get(0).getInstruments().get(0);
        assertThat(instrument.getUniqueIdentifier())
                .isEqualTo("DKWRAP:1234123456+BMS-DK9876543210-UNOT-null");
        assertThat(instrument.getIsin()).isEqualTo("DK9876543210");
        assertThat(instrument.getType()).isEqualByComparingTo(Instrument.Type.FUND);
        assertThat(instrument.getName()).isEqualTo("Nora Fund Five (DKK)");
        assertThat(instrument.getCurrency()).isEqualTo("DKK");
        assertThat(instrument.getMarketValue()).isEqualByComparingTo(9785.3);
        assertThat(instrument.getPrice()).isEqualByComparingTo(0.98);
        assertThat(instrument.getProfit()).isEqualByComparingTo(-98.24);
        assertThat(instrument.getQuantity()).isEqualByComparingTo(9985.0);
    }

    private void validatePension(Collection<InvestmentAccount> investments) {
        Optional<InvestmentAccount> pension =
                investments.stream()
                        .filter(
                                i ->
                                        InvestmentTestData.PENSION_ID.equals(
                                                i.getIdModule().getUniqueId()))
                        .findAny();
        assertThat(pension.isPresent()).isTrue();
        assertThat(pension.get().getIdModule().getAccountNumber()).isEqualTo("9999 999 999");
        assertThat(pension.get().getName()).isEqualTo("Kapitalpension-pulje");
        assertThat(pension.get().getSystemPortfolios()).hasSize(1);
        assertThat(pension.get().getSystemPortfolios().get(0).getInstruments()).isEmpty();
        assertThat(pension.get().getSystemPortfolios().get(0).getTotalValue()).isEqualTo(57148.21);
        assertThat(pension.get().getSystemPortfolios().get(0).getType())
                .isEqualTo(Portfolio.Type.PENSION);
    }
}
