package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount;

import static org.assertj.core.api.Java6Assertions.catchThrowable;
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
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.rpc.AccountTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.rpc.FinancialDashboardResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class AccountTransactionsFetcherTest {

    static final String DATA_PATH = "data/test/agents/es/bbva/";
    BbvaApiClient apiClient;

    @Before
    public void setup() {
        apiClient = mock(BbvaApiClient.class);
    }

    @Test
    public void testAccountFetcher() throws IOException {
        final FinancialDashboardResponse financialDashboardResponse =
                loadSampleData("financial_dashboard.json", FinancialDashboardResponse.class);

        when(apiClient.fetchFinancialDashboard()).thenReturn(financialDashboardResponse);

        final BbvaAccountFetcher fetcher = new BbvaAccountFetcher(apiClient);
        final Collection<TransactionalAccount> transactionalAccounts = fetcher.fetchAccounts();

        Assert.assertEquals(3, transactionalAccounts.size());
    }

    @Test
    public void shouldFetchAccountTransactions() throws IOException {
        final AccountTransactionsResponse transactions =
                loadSampleData("transactions.json", AccountTransactionsResponse.class);

        when(apiClient.fetchAccountTransactions(any(), any())).thenReturn(transactions);

        AccountTransactionsResponse response = apiClient.fetchAccountTransactions(any(), any());

        Assert.assertEquals(response.getAccountTransactions().size(), 1);
    }

    @Test
    public void shouldThrowHttpResponseException() {
        HttpResponse httpResponse = mockResponse(409);
        HttpResponseException httpResponseException =
                new HttpResponseException(any(), httpResponse);

        when(apiClient.fetchAccountTransactions(any(), null)).thenThrow(httpResponseException);

        Throwable throwable =
                catchThrowable(() -> apiClient.fetchAccountTransactions(any(), any()));

        Assert.assertEquals(throwable, httpResponseException);
    }

    private HttpResponse mockResponse(int status) {
        HttpResponse mocked = mock(HttpResponse.class);
        when(mocked.getStatus()).thenReturn(status);
        return mocked;
    }

    private <T> T loadSampleData(String path, Class<T> cls) throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new VavrModule());
        return objectMapper.readValue(Paths.get(DATA_PATH, path).toFile(), cls);
    }
}
