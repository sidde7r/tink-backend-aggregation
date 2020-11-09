package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.ApiServices.CREATE_PAYMENT;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.ApiServices.GET_ACCOUNTS;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.ApiServices.POST_CONSENT;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.ApiServices.TOKEN;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.StorageKeys;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.StorageKeys.OAUTH_TOKEN;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.StorageKeys.PIS_TOKEN;
import static se.tink.libraries.serialization.utils.SerializationUtils.deserializeFromString;

import com.google.common.collect.Lists;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.rpc.GetTokenForm;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.rpc.GetTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.rpc.PostConsentBody;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.rpc.PostConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.configuration.Xs2aDevelopersProviderConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.executor.payment.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.entities.TransactionsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.entities.TransactionsLinksEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.rpc.GetBalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGeneratorImpl;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class Xs2aDevelopersApiClientTest {
    private static final String BASE_URL = "BASE_URL";
    private static final String JSON_MOCK = "{}";

    private Xs2aDevelopersApiClient apiClient;
    private TinkHttpClient tinkHttpClient;
    private RequestBuilder requestBuilder;
    private PersistentStorage storage;

    @Before
    public void init() {
        Xs2aDevelopersProviderConfiguration configuration =
                new Xs2aDevelopersProviderConfiguration("CLIENT_ID", BASE_URL, "REDIRECT_URL");
        OAuth2Token oauth2Token =
                OAuth2Token.create("TOKEN_TYPE", "ACCESS_TOKEN", "REFRESH_TOKEN", 1);
        tinkHttpClient = mock(TinkHttpClient.class);

        requestBuilder = mock(RequestBuilder.class);
        storage = mock(PersistentStorage.class);

        when(storage.get(StorageKeys.CONSENT_ID)).thenReturn("CONSENT_ID");
        when(storage.get(OAUTH_TOKEN, OAuth2Token.class)).thenReturn(Optional.of(oauth2Token));
        when(storage.get(PIS_TOKEN, OAuth2Token.class)).thenReturn(Optional.of(oauth2Token));

        when(requestBuilder.accept(MediaType.APPLICATION_JSON_TYPE)).thenReturn(requestBuilder);
        when(requestBuilder.type(MediaType.APPLICATION_JSON)).thenReturn(requestBuilder);
        when(requestBuilder.addBearerToken(oauth2Token)).thenReturn(requestBuilder);
        when(requestBuilder.header(anyString(), anyString())).thenReturn(requestBuilder);
        when(requestBuilder.header(anyString(), any(Object.class))).thenReturn(requestBuilder);
        when(requestBuilder.body(any(Object.class), anyString())).thenReturn(requestBuilder);
        when(requestBuilder.body(any(Object.class))).thenReturn(requestBuilder);
        when(requestBuilder.queryParam(anyString(), anyString())).thenReturn(requestBuilder);

        apiClient =
                new Xs2aDevelopersApiClient(
                        tinkHttpClient,
                        storage,
                        configuration,
                        true,
                        "127.0.0.1",
                        new RandomValueGeneratorImpl());
    }

    @Test(expected = SessionException.class)
    public void shouldThrowSessionExceptionWhenTokenDoesNotExistsInStorageDuringAis() {
        when(storage.get(OAUTH_TOKEN, OAuth2Token.class)).thenReturn(Optional.empty());
        apiClient.getAccounts();
    }

    @Test
    public void shouldBuildAuthorizeUrl() {
        URL result = apiClient.buildAuthorizeUrl("STATE", "SCOPE", "HREF");

        assertThat(result.toString())
                .contains(
                        "HREF?state=STATE&redirect_uri=REDIRECT_URL&client_id=CLIENT_ID&scope=SCOPE");
        assertThat(result.toString()).contains("response_type=code&code_challenge_method=S256");
    }

    @Test
    public void shouldBuildAndExecuteGetAccountsRequest() {
        // given
        URL url = new URL(BASE_URL + GET_ACCOUNTS);
        GetAccountsResponse getAccountsResponse =
                deserializeFromString(
                        "{\"accounts\" : [{\"iban\" : \"PL6\", \"resourceId\" : \"1\", \"name\" : \"NAME\", \"product\" : \"ACCOUNT_TYPE\"}]}",
                        GetAccountsResponse.class);
        when(tinkHttpClient.request(url)).thenReturn(requestBuilder);
        when(requestBuilder.get(GetAccountsResponse.class)).thenReturn(getAccountsResponse);

        // when
        GetAccountsResponse result = apiClient.getAccounts();

        // then
        assertThat(result).isEqualTo(getAccountsResponse);
        verify(tinkHttpClient).request(url);
        verifyNoMoreInteractions(tinkHttpClient);
    }

    @Test
    public void shouldBuildAndExecuteGetBalanceRequest() {
        // given
        URL url = new URL(BASE_URL + "/berlingroup/v1/accounts/1/balances");
        GetBalanceResponse getBalanceResponse =
                deserializeFromString(JSON_MOCK, GetBalanceResponse.class);
        when(tinkHttpClient.request(url)).thenReturn(requestBuilder);
        when(requestBuilder.get(GetBalanceResponse.class)).thenReturn(getBalanceResponse);
        AccountEntity accountEntity = mock(AccountEntity.class);
        when(accountEntity.getResourceId()).thenReturn("1");

        // when
        GetBalanceResponse result = apiClient.getBalance(accountEntity);

        // then
        assertThat(result).isEqualTo(getBalanceResponse);
        verify(tinkHttpClient).request(url);
        verifyNoMoreInteractions(tinkHttpClient);
    }

    @Test
    public void shouldBuildAndExecuteGetTransactionsRequest() {
        // given
        URL url = new URL(BASE_URL + "/berlingroup/v1/accounts/1/transactions");
        GetTransactionsResponse getTransactionsResponse =
                Mockito.mock(GetTransactionsResponse.class);
        List transactions = Lists.newArrayList(Mockito.mock(Transaction.class));
        Mockito.when(getTransactionsResponse.toTinkTransactions()).thenReturn(transactions);
        TransactionsLinksEntity linksEntity = Mockito.mock(TransactionsLinksEntity.class);
        Mockito.when(linksEntity.getNext()).thenReturn(Optional.empty());
        TransactionsEntity transactionsEntity = Mockito.mock(TransactionsEntity.class);
        Mockito.when(transactionsEntity.getLinks()).thenReturn(linksEntity);
        Mockito.when(getTransactionsResponse.getTransactions()).thenReturn(transactionsEntity);
        when(tinkHttpClient.request(url)).thenReturn(requestBuilder);
        when(requestBuilder.get(GetTransactionsResponse.class)).thenReturn(getTransactionsResponse);
        TransactionalAccount account = mock(TransactionalAccount.class);
        when(account.getApiIdentifier()).thenReturn("1");

        // when
        List<? extends Transaction> result =
                apiClient.getTransactions(account, LocalDate.now(), LocalDate.now());

        // then
        Assert.assertTrue(result.size() == 1);
        assertThat(result.get(0)).isEqualTo(getTransactionsResponse.toTinkTransactions().get(0));
        verify(tinkHttpClient).request(url);
        verifyNoMoreInteractions(tinkHttpClient);
    }

    @Test
    public void shouldBuildAndExecuteGetPaymentRequest() {
        // given
        URL url = new URL(BASE_URL + "/berlingroup/v1/payments/sepa-credit-transfers/1");
        GetPaymentResponse getPaymentResponse =
                deserializeFromString(JSON_MOCK, GetPaymentResponse.class);
        when(tinkHttpClient.request(url)).thenReturn(requestBuilder);
        when(requestBuilder.get(GetPaymentResponse.class)).thenReturn(getPaymentResponse);

        // when
        GetPaymentResponse result = apiClient.getPayment("1");

        // then
        assertThat(result).isEqualTo(getPaymentResponse);
        verify(tinkHttpClient).request(url);
        verifyNoMoreInteractions(tinkHttpClient);
    }

    @Test
    public void shouldBuildAndExecuteGetTokenRequest() {
        // given
        URL url = new URL(BASE_URL + TOKEN);
        GetTokenResponse getTokenResponse =
                deserializeFromString(JSON_MOCK, GetTokenResponse.class);
        when(tinkHttpClient.request(url)).thenReturn(requestBuilder);
        when(requestBuilder.post(GetTokenResponse.class)).thenReturn(getTokenResponse);

        // when
        GetTokenResponse result =
                apiClient.getToken(
                        GetTokenForm.builder()
                                .setClientId("clientId")
                                .setGrantType("grantType")
                                .build());

        // then
        assertThat(result).isEqualTo(getTokenResponse);
        verify(tinkHttpClient).request(url);
        verifyNoMoreInteractions(tinkHttpClient);
    }

    @Test
    public void shouldBuildAndExecuteCreateConsentRequest() {
        // given
        URL url = new URL(BASE_URL + POST_CONSENT);
        PostConsentResponse postConsentResponse =
                deserializeFromString(JSON_MOCK, PostConsentResponse.class);
        when(tinkHttpClient.request(url)).thenReturn(requestBuilder);
        when(requestBuilder.post(PostConsentResponse.class)).thenReturn(postConsentResponse);
        PostConsentBody postConsentBody = mock(PostConsentBody.class);

        // when
        PostConsentResponse result = apiClient.createConsent(postConsentBody);

        // then
        assertThat(result).isEqualTo(postConsentResponse);
        verify(tinkHttpClient).request(url);
        verifyNoMoreInteractions(tinkHttpClient);
    }

    @Test
    public void shouldBuildAndExecuteCreatePaymentRequest() {
        // given
        URL url = new URL(BASE_URL + CREATE_PAYMENT);
        CreatePaymentResponse createPaymentResponse =
                deserializeFromString(JSON_MOCK, CreatePaymentResponse.class);
        when(tinkHttpClient.request(url)).thenReturn(requestBuilder);
        when(requestBuilder.post(CreatePaymentResponse.class)).thenReturn(createPaymentResponse);
        CreatePaymentRequest createPaymentRequest = mock(CreatePaymentRequest.class);

        // when
        CreatePaymentResponse result = apiClient.createPayment(createPaymentRequest);

        // then
        assertThat(result).isEqualTo(createPaymentResponse);
        verify(tinkHttpClient).request(url);
        verifyNoMoreInteractions(tinkHttpClient);
    }

    @Test
    public void shouldThrowBankServiceExceptionWhenRefreshingTokenFailsWith500() {
        // given
        GetTokenForm getTokenForm =
                GetTokenForm.builder()
                        .setClientId("asdf")
                        .setCode("zxcv")
                        .setCodeVerifier("bnm")
                        .setGrantType("uiop")
                        .setRedirectUri("iopp")
                        .setValidRequest(true)
                        .build();

        HttpResponseException hre = mock(HttpResponseException.class);
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(hre.getResponse()).thenReturn(httpResponse);
        when(httpResponse.getStatus()).thenReturn(500);

        URL url = new URL(BASE_URL + TOKEN);
        when(tinkHttpClient.request(url)).thenReturn(requestBuilder);
        when(requestBuilder.post(GetTokenResponse.class)).thenThrow(hre);

        // when
        Throwable throwable = catchThrowable(() -> apiClient.getToken(getTokenForm));

        // then
        assertThat(throwable)
                .isInstanceOf(BankServiceException.class)
                .hasMessage("Cause: BankServiceError.BANK_SIDE_FAILURE");
    }
}
