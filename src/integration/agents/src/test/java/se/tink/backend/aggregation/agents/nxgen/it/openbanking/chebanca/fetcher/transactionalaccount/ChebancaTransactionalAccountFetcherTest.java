package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount;

import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import java.util.Collection;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.exception.UnsuccessfulApiCallException;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc.GetBalancesResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class ChebancaTransactionalAccountFetcherTest {

    private static final int ERROR_RESPONSE_CODE = 500;
    private static final int SUCCESSFUL_RESPONSE_CODE = 200;
    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/it/openbanking/chebanca/resources";

    private ChebancaApiClient apiClient;
    private HttpResponse erroneousResponse = getErroneousResponse();

    @Before
    public void initApiClientMockForPositiveScenario() {
        apiClient = mock(ChebancaApiClient.class);
        HttpResponse getAccountsResponse = getAccountsResponse();
        when(apiClient.getAccounts()).thenReturn(getAccountsResponse);
        HttpResponse balanceResponse = getBalanceResponse();
        when(apiClient.getBalances(any())).thenReturn(balanceResponse);
    }

    @Test
    public void shouldReturnProperNumberOfAccounts() {
        // given
        ChebancaTransactionalAccountFetcher fetcher =
                new ChebancaTransactionalAccountFetcher(apiClient);
        // when
        Collection<TransactionalAccount> accounts = fetcher.fetchAccounts();

        // then
        assertEquals(2, accounts.size());
    }

    @Test
    public void shouldThrowIfGetBalancesFailed() {
        // given
        when(apiClient.getBalances(any())).thenReturn(erroneousResponse);
        ChebancaTransactionalAccountFetcher fetcher =
                new ChebancaTransactionalAccountFetcher(apiClient);
        // when
        Throwable thrown = catchThrowable(fetcher::fetchAccounts);

        // then
        Assertions.assertThat(thrown)
                .isInstanceOf(UnsuccessfulApiCallException.class)
                .hasMessage(
                        "Could not fetch balances. Error response code: " + ERROR_RESPONSE_CODE);
    }

    @Test
    public void shouldThrowIfGetAccountsFailed() {
        // given
        when(apiClient.getAccounts()).thenReturn(erroneousResponse);
        ChebancaTransactionalAccountFetcher fetcher =
                new ChebancaTransactionalAccountFetcher(apiClient);
        // when
        Throwable thrown = catchThrowable(fetcher::fetchAccounts);

        // then
        Assertions.assertThat(thrown)
                .isInstanceOf(UnsuccessfulApiCallException.class)
                .hasMessage(
                        "Could not fetch accounts. Error response code: " + ERROR_RESPONSE_CODE);
    }

    private HttpResponse getErroneousResponse() {
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatus()).thenReturn(ERROR_RESPONSE_CODE);
        return response;
    }

    private HttpResponse getBalanceResponse() {
        GetBalancesResponse balancesResponse =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "balances_response.json").toFile(),
                        GetBalancesResponse.class);
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatus()).thenReturn(SUCCESSFUL_RESPONSE_CODE);
        when(response.getBody(GetBalancesResponse.class)).thenReturn(balancesResponse);
        return response;
    }

    private HttpResponse getAccountsResponse() {
        GetAccountsResponse accountsResponse =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "accounts_response.json").toFile(),
                        GetAccountsResponse.class);

        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatus()).thenReturn(SUCCESSFUL_RESPONSE_CODE);
        when(response.getBody(GetAccountsResponse.class)).thenReturn(accountsResponse);
        return response;
    }
}
