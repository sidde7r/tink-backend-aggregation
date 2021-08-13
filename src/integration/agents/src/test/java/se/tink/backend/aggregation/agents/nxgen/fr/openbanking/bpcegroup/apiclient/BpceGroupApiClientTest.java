package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.apiclient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.BpceTestFixtures.CLIENT_ID;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.BpceTestFixtures.EXCHANGE_CODE;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.BpceTestFixtures.REDIRECT_URL;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.BpceTestFixtures.RESOURCE_ID;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.BpceTestFixtures.SERVER_URL;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.BpceTestFixtures.SIGNATURE;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.BpceTestFixtures.getAccountsResponse;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.BpceTestFixtures.getBalancesResponse;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.BpceTestFixtures.getTokenResponse;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.BpceTestFixtures.getTransactionsResponse;

import com.google.common.collect.ImmutableList;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.authenticator.rpc.RefreshRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.configuration.BpceGroupConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.signature.BpceGroupSignatureHeaderGenerator;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.storage.BpceOAuth2TokenStorage;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.entity.consent.ConsentDataEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.entity.consent.CustomerConsent;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.rpc.BalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class BpceGroupApiClientTest {

    private static final String TOKEN_URL = SERVER_URL + "/stet/psd2/oauth/token";
    private static final String USER_IP_ADDRESS = "127.0.0.1";

    private BpceGroupApiClient bpceGroupApiClient;

    private TinkHttpClient httpClientMock;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        final BpceGroupConfiguration configurationMock = mock(BpceGroupConfiguration.class);
        final AgentConfiguration<BpceGroupConfiguration> agentConfigurationMock =
                mock(AgentConfiguration.class);
        when(configurationMock.getClientId()).thenReturn(CLIENT_ID);
        when(configurationMock.getServerUrl()).thenReturn(SERVER_URL);
        when(agentConfigurationMock.getProviderSpecificConfiguration())
                .thenReturn(configurationMock);
        when(agentConfigurationMock.getRedirectUrl()).thenReturn(REDIRECT_URL);

        httpClientMock = mock(TinkHttpClient.class);

        final BpceOAuth2TokenStorage bpceOAuth2TokenStorageMock =
                mock(BpceOAuth2TokenStorage.class);
        final OAuth2Token oAuth2TokenMock = mock(OAuth2Token.class);
        when(bpceOAuth2TokenStorageMock.getToken()).thenReturn(oAuth2TokenMock);

        final BpceGroupSignatureHeaderGenerator bpceGroupSignatureHeaderGeneratorMock =
                mock(BpceGroupSignatureHeaderGenerator.class);
        when(bpceGroupSignatureHeaderGeneratorMock.buildSignatureHeader(any(), any(), anyString()))
                .thenReturn(SIGNATURE);

        bpceGroupApiClient =
                new BpceGroupApiClient(
                        httpClientMock,
                        bpceOAuth2TokenStorageMock,
                        configurationMock,
                        REDIRECT_URL,
                        bpceGroupSignatureHeaderGeneratorMock,
                        USER_IP_ADDRESS);
    }

    @Test
    public void shouldExchangeAuthorizationToken() {
        // given
        final TokenResponse expectedTokenResponse = getTokenResponse();
        final RequestBuilder requestBuilderMock = setUpHttpClientMockForAuth(expectedTokenResponse);
        final ArgumentCaptor<TokenRequest> tokenRequestCaptor =
                ArgumentCaptor.forClass(TokenRequest.class);
        when(requestBuilderMock.body(tokenRequestCaptor.capture(), anyString()))
                .thenReturn(requestBuilderMock);

        // when
        final TokenResponse returnedResponse =
                bpceGroupApiClient.exchangeAuthorizationToken(EXCHANGE_CODE);

        // then
        assertThat(returnedResponse).isEqualTo(expectedTokenResponse);

        final String expectedTokenRequest = getTokenRequest();
        final String actualTokenRequest = tokenRequestCaptor.getValue().getBodyValue();
        assertThat(actualTokenRequest).isEqualTo(expectedTokenRequest);
    }

    @Test
    public void shouldExchangeRefreshToken() {
        // given
        final TokenResponse expectedTokenResponse = getTokenResponse();
        final RequestBuilder requestBuilderMock = setUpHttpClientMockForAuth(expectedTokenResponse);
        final ArgumentCaptor<RefreshRequest> refreshRequestCaptor =
                ArgumentCaptor.forClass(RefreshRequest.class);
        when(requestBuilderMock.body(refreshRequestCaptor.capture(), anyString()))
                .thenReturn(requestBuilderMock);

        // when
        final TokenResponse returnedResponse =
                bpceGroupApiClient.exchangeRefreshToken(EXCHANGE_CODE);

        // then
        assertThat(returnedResponse).isEqualTo(expectedTokenResponse);

        final String expectedRefreshRequest = getRefreshRequest();
        final String actualRefreshRequest = refreshRequestCaptor.getValue().getBodyValue();
        assertThat(actualRefreshRequest).isEqualTo(expectedRefreshRequest);
    }

    @Test
    public void shouldRecordCustomerConsent() {
        // given
        final ArgumentCaptor<CustomerConsent> customerConsentCaptor =
                setUpHttpClientMockForConsentRequest();
        final String accountId1 = "123";
        final String accountId2 = "456";
        final List<String> accountIds = ImmutableList.of(accountId1, accountId2);

        // when
        bpceGroupApiClient.recordCustomerConsent(accountIds);

        // then
        final List<ConsentDataEntity> consentDataEntityList =
                ImmutableList.of(
                        new ConsentDataEntity(accountId1), new ConsentDataEntity(accountId2));
        final CustomerConsent expectedCustomerConsent =
                CustomerConsent.builder()
                        .balances(consentDataEntityList)
                        .transactions(consentDataEntityList)
                        .psuIdentity(true)
                        .trustedBeneficiaries(true)
                        .build();
        final CustomerConsent capturedCustomerConsent = customerConsentCaptor.getValue();

        assertThat(capturedCustomerConsent).isEqualTo(expectedCustomerConsent);
    }

    @Test
    public void shouldFetchAccounts() {
        // given
        final String url = SERVER_URL + "/stet/psd2/v1/accounts";
        final AccountsResponse expectedResponse = getAccountsResponse();
        setUpHttpClientMockForApi(url, expectedResponse);

        // when
        final AccountsResponse actualResponse = bpceGroupApiClient.fetchAccounts();

        // then
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    public void shouldFetchBalances() {
        // given
        final String url = SERVER_URL + "/stet/psd2/v1/accounts/" + RESOURCE_ID + "/balances";
        final BalancesResponse expectedResponse = getBalancesResponse();
        setUpHttpClientMockForApi(url, expectedResponse);

        // when
        final BalancesResponse actualResponse = bpceGroupApiClient.fetchBalances(RESOURCE_ID);

        // then
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    public void shouldGetTransactions() {
        // given
        final String url = SERVER_URL + "/stet/psd2/v1/accounts/" + RESOURCE_ID + "/transactions";
        final TransactionsResponse expectedResponse = getTransactionsResponse();
        setUpHttpClientMockForApi(url, expectedResponse);

        // when
        final TransactionsResponse actualResponse = bpceGroupApiClient.getTransactions(RESOURCE_ID);

        // then
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    public void shouldGetTransactionsForPage2() {
        // given
        final String url =
                SERVER_URL + "/stet/psd2/v1/accounts/" + RESOURCE_ID + "/transactions?page=2";
        final TransactionsResponse expectedResponse = getTransactionsResponse();
        setUpHttpClientMockForApi(url, expectedResponse);

        // when
        final TransactionsResponse actualResponse =
                bpceGroupApiClient.getTransactions(RESOURCE_ID, 2);

        // then
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    private RequestBuilder setUpHttpClientMockForAuth(Object response) {
        final RequestBuilder requestBuilderMock = mock(RequestBuilder.class);
        when(requestBuilderMock.accept(anyString())).thenReturn(requestBuilderMock);

        final HttpResponse httpResponseMock = mock(HttpResponse.class);
        when(httpResponseMock.getBody(any())).thenReturn(response);

        when(requestBuilderMock.post(any())).thenReturn(httpResponseMock);
        when(requestBuilderMock.post(any(), anyString())).thenReturn(httpResponseMock);

        when(httpClientMock.request(new URL(TOKEN_URL))).thenReturn(requestBuilderMock);

        return requestBuilderMock;
    }

    private void setUpHttpClientMockForApi(String urlString, Object response) {
        final RequestBuilder requestBuilderMock = mock(RequestBuilder.class);
        when(requestBuilderMock.body(any(), anyString())).thenReturn(requestBuilderMock);
        when(requestBuilderMock.addBearerToken(any())).thenReturn(requestBuilderMock);
        when(requestBuilderMock.header(Psd2Headers.Keys.PSU_IP_ADDRESS, USER_IP_ADDRESS))
                .thenReturn(requestBuilderMock);
        when(requestBuilderMock.header(eq(Psd2Headers.Keys.SIGNATURE), any(String.class)))
                .thenReturn(requestBuilderMock);
        when(requestBuilderMock.header(eq(Psd2Headers.Keys.X_REQUEST_ID), any(String.class)))
                .thenReturn(requestBuilderMock);

        when(requestBuilderMock.method(any(), any())).thenReturn(response);

        final URL url = new URL(urlString);
        when(requestBuilderMock.getUrl()).thenReturn(url);

        when(httpClientMock.request(url)).thenReturn(requestBuilderMock);
    }

    private ArgumentCaptor<CustomerConsent> setUpHttpClientMockForConsentRequest() {
        final ArgumentCaptor<CustomerConsent> customerConsentCaptor =
                ArgumentCaptor.forClass(CustomerConsent.class);
        final RequestBuilder requestBuilderMock = mock(RequestBuilder.class);
        when(requestBuilderMock.body(customerConsentCaptor.capture(), anyString()))
                .thenReturn(requestBuilderMock);
        when(requestBuilderMock.addBearerToken(any())).thenReturn(requestBuilderMock);
        when(requestBuilderMock.header(Psd2Headers.Keys.PSU_IP_ADDRESS, USER_IP_ADDRESS))
                .thenReturn(requestBuilderMock);
        when(requestBuilderMock.header(eq(Psd2Headers.Keys.SIGNATURE), any(String.class)))
                .thenReturn(requestBuilderMock);
        when(requestBuilderMock.header(eq(Psd2Headers.Keys.X_REQUEST_ID), any(String.class)))
                .thenReturn(requestBuilderMock);

        final URL url = new URL(SERVER_URL + "/stet/psd2/v1/consents");
        when(requestBuilderMock.getUrl()).thenReturn(url);

        when(httpClientMock.request(url)).thenReturn(requestBuilderMock);

        return customerConsentCaptor;
    }

    private static String getTokenRequest() {
        try {
            return String.format(
                    "client_id=%s&redirect_uri=%s&grant_type=authorization_code&code=%s",
                    CLIENT_ID,
                    URLEncoder.encode(REDIRECT_URL, StandardCharsets.UTF_8.toString()),
                    EXCHANGE_CODE);
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static String getRefreshRequest() {
        return String.format(
                "client_id=%s&grant_type=refresh_token&refresh_token=%s", CLIENT_ID, EXCHANGE_CODE);
    }
}
