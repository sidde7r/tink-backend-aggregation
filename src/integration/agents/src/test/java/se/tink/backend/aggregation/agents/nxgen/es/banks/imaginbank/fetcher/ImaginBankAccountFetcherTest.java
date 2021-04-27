package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher;

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
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.transactionalaccount.ImaginBankAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class ImaginBankAccountFetcherTest {
    static final String DATA_PATH = "data/test/agents/es/imaginbank/";
    private ImaginBankApiClient apiClient;

    @Before
    public void setup() {
        apiClient = mock(ImaginBankApiClient.class);
    }

    @Test
    public void shouldFetchInvestmentAccount() throws IOException {
        // given
        final AccountsResponse response =
                loadSampleData("checking_account.json", AccountsResponse.class);
        when(apiClient.fetchAccounts()).thenReturn(response);

        // when
        final ImaginBankAccountFetcher fetcher = new ImaginBankAccountFetcher(apiClient);
        Collection<TransactionalAccount> accounts = fetcher.fetchAccounts();

        // then
        Assert.assertEquals(1, accounts.size());
    }

    @Test
    public void shouldFetchNullOrEmptyInvestmentAccount() throws IOException {
        // given
        final AccountsResponse response =
                loadSampleData("unknown_account.json", AccountsResponse.class);
        when(apiClient.fetchAccounts()).thenReturn(response);

        // when
        final ImaginBankAccountFetcher fetcher = new ImaginBankAccountFetcher(apiClient);
        Collection<TransactionalAccount> accounts = fetcher.fetchAccounts();

        // then
        Assert.assertEquals(0, accounts.size());
    }

    private <T> T loadSampleData(String path, Class<T> cls) throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new VavrModule());
        return objectMapper.readValue(Paths.get(DATA_PATH, path).toFile(), cls);
    }
}
