package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import java.util.Collection;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.transactionalaccount.RevolutTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.rpc.BaseUserResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class RevolutTest {
    final String TEST_DATA_PATH = "data/test/agents/revolut";

    private <T> T loadTestResponse(String path, Class<T> cls) {
        return SerializationUtils.deserializeFromString(
                Paths.get(TEST_DATA_PATH, path).toFile(), cls);
    }

    @Test
    public void testAccountFetcher() {
        final RevolutApiClient apiClient = mock(RevolutApiClient.class);

        final BaseUserResponse userResponse = loadTestResponse("user.json", BaseUserResponse.class);
        final AccountsResponse accountsResponse =
                loadTestResponse("accounts.json", AccountsResponse.class);
        when(apiClient.fetchUser()).thenReturn(userResponse);
        when(apiClient.fetchAccounts()).thenReturn(accountsResponse);

        final RevolutTransactionalAccountFetcher fetcher =
                new RevolutTransactionalAccountFetcher(apiClient);
        final Collection<TransactionalAccount> accounts = fetcher.fetchAccounts();

        assertEquals(2, accounts.size());
    }
}
