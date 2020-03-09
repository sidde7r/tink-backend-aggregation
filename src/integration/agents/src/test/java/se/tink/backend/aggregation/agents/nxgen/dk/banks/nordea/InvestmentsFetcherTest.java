package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.investment.NordeaInvestmentFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;

public class InvestmentsFetcherTest {

    private NordeaInvestmentFetcher fetcher;

    @Before
    public void before() {
        fetcher = new NordeaInvestmentFetcher(NordeaDkTestUtils.mockApiClient());
    }

    @Test
    public void shouldFetchInvestments() {
        // when
        Collection<InvestmentAccount> investments = fetcher.fetchAccounts();
        // then
        assertThat(investments).hasSize(2);
        // and
        Optional<InvestmentAccount> investment1 =
                investments.stream()
                        .filter(
                                i ->
                                        NordeaTestData.INVESTMENT_1_ID.equals(
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
}
