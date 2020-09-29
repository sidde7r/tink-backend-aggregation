package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.BeforeClass;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.rpc.BalancesResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class DnbAccountFetcherTest {
    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/openbanking/dnb/resources";
    private static DnbApiClient apiClient;

    @Test
    public void shouldReturnProperNumberOfAccounts() {
        DnbAccountFetcher fetcher = new DnbAccountFetcher(apiClient);

        Collection<TransactionalAccount> accounts = fetcher.fetchAccounts();

        assertThat(accounts).hasSize(5);
    }

    @Test
    public void shouldReturnProperNumberOfSavingsAccounts() {
        DnbAccountFetcher fetcher = new DnbAccountFetcher(apiClient);

        Collection<TransactionalAccount> accounts = fetcher.fetchAccounts();
        List<TransactionalAccount> savingAccounts =
                accounts.stream()
                        .filter(
                                transactionalAccount ->
                                        transactionalAccount.getType().equals(AccountTypes.SAVINGS))
                        .collect(Collectors.toList());

        assertThat(savingAccounts).hasSize(4);
    }

    @BeforeClass
    public static void initApiClient() {
        apiClient = mock(DnbApiClient.class);
        HttpResponse accountsResponse = getMockedAccountsResponse();
        when(apiClient.fetchAccounts()).thenReturn(accountsResponse);

        HttpResponse balancesResponse = getMockedBalancesResponse();
        when(apiClient.fetchBalances(anyString())).thenReturn(balancesResponse);
    }

    private static HttpResponse getMockedAccountsResponse() {
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getBody(AccountsResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "accounts.json").toFile(),
                                AccountsResponse.class));
        return httpResponse;
    }

    private static HttpResponse getMockedBalancesResponse() {
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getBody(BalancesResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "balances.json").toFile(),
                                BalancesResponse.class));
        return httpResponse;
    }
}
