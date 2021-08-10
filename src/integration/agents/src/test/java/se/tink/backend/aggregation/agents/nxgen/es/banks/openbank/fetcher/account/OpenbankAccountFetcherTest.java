package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.account;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.collection.List;
import io.vavr.jackson.datatype.VavrModule;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.OpenbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.rpc.UserDataResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.transactionalaccount.OpenbankTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.transactionalaccount.rpc.AccountHolderResponse;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party.Role;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class OpenbankAccountFetcherTest {

    static final String DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/es/banks/openbank/resources";
    private OpenbankApiClient apiClient;
    private OpenbankTransactionalAccountFetcher accountFetcher;

    @Before
    public void setup() {
        apiClient = mock(OpenbankApiClient.class);
        accountFetcher = new OpenbankTransactionalAccountFetcher(apiClient);
    }

    @Test
    public void shouldFetchTransactionalAccount() throws IOException {
        // given
        final Party holder = new Party("Alejandro Roberto", Role.HOLDER);
        final Party authUser = new Party("Genowefa Pedro", Role.AUTHORIZED_USER);
        List<Party> expectedParties = List.of(holder, authUser);
        final UserDataResponse userData = loadSampleData("accounts.json", UserDataResponse.class);
        final AccountHolderResponse[] accountHolder =
                loadSampleData("holders.json", AccountHolderResponse[].class);
        when(apiClient.fetchAccounts()).thenReturn(userData);
        when(apiClient.fetchAccountHolders(any())).thenReturn(List.of(accountHolder));

        // when
        Collection<TransactionalAccount> accounts = accountFetcher.fetchAccounts();
        java.util.List<Party> parties = accounts.stream().findFirst().get().getParties();

        // then
        Assert.assertEquals(1, accounts.size());
        Assert.assertEquals(parties.size(), expectedParties.size());
        Assert.assertEquals(parties.get(0).getName(), holder.getName());
        Assert.assertEquals(parties.get(1).getName(), authUser.getName());
    }

    @Test
    public void shouldFetchAccountsWithoutHolders() throws IOException {
        // given
        final Party party = new Party("Alejandro Roberto", Role.HOLDER);
        final UserDataResponse userData = loadSampleData("accounts.json", UserDataResponse.class);
        when(apiClient.fetchAccounts()).thenReturn(userData);
        when(apiClient.fetchAccountHolders(any())).thenReturn(List.empty());

        // when
        Collection<TransactionalAccount> accounts = accountFetcher.fetchAccounts();
        java.util.List<Party> parties = accounts.stream().findFirst().get().getParties();

        // then
        Assert.assertEquals(1, accounts.size());
        Assert.assertEquals(parties.get(0).getName(), party.getName());
    }

    private <T> T loadSampleData(String path, Class<T> cls) throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new VavrModule());
        return objectMapper.readValue(Paths.get(DATA_PATH, path).toFile(), cls);
    }
}
