package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.component.transactional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.component.transactional.data.TransactionalAccountTestData;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.ChebancaTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc.ConsentAuthorizationResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc.CustomerIdResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc.GetBalancesResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;

public class ChebancaTransactionalAccountFetcherTest {

    private ChebancaApiClient apiClient;
    private ThirdPartyAppAuthenticationController authenticationController =
            mock(ThirdPartyAppAuthenticationController.class);
    private Credentials credentials = new Credentials();
    private HttpResponse erroneousResponse = getErroneousResponse();

    @Test
    public void shouldReturnProperNumberOfAccounts() {
        // given
        ChebancaTransactionalAccountFetcher fetcher =
                new ChebancaTransactionalAccountFetcher(
                        apiClient, authenticationController, credentials);
        // when
        Collection<TransactionalAccount> accounts = fetcher.fetchAccounts();

        // then
        assertEquals(2, accounts.size());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIfCreateConsentFailed() {
        // given
        when(apiClient.createConsent(any())).thenReturn(erroneousResponse);
        ChebancaTransactionalAccountFetcher fetcher =
                new ChebancaTransactionalAccountFetcher(
                        apiClient, authenticationController, credentials);
        // when
        fetcher.fetchAccounts();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIfAuthorizeConsentFailed() {
        // given
        when(apiClient.authorizeConsent(any())).thenReturn(erroneousResponse);
        ChebancaTransactionalAccountFetcher fetcher =
                new ChebancaTransactionalAccountFetcher(
                        apiClient, authenticationController, credentials);
        // when
        fetcher.fetchAccounts();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIfConfirmConsentFailed() {
        // given
        when(apiClient.confirmConsent(any())).thenReturn(erroneousResponse);
        ChebancaTransactionalAccountFetcher fetcher =
                new ChebancaTransactionalAccountFetcher(
                        apiClient, authenticationController, credentials);
        // when
        fetcher.fetchAccounts();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIfGetCustomerIdFailed() {
        // given
        when(apiClient.getCustomerId()).thenReturn(erroneousResponse);
        ChebancaTransactionalAccountFetcher fetcher =
                new ChebancaTransactionalAccountFetcher(
                        apiClient, authenticationController, credentials);
        // when
        fetcher.fetchAccounts();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIfGetBalancesFailed() {
        // given
        when(apiClient.getBalances(any())).thenReturn(erroneousResponse);
        ChebancaTransactionalAccountFetcher fetcher =
                new ChebancaTransactionalAccountFetcher(
                        apiClient, authenticationController, credentials);
        // when
        fetcher.fetchAccounts();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIfGetAccountsFailed() {
        // given
        when(apiClient.getAccounts()).thenReturn(erroneousResponse);
        ChebancaTransactionalAccountFetcher fetcher =
                new ChebancaTransactionalAccountFetcher(
                        apiClient, authenticationController, credentials);
        // when
        fetcher.fetchAccounts();
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
        when(response.getStatus()).thenReturn(500);
        return response;
    }

    private HttpResponse getMockedCreateConsentResponse() {
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatus()).thenReturn(200);
        when(response.getBody(ConsentResponse.class))
                .thenReturn(TransactionalAccountTestData.getCreateConsentResponse());
        return response;
    }

    private HttpResponse getMockedAuthorizeConsentResponse() {
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatus()).thenReturn(200);
        when(response.getBody(ConsentAuthorizationResponse.class))
                .thenReturn(TransactionalAccountTestData.getAuthorizeConsentResponse());
        return response;
    }

    private HttpResponse getMockedConfirmConsentResponse() {
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatus()).thenReturn(200);
        return response;
    }

    private HttpResponse getMockedCustomerIdResponse() {
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatus()).thenReturn(200);
        when(response.getBody(CustomerIdResponse.class))
                .thenReturn(TransactionalAccountTestData.getCustomerId());
        return response;
    }

    private HttpResponse getBalanceResponse() {
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatus()).thenReturn(200);
        when(response.getBody(GetBalancesResponse.class))
                .thenReturn(TransactionalAccountTestData.getBalances());
        return response;
    }

    private HttpResponse getAccountsResponse() {
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatus()).thenReturn(200);
        when(response.getBody(GetAccountsResponse.class))
                .thenReturn(TransactionalAccountTestData.getAccountsResponse());
        return response;
    }
}
