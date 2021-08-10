package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.jackson.datatype.VavrModule;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.SabadellApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.SabadellInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.rpc.MarketsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.rpc.ProductsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.rpc.StocksResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SabadellInvestmentFetcherTest {

    static final String DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/es/banks/sabadell/resources/";
    private SabadellApiClient apiClient;
    private SabadellInvestmentFetcher investmentFetcher;

    @Before
    public void setup() {
        apiClient = mock(SabadellApiClient.class);
        investmentFetcher = new SabadellInvestmentFetcher(apiClient);
    }

    @Test
    public void shouldFetchInvestmentAccounts() throws IOException {

        final MarketsResponse marketsResponse =
                loadSampleData("markets.json", MarketsResponse.class);
        final ProductsResponse productsResponse =
                loadSampleData("investment.json", ProductsResponse.class);
        final StocksResponse stocksResponse = loadSampleData("stocks.json", StocksResponse.class);

        when(apiClient.fetchMarkets(any())).thenReturn(marketsResponse);
        when(apiClient.fetchProducts()).thenReturn(productsResponse);
        when(apiClient.fetchStocks(any(), any())).thenReturn(stocksResponse);

        final Collection<InvestmentAccount> investmentAccounts = investmentFetcher.fetchAccounts();

        // then
        Assert.assertEquals(1, investmentAccounts.size());
    }

    @Test
    public void shouldFetchEmptyInvestmentAccounts() {
        // given
        HttpResponseException exception = mockResponse(500);
        when(apiClient.fetchProducts()).thenThrow(exception);

        // when
        final Collection<InvestmentAccount> investmentAccountCollection =
                investmentFetcher.fetchAccounts();

        // then
        Assert.assertEquals(0, investmentAccountCollection.size());
    }

    @Test
    public void shouldFetchErrorInvestmentAccounts() {
        // given
        HttpResponseException exception = mockResponse(403);
        when(apiClient.fetchProducts()).thenThrow(exception);

        // when
        Throwable thrown = catchThrowable(() -> investmentFetcher.fetchAccounts());

        // then
        assertThat(thrown).isExactlyInstanceOf(HttpResponseException.class);
    }

    private HttpResponseException mockResponse(int status) {
        HttpResponse mocked = mock(HttpResponse.class);
        when(mocked.getStatus()).thenReturn(status);
        HttpResponseException exception = new HttpResponseException(null, mocked);

        when(exception.getResponse().getBody(ErrorResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                "{\"errorMessage\":\"Se ha producido un error inesperado. Por favor, inténtelo de nuevo.\",\"code\":\"\",\"errorMessageTitle\":\"¡Vaya! Algo no ha ido bien...\",\"errorMessageDetail\":\"\",\"errorCode\":\"Se ha producido un error inesperado. Por favor, inténtelo de nuevo.\",\"severity\":\"FATAL\",\"labelCta\":\"\",\"operationCta\":\"\",\"clickToCall\":\"\",\"evento\":\"\",\"nombreOperativa\":\"\",\"idCanal\":\"\"}\n",
                                ErrorResponse.class));

        return exception;
    }

    private <T> T loadSampleData(String path, Class<T> cls) throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new VavrModule());
        return objectMapper.readValue(Paths.get(DATA_PATH, path).toFile(), cls);
    }
}
