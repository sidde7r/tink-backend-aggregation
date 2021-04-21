package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.investment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import java.util.Collection;
import javax.ws.rs.core.MediaType;
import org.junit.Test;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.SparebankenVestApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.investment.rpc.FetchFundInvestmentsResponse;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SparebankenVestInvestmentsFetcherTest {

    private static final String RESOURCE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/banks/sparebankenvest/resources";

    @Test
    public void fetchAccountsShouldReturnTinkInvestments() {
        // given
        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        HttpResponse response = mock(HttpResponse.class);
        TinkHttpClient client = mockHttpClient(requestBuilder, response);
        when(requestBuilder.get(FetchFundInvestmentsResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(RESOURCE_PATH, "investments.json").toFile(),
                                FetchFundInvestmentsResponse.class));

        SparebankenVestApiClient apiClient = new SparebankenVestApiClient(client);
        SparebankenVestInvestmentsFetcher fetcher =
                SparebankenVestInvestmentsFetcher.create(apiClient);

        // when
        Collection<InvestmentAccount> accounts = fetcher.fetchAccounts();

        // then
        assertThat(accounts).hasSize(2);
        InvestmentAccount account1 = getInvestmentAccount(accounts, "036259009999");
        assertThat(account1.getIdModule().getUniqueId()).isEqualTo("036259009999");
        assertThat(account1.getSystemPortfolios()).isNotEmpty();
        Portfolio portfolio1 = account1.getSystemPortfolios().get(0);
        assertPortfolio(portfolio1, "036259009999", 376740.0, 0.00, 1, 376740.0);
        assertInstrument(
                portfolio1.getInstruments().get(0),
                "NO0010099999",
                "NO0010099999",
                "EQUINOR ASA",
                376740.0,
                0,
                1,
                null);

        InvestmentAccount account2 = getInvestmentAccount(accounts, "9999999");
        assertThat(account2.getIdModule().getUniqueId()).isEqualTo("9999999");
        assertThat(account2.getSystemPortfolios()).isNotEmpty();
        Portfolio portfolio2 = account2.getSystemPortfolios().get(0);
        assertPortfolio(portfolio2, "9999999", 218645.4465, 188465.45, 1, 218645.4465);
    }

    private InvestmentAccount getInvestmentAccount(
            Collection<InvestmentAccount> accounts, String accountNumber) {
        return accounts.stream()
                .filter(account -> account.getAccountNumber().equals(accountNumber))
                .findFirst()
                .orElseThrow(IllegalStateException::new);
    }

    private void assertPortfolio(
            Portfolio portfolio,
            String id,
            double totalMarketValue,
            double totalProfit,
            int instrumentSize,
            double cashValue) {
        assertThat(portfolio.getUniqueIdentifier()).isEqualTo(id);
        assertThat(portfolio.getTotalValue()).isEqualTo(totalMarketValue);
        assertThat(portfolio.getTotalProfit()).isEqualTo(totalProfit);
        assertThat(portfolio.getInstruments()).hasSize(instrumentSize);
        assertThat(portfolio.getCashValue()).isEqualTo(cashValue);
    }

    private void assertInstrument(
            Instrument instrument,
            String id,
            String isin,
            String name,
            double marketValue,
            double price,
            double quantity,
            Double profit) {
        assertThat(instrument.getUniqueIdentifier()).isEqualTo(id);
        assertThat(instrument.getIsin()).isEqualTo(isin);
        assertThat(instrument.getName()).isEqualTo(name);
        assertThat(instrument.getMarketValue()).isEqualTo(marketValue);
        assertThat(instrument.getPrice()).isEqualTo(price);
        assertThat(instrument.getQuantity()).isEqualTo(quantity);
        assertThat(instrument.getProfit()).isEqualTo(profit);
    }

    private TinkHttpClient mockHttpClient(RequestBuilder requestBuilder, HttpResponse response) {
        TinkHttpClient tinkHttpClient = mock(TinkHttpClient.class);
        when(tinkHttpClient.request(any(URL.class))).thenReturn(requestBuilder);
        when(requestBuilder.accept(any(MediaType.class))).thenReturn(requestBuilder);
        when(requestBuilder.type(any(String.class))).thenReturn(requestBuilder);
        when(requestBuilder.type(any(MediaType.class))).thenReturn(requestBuilder);
        when(requestBuilder.header(any(), any())).thenReturn(requestBuilder);
        when(requestBuilder.body(any(Object.class))).thenReturn(requestBuilder);
        when(requestBuilder.body(any(Object.class), anyString())).thenReturn(requestBuilder);
        when(requestBuilder.addBearerToken(any(OAuth2Token.class))).thenReturn(requestBuilder);
        when(requestBuilder.queryParam(anyString(), anyString())).thenReturn(requestBuilder);
        when(requestBuilder.get(HttpResponse.class)).thenReturn(response);
        return tinkHttpClient;
    }
}
