package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.apiclient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.BredBanquePopulaireTestFixtures.CLIENT_ID;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.BredBanquePopulaireTestFixtures.DIGEST;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.BredBanquePopulaireTestFixtures.EXCHANGE_CODE;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.BredBanquePopulaireTestFixtures.OCP_APIM_KEY;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.BredBanquePopulaireTestFixtures.REDIRECT_URL;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.BredBanquePopulaireTestFixtures.RESOURCE_ID;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.BredBanquePopulaireTestFixtures.SIGNATURE;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.BredBanquePopulaireTestFixtures.STATE;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.BredBanquePopulaireTestFixtures.getAccountResponseWithoutConsent;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.BredBanquePopulaireTestFixtures.getBalancesResponse;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.BredBanquePopulaireTestFixtures.getConsentDataEntities;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.BredBanquePopulaireTestFixtures.getEndUserIdentityResponse;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.BredBanquePopulaireTestFixtures.getTokenResponse;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.BredBanquePopulaireTestFixtures.getTransactionsResponse;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javax.ws.rs.core.MediaType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.BredBanquePopulaireConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.authenticator.rpc.RefreshRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.configuration.BredBanquePopulaireConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.identity.rpc.EndUserIdentityResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.transacitons.rpc.TransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.transactionalaccount.entity.CustomerConsent;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.transactionalaccount.rpc.BalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.signature.BredBanquePopulaireHeaderGenerator;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.MockRandomValueGenerator;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RunWith(MockitoJUnitRunner.class)
public class BredBanquePopulaireApiClientTest {

    @Mock private TinkHttpClient httpClient;
    @Mock private AgentConfiguration<BredBanquePopulaireConfiguration> agentConfiguration;
    @Mock private BredBanquePopulaireConfiguration configuration;
    @Mock private BredBanquePopulaireHeaderGenerator headerGenerator;

    private final PersistentStorage persistentStorage = new PersistentStorage();
    private final RandomValueGenerator randomValueGenerator = new MockRandomValueGenerator();
    private BredBanquePopulaireApiClient bredBanquePopulaireApiClient;

    @Before
    public void setUp() {
        final OAuth2Token oAuth2Token = mock(OAuth2Token.class);
        persistentStorage.put(BredBanquePopulaireConstants.StorageKeys.OAUTH_TOKEN, oAuth2Token);

        when(agentConfiguration.getProviderSpecificConfiguration()).thenReturn(configuration);
        when(agentConfiguration.getRedirectUrl()).thenReturn(REDIRECT_URL);
        when(configuration.getClientId()).thenReturn(CLIENT_ID);
        when(configuration.getOcpApimSubscriptionKey()).thenReturn(OCP_APIM_KEY);

        when(headerGenerator.buildSignatureHeader(any(), any(), anyString())).thenReturn(SIGNATURE);
        when(headerGenerator.getDigestHeaderValue(any())).thenReturn(DIGEST);

        bredBanquePopulaireApiClient =
                new BredBanquePopulaireApiClient(
                        httpClient,
                        persistentStorage,
                        agentConfiguration,
                        randomValueGenerator,
                        headerGenerator);
    }

    @Test
    public void shouldExchangeAuthorizationToken() {
        // given
        final TokenResponse expectedTokenResponse = getTokenResponse();
        final RequestBuilder requestBuilder =
                setUpHttpClientForAuthentication(expectedTokenResponse);

        final ArgumentCaptor<TokenRequest> tokenRequestArgumentCaptor =
                ArgumentCaptor.forClass(TokenRequest.class);
        when(requestBuilder.body(tokenRequestArgumentCaptor.capture(), anyString()))
                .thenReturn(requestBuilder);
        bredBanquePopulaireApiClient.getAuthorizeUrl(STATE);

        // when
        final OAuth2Token response =
                bredBanquePopulaireApiClient.exchangeAuthorizationToken(EXCHANGE_CODE);

        // then
        assertThat(response).isEqualTo(expectedTokenResponse.toOauthToken());

        final String expectedTokenRequest = getTokenRequest();
        final String actualTokenRequest = tokenRequestArgumentCaptor.getValue().getBodyValue();
        assertThat(actualTokenRequest).isEqualTo(expectedTokenRequest);
    }

    @Test
    public void shouldExchangeRefreshToken() {
        // given
        final TokenResponse expectedTokenResponse = getTokenResponse();
        final RequestBuilder requestBuilder =
                setUpHttpClientForAuthentication(expectedTokenResponse);

        final ArgumentCaptor<RefreshRequest> refreshRequestArgumentCaptor =
                ArgumentCaptor.forClass(RefreshRequest.class);
        when(requestBuilder.body(refreshRequestArgumentCaptor.capture(), anyString()))
                .thenReturn(requestBuilder);

        // when
        final OAuth2Token response =
                bredBanquePopulaireApiClient.exchangeRefreshToken(EXCHANGE_CODE);

        // then
        assertThat(response).isEqualTo(expectedTokenResponse.toOauthToken());

        final String expectedTokenRequest = getRefreshRequest();
        final String actualTokenRequest = refreshRequestArgumentCaptor.getValue().getBodyValue();
        assertThat(actualTokenRequest).isEqualTo(expectedTokenRequest);
    }

    @Test
    public void shouldRecordCustomerConsent() {
        // given
        final CustomerConsent customerConsent =
                CustomerConsent.builder()
                        .balances(getConsentDataEntities())
                        .transactions(getConsentDataEntities())
                        .psuIdentity(true)
                        .trustedBeneficiaries(true)
                        .build();
        setUpHttpClientMockForConsentRequest();

        // when
        bredBanquePopulaireApiClient.recordCustomerConsent(customerConsent);

        // then no exception
    }

    @Test
    public void shouldFetchAccounts() {
        // given
        final AccountsResponse expectedResponse = getAccountResponseWithoutConsent();
        setUpHttpClientForFetching(
                BredBanquePopulaireConstants.Urls.FETCH_ACCOUNTS, expectedResponse);

        // when
        final AccountsResponse actualResponse = bredBanquePopulaireApiClient.fetchAccounts();

        // then
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    public void shouldFetchBalances() {
        // given
        final String url =
                String.format(BredBanquePopulaireConstants.Urls.FETCH_BALANCES, RESOURCE_ID);
        final BalancesResponse expectedResponse = getBalancesResponse();
        setUpHttpClientForFetching(url, expectedResponse);

        // when
        final BalancesResponse actualResponse =
                bredBanquePopulaireApiClient.fetchBalances(RESOURCE_ID);

        // then
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    public void shouldFetchTransactions() {
        // given
        final String url =
                String.format(BredBanquePopulaireConstants.Urls.FETCH_TRANSACTIONS, RESOURCE_ID);
        final TransactionResponse expectedResponse = getTransactionsResponse(true);
        setUpHttpClientForFetching(url, expectedResponse);

        // when
        final TransactionResponse actualResponse =
                bredBanquePopulaireApiClient.getTransactions(RESOURCE_ID);

        // then
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    public void shouldFetchTransactionsForLastPage() {
        // given
        final String url =
                String.format(
                        BredBanquePopulaireConstants.Urls.FETCH_TRANSACTIONS + "?page=2",
                        RESOURCE_ID);
        final TransactionResponse expectedResponse = getTransactionsResponse(false);
        setUpHttpClientForFetching(url, expectedResponse);

        // when
        final TransactionResponse actualResponse =
                bredBanquePopulaireApiClient.getTransactions(RESOURCE_ID, 2);

        // then
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    public void shouldFetchEndUserIdentity() {
        // given
        final EndUserIdentityResponse expectedResponse = getEndUserIdentityResponse();
        setUpHttpClientForFetching(
                BredBanquePopulaireConstants.Urls.FETCH_IDENTITY_DATA, expectedResponse);

        // when
        final EndUserIdentityResponse actualResponse =
                bredBanquePopulaireApiClient.getEndUserIdentity();

        // then
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    private RequestBuilder setUpHttpClientForAuthentication(Object response) {
        final RequestBuilder requestBuilder = mock(RequestBuilder.class);

        when(requestBuilder.accept(anyString())).thenReturn(requestBuilder);
        when(requestBuilder.post(any())).thenReturn(response);
        when(requestBuilder.queryParam(anyString(), anyString())).thenReturn(requestBuilder);

        when(httpClient.request(new URL(BredBanquePopulaireConstants.Urls.GET_TOKEN)))
                .thenReturn(requestBuilder);
        when(httpClient.request(new URL(BredBanquePopulaireConstants.Urls.OAUTH_URL)))
                .thenReturn(requestBuilder);

        return requestBuilder;
    }

    private void setUpHttpClientMockForConsentRequest() {
        final RequestBuilder requestBuilder = mock(RequestBuilder.class);
        when(requestBuilder.body(any(), any(MediaType.class))).thenReturn(requestBuilder);
        when(requestBuilder.addBearerToken(any())).thenReturn(requestBuilder);
        when(requestBuilder.header(eq(Psd2Headers.Keys.X_REQUEST_ID), any(String.class)))
                .thenReturn(requestBuilder);
        when(requestBuilder.header(eq(Psd2Headers.Keys.SIGNATURE), any(String.class)))
                .thenReturn(requestBuilder);
        when(requestBuilder.header(
                        eq(BredBanquePopulaireConstants.QueryKeys.OCP_APIM_SUBSCRIPTION_KEY),
                        any(String.class)))
                .thenReturn(requestBuilder);
        when(requestBuilder.header(eq(Psd2Headers.Keys.DIGEST), any(String.class)))
                .thenReturn(requestBuilder);

        when(httpClient.request(new URL(BredBanquePopulaireConstants.Urls.CUSTOMERS_CONSENTS)))
                .thenReturn(requestBuilder);
    }

    private void setUpHttpClientForFetching(String urlString, Object response) {
        final RequestBuilder requestBuilder = mock(RequestBuilder.class);
        when(requestBuilder.addBearerToken(any())).thenReturn(requestBuilder);
        when(requestBuilder.header(eq(Psd2Headers.Keys.X_REQUEST_ID), any(String.class)))
                .thenReturn(requestBuilder);
        when(requestBuilder.header(eq(Psd2Headers.Keys.SIGNATURE), any(String.class)))
                .thenReturn(requestBuilder);
        when(requestBuilder.header(
                        eq(BredBanquePopulaireConstants.QueryKeys.OCP_APIM_SUBSCRIPTION_KEY),
                        any(String.class)))
                .thenReturn(requestBuilder);

        when(requestBuilder.get(any())).thenReturn(response);
        when(httpClient.request(new URL(urlString))).thenReturn(requestBuilder);
    }

    private static String getTokenRequest() {
        try {
            final String redirectUri =
                    String.format(
                            BredBanquePopulaireConstants.QueryKeys.STATE, REDIRECT_URL, STATE);
            return String.format(
                    "grant_type=authorization_code&code=%s&redirect_uri=%s&client_id=%s",
                    EXCHANGE_CODE,
                    URLEncoder.encode(redirectUri, StandardCharsets.UTF_8.toString()),
                    CLIENT_ID);
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static String getRefreshRequest() {
        return String.format(
                "client_id=%s&grant_type=refresh_token&refresh_token=%s", CLIENT_ID, EXCHANGE_CODE);
    }
}
