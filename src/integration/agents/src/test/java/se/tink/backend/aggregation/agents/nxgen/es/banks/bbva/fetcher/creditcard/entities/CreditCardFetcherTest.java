package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.creditcard.entities;

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
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.creditcard.BbvaCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.rpc.FinancialDashboardResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class CreditCardFetcherTest {

    static final String DATA_PATH = "data/test/agents/es/bbva/";

    private <T> T loadSampleData(String path, Class<T> cls) throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new VavrModule());
        return objectMapper.readValue(Paths.get(DATA_PATH, path).toFile(), cls);
    }

    @Test
    public void testCreditCardFetcher() throws IOException {
        final BbvaApiClient apiClient = mock(BbvaApiClient.class);

        final FinancialDashboardResponse financialDashboardResponse =
                loadSampleData("financial_dashboard.json", FinancialDashboardResponse.class);

        when(apiClient.fetchFinancialDashboard()).thenReturn(financialDashboardResponse);

        final BbvaCreditCardFetcher fetcher = new BbvaCreditCardFetcher(apiClient);
        final Collection<CreditCardAccount> creditCards = fetcher.fetchAccounts();

        Assert.assertEquals(1, creditCards.size());
    }
}
