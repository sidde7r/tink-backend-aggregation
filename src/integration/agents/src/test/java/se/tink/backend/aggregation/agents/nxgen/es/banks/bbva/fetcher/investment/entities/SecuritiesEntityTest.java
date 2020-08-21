package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.entities;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.jackson.datatype.VavrModule;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.BbvaInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.rpc.FinancialInvestmentResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.rpc.HistoricalDateResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.rpc.FinancialDashboardResponse;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;

public class SecuritiesEntityTest {
    static final String DATA_PATH = "data/test/agents/es/bbva/";

    private <T> T loadSampleData(String path, Class<T> cls) throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new VavrModule());
        return objectMapper.readValue(Paths.get(DATA_PATH, path).toFile(), cls);
    }

    @Test
    public void testSecuritiesFetcher() throws IOException {

        final BbvaApiClient apiClient = mock(BbvaApiClient.class);

        final HistoricalDateResponse historicalDateResponse =
                loadSampleData("investments.json", HistoricalDateResponse.class);
        final FinancialDashboardResponse financialDashboardResponse =
                loadSampleData("securities.json", FinancialDashboardResponse.class);
        final FinancialInvestmentResponse financialInvestmentResponse =
                loadSampleData("financial.json", FinancialInvestmentResponse.class);

        when(apiClient.fetchInvestmentHistoricalDate(any())).thenReturn(historicalDateResponse);
        when(apiClient.fetchFinancialDashboard()).thenReturn(financialDashboardResponse);
        when(apiClient.fetchFinancialInvestment(any())).thenReturn(financialInvestmentResponse);

        final BbvaInvestmentFetcher fetcher = new BbvaInvestmentFetcher(apiClient);
        final Collection<InvestmentAccount> accounts = fetcher.fetchAccounts();

        Assert.assertEquals(2, accounts.size());
    }
}
