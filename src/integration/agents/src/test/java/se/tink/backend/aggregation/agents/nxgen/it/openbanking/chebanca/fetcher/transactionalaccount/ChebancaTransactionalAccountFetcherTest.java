package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount;

import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.exception.UnsuccessfulApiCallException;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.data.TransactionalAccountTestData;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc.ConsentAuthorizationResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc.CustomerIdResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc.GetBalancesResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class ChebancaTransactionalAccountFetcherTest {

    private static final int ERROR_RESPONSE_CODE = 500;
    private static final int SUCCESSFUL_RESPONSE_CODE = 200;

    private ChebancaApiClient apiClient;
    private ChebancaConsentManualApproveController consentController =
            mock(ChebancaConsentManualApproveController.class);
    private Credentials credentials = new Credentials();
    private HttpResponse erroneousResponse = getErroneousResponse();

    @Test
    public void shouldReturnProperNumberOfAccounts() {
        // given
        ChebancaTransactionalAccountFetcher fetcher =
                new ChebancaTransactionalAccountFetcher(apiClient, consentController);
        // when
        Collection<TransactionalAccount> accounts = fetcher.fetchAccounts();

        // then
        assertEquals(2, accounts.size());
    }

    @Test
    public void shouldThrowIfCreateConsentFailed() {
        // given
        when(apiClient.createConsent(any())).thenReturn(erroneousResponse);
        ChebancaTransactionalAccountFetcher fetcher =
                new ChebancaTransactionalAccountFetcher(apiClient, consentController);
        // when
        Throwable thrown = catchThrowable(fetcher::fetchAccounts);

        // then
        Assertions.assertThat(thrown)
                .isInstanceOf(UnsuccessfulApiCallException.class)
                .hasMessage(
                        "Could not create consent. Error response code: " + ERROR_RESPONSE_CODE);
    }

    @Test
    public void shouldThrowIfAuthorizeConsentFailed() {
        // given
        when(apiClient.authorizeConsent(any())).thenReturn(erroneousResponse);
        ChebancaTransactionalAccountFetcher fetcher =
                new ChebancaTransactionalAccountFetcher(apiClient, consentController);
        // when
        Throwable thrown = catchThrowable(fetcher::fetchAccounts);

        // then
        Assertions.assertThat(thrown)
                .isInstanceOf(UnsuccessfulApiCallException.class)
                .hasMessage(
                        "Could not authorize consent. Error response code: " + ERROR_RESPONSE_CODE);
    }

    @Test
    public void shouldThrowIfConfirmConsentFailed() {
        // given
        when(apiClient.confirmConsent(any())).thenReturn(erroneousResponse);
        ChebancaTransactionalAccountFetcher fetcher =
                new ChebancaTransactionalAccountFetcher(apiClient, consentController);
        // when
        Throwable thrown = catchThrowable(fetcher::fetchAccounts);

        // then
        Assertions.assertThat(thrown)
                .isInstanceOf(UnsuccessfulApiCallException.class)
                .hasMessage(
                        "Could not confirm consent. Error response code: " + ERROR_RESPONSE_CODE);
    }

    @Test
    public void shouldThrowIfGetCustomerIdFailed() {
        // given
        when(apiClient.getCustomerId()).thenReturn(erroneousResponse);
        ChebancaTransactionalAccountFetcher fetcher =
                new ChebancaTransactionalAccountFetcher(apiClient, consentController);
        // when
        Throwable thrown = catchThrowable(fetcher::fetchAccounts);

        // then
        Assertions.assertThat(thrown)
                .isInstanceOf(UnsuccessfulApiCallException.class)
                .hasMessage(
                        "Could not get customer id. Error response code: " + ERROR_RESPONSE_CODE);
    }

    @Test
    public void shouldThrowIfGetBalancesFailed() {
        // given
        when(apiClient.getBalances(any())).thenReturn(erroneousResponse);
        ChebancaTransactionalAccountFetcher fetcher =
                new ChebancaTransactionalAccountFetcher(apiClient, consentController);
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
                new ChebancaTransactionalAccountFetcher(apiClient, consentController);
        // when
        Throwable thrown = catchThrowable(fetcher::fetchAccounts);

        // then
        Assertions.assertThat(thrown)
                .isInstanceOf(UnsuccessfulApiCallException.class)
                .hasMessage(
                        "Could not fetch accounts. Error response code: " + ERROR_RESPONSE_CODE);
    }

    @Before
    public void initApiClientMockForPositiveScenario() {
        apiClient = mock(ChebancaApiClient.class);
        HttpResponse createConsentResponse = getMockedCreateConsentResponse();
        when(apiClient.createConsent(any())).thenReturn(createConsentResponse);
        HttpResponse authorizeConsentResponse = getMockedAuthorizeConsentResponse();
        when(apiClient.authorizeConsent(any())).thenReturn(authorizeConsentResponse);
        HttpResponse confirmConsentResponse = getMockedConfirmConsentResponse();
        when(apiClient.confirmConsent(any())).thenReturn(confirmConsentResponse);
        HttpResponse getAccountsResponse = getAccountsResponse();
        when(apiClient.getAccounts()).thenReturn(getAccountsResponse);
        HttpResponse customerIdResponse = getMockedCustomerIdResponse();
        when(apiClient.getCustomerId()).thenReturn(customerIdResponse);
        HttpResponse balanceResponse = getBalanceResponse();
        when(apiClient.getBalances(any())).thenReturn(balanceResponse);
    }

    private HttpResponse getErroneousResponse() {
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatus()).thenReturn(ERROR_RESPONSE_CODE);
        return response;
    }

    private HttpResponse getMockedCreateConsentResponse() {
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatus()).thenReturn(SUCCESSFUL_RESPONSE_CODE);
        when(response.getBody(ConsentResponse.class))
                .thenReturn(TransactionalAccountTestData.getCreateConsentResponse());
        return response;
    }

    private HttpResponse getMockedAuthorizeConsentResponse() {
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatus()).thenReturn(SUCCESSFUL_RESPONSE_CODE);
        when(response.getBody(ConsentAuthorizationResponse.class))
                .thenReturn(TransactionalAccountTestData.getAuthorizeConsentResponse());
        return response;
    }

    private HttpResponse getMockedConfirmConsentResponse() {
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatus()).thenReturn(SUCCESSFUL_RESPONSE_CODE);
        return response;
    }

    private HttpResponse getMockedCustomerIdResponse() {
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatus()).thenReturn(SUCCESSFUL_RESPONSE_CODE);
        when(response.getBody(CustomerIdResponse.class))
                .thenReturn(TransactionalAccountTestData.getCustomerId());
        return response;
    }

    private HttpResponse getBalanceResponse() {
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatus()).thenReturn(SUCCESSFUL_RESPONSE_CODE);
        when(response.getBody(GetBalancesResponse.class))
                .thenReturn(TransactionalAccountTestData.getBalances());
        return response;
    }

    private HttpResponse getAccountsResponse() {
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatus()).thenReturn(SUCCESSFUL_RESPONSE_CODE);
        when(response.getBody(GetAccountsResponse.class))
                .thenReturn(TransactionalAccountTestData.getAccountsResponse());
        return response;
    }
}
