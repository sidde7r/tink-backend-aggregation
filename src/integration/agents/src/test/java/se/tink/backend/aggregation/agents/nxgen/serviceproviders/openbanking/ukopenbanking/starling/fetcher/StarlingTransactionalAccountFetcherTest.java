package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.starling.fetcher;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.StarlingApiClient;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.StarlingTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.AccountBalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.AccountHolderNameResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.AccountIdentifiersResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc.StarlingAccountHolderType;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class StarlingTransactionalAccountFetcherTest {

    static final String DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/starling/resources/";
    private StarlingApiClient apiClient;
    private StarlingTransactionalAccountFetcher transactionalAccountFetcher;

    @Before
    public void setup() {
        apiClient = mock(StarlingApiClient.class);
        transactionalAccountFetcher = new StarlingTransactionalAccountFetcher(apiClient);
    }

    @Test
    public void shouldFetchAndMapAccounts() throws IOException {
        // given
        final AccountsResponse accountEntities =
                loadSampleData("account.json", AccountsResponse.class);
        final StarlingAccountHolderType starlingAccountHolderType =
                StarlingAccountHolderType.INDIVIDUAL;
        final AccountHolderNameResponse holderNameResponse =
                new AccountHolderNameResponse("Marcin");
        final AccountIdentifiersResponse accountIdentifiersResponse =
                loadSampleData("accountIdentifier.json", AccountIdentifiersResponse.class);
        final AccountBalanceResponse accountBalanceResponse =
                loadSampleData("accountBalance.json", AccountBalanceResponse.class);

        when(apiClient.fetchAccounts()).thenReturn(accountEntities.getAccounts());
        when(apiClient.fetchAccountHolderType()).thenReturn(starlingAccountHolderType);
        when(apiClient.fetchAccountIdentifiers(any())).thenReturn(accountIdentifiersResponse);
        when(apiClient.fetchAccountBalance(any())).thenReturn(accountBalanceResponse);
        when(apiClient.fetchAccountHolderName()).thenReturn(holderNameResponse);

        // when
        final Collection<TransactionalAccount> transactionalAccounts =
                transactionalAccountFetcher.fetchAccounts();

        // then
        Assert.assertEquals(1, transactionalAccounts.size());
    }

    @Test
    public void shouldHandleEmptyAccounts() throws IOException {
        // given
        final AccountsResponse accountEntities =
                loadSampleData("noAccount.json", AccountsResponse.class);

        when(apiClient.fetchAccounts()).thenReturn(accountEntities.getAccounts());

        // when
        final Collection<TransactionalAccount> transactionalAccounts =
                transactionalAccountFetcher.fetchAccounts();

        // then
        Assert.assertEquals(0, transactionalAccounts.size());
    }

    private <T> T loadSampleData(String path, Class<T> cls) throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new SimpleModule());
        return objectMapper.readValue(Paths.get(DATA_PATH, path).toFile(), cls);
    }
}
