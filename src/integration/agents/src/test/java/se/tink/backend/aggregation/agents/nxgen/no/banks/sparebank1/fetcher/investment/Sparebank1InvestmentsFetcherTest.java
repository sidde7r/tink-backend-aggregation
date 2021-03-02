package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.investment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.TestHelper.mockHttpClient;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.Iterator;
import org.junit.Test;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1ApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.investment.rpc.PortfolioEntitiesResponse;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class Sparebank1InvestmentsFetcherTest {
    private static final String RESOURCE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/banks/sparebank1/resources";

    @Test
    public void fetchAccountsShouldReturnTinkInvestments() {
        // given
        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        HttpResponse response = mock(HttpResponse.class);
        TinkHttpClient client = mockHttpClient(requestBuilder, response);
        when(requestBuilder.get(PortfolioEntitiesResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(RESOURCE_PATH, "investments_list_response.json").toFile(),
                                PortfolioEntitiesResponse.class));

        Sparebank1ApiClient apiClient = new Sparebank1ApiClient(client, "dummyBankId");
        Sparebank1InvestmentsFetcher fetcher = new Sparebank1InvestmentsFetcher(apiClient);

        // when
        Collection<InvestmentAccount> accounts = fetcher.fetchAccounts();

        // then
        assertThat(accounts).hasSize(2);
        Iterator<InvestmentAccount> iterator = accounts.iterator();
        InvestmentAccount account1 = iterator.next();
        assertThat(account1.getIdModule().getUniqueId()).isEqualTo("portfolio1id");
        assertThat(account1.getSystemPortfolios()).isNotEmpty();
        Portfolio portfolio1 = account1.getSystemPortfolios().get(0);
        assertPortfolio(portfolio1, "portfolio1id", 499.94, -0.06, 1);
        assertInstrument(
                portfolio1.getInstruments().get(0),
                "intrument1id",
                "intrument1isin",
                "intrument1",
                499.94,
                500,
                0.2005000000,
                -0.06);

        InvestmentAccount account2 = iterator.next();
        assertThat(account2.getIdModule().getUniqueId()).isEqualTo("portfolio2id");
        assertThat(account2.getSystemPortfolios()).isNotEmpty();
        Portfolio portfolio2 = account2.getSystemPortfolios().get(0);
        assertPortfolio(portfolio2, "portfolio2id", 1231.65, 600.23, 2);
    }

    private void assertPortfolio(
            Portfolio portfolio,
            String id,
            double totalMarketValue,
            double totalProfit,
            int instrumentSize) {
        assertThat(portfolio.getUniqueIdentifier()).isEqualTo(id);
        assertThat(portfolio.getTotalValue()).isEqualTo(totalMarketValue);
        assertThat(portfolio.getTotalProfit()).isEqualTo(totalProfit);
        assertThat(portfolio.getInstruments()).hasSize(instrumentSize);
        assertThat(portfolio.getCashValue()).isEqualTo(0.0);
    }

    private void assertInstrument(
            Instrument instrument,
            String id,
            String isin,
            String name,
            double marketValue,
            double price,
            double quantity,
            double profit) {
        assertThat(instrument.getUniqueIdentifier()).isEqualTo(id);
        assertThat(instrument.getIsin()).isEqualTo(isin);
        assertThat(instrument.getName()).isEqualTo(name);
        assertThat(instrument.getMarketValue()).isEqualTo(marketValue);
        assertThat(instrument.getPrice()).isEqualTo(price);
        assertThat(instrument.getQuantity()).isEqualTo(quantity);
        assertThat(instrument.getProfit()).isEqualTo(profit);
    }
}
