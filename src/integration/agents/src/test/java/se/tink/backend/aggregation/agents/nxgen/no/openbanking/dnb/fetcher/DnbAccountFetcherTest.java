package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import org.junit.BeforeClass;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.AccountTestData;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.rpc.BalancesResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class DnbAccountFetcherTest {

    private static DnbApiClient apiClient;

    @Test
    public void shouldReturnProperNumberOfAccounts() {
        DnbAccountFetcher fetcher = new DnbAccountFetcher(apiClient);

        Collection<TransactionalAccount> accounts = fetcher.fetchAccounts();

        assertEquals(5, accounts.size());
    }

    @Test
    public void shouldReturnThreeSavingsAccounts() {
        DnbAccountFetcher fetcher = new DnbAccountFetcher(apiClient);

        Collection<TransactionalAccount> accounts = fetcher.fetchAccounts();
        long savingsAccountCount =
                accounts.stream()
                        .filter(
                                transactionalAccount ->
                                        transactionalAccount.getType().equals(AccountTypes.SAVINGS))
                        .count();

        assertEquals(4, savingsAccountCount);
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
                .thenReturn(AccountTestData.getAccountsResponse());
        return httpResponse;
    }

    private static HttpResponse getMockedBalancesResponse() {
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getBody(BalancesResponse.class))
                .thenReturn(AccountTestData.getBalancesResponse());
        return httpResponse;
    }
}
