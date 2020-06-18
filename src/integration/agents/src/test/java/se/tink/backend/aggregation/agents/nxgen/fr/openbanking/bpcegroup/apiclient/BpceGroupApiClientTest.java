package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.apiclient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BpceGroupApiClientTest {

    private static final String SERVER_URL = "http://server-url";
    private static final String TOKEN_URL = SERVER_URL + "/stet/psd2/oauth/token";
    private static final String EXCHANGE_CODE = "exchange_code";
    private static final String ACCESS_TOKEN = "1234";
    private static final String REFRESH_TOKEN = "2345";
    private static final String TOKEN_TYPE = "Bearer";
    private static final String SIGNATURE = "beef";
    private static final String RESOURCE_ID = "009988";
    private static final String CLIENT_ID = "cId";
    private static final String REDIRECT_URL = "http://redirect-url";
    private static final long TOKEN_EXPIRES_IN = 3600L;

    private BpceGroupApiClient bpceGroupApiClient;

    private TinkHttpClient httpClientMock;

    @Before
    public void setUp() {
        final BpceGroupConfiguration configurationMock = mock(BpceGroupConfiguration.class);
        final AgentConfiguration<BpceGroupConfiguration> agentConfigurationMock =
                mock(AgentConfiguration.class);
        when(configurationMock.getClientId()).thenReturn(CLIENT_ID);
        when(configurationMock.getServerUrl()).thenReturn(SERVER_URL);
        when(agentConfigurationMock.getClientConfiguration()).thenReturn(configurationMock);
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
                        bpceGroupSignatureHeaderGeneratorMock);
    }

    @Test
    public void shouldExchangeAuthorizationToken() {
        // given
        final TokenResponse expectedTokenResponse = getTokenResponse();
        final RequestBuilder requestBuilderMock =
                setUpHttpClientMockForAuth(TOKEN_URL, expectedTokenResponse);
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
        final RequestBuilder requestBuilderMock =
                setUpHttpClientMockForAuth(TOKEN_URL, expectedTokenResponse);
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

    private RequestBuilder setUpHttpClientMockForAuth(String urlString, Object response) {
        final RequestBuilder requestBuilderMock = mock(RequestBuilder.class);
        when(requestBuilderMock.accept(anyString())).thenReturn(requestBuilderMock);

        final HttpResponse httpResponseMock = mock(HttpResponse.class);
        when(httpResponseMock.getBody(any())).thenReturn(response);

        when(requestBuilderMock.post(any())).thenReturn(httpResponseMock);
        when(requestBuilderMock.post(any(), anyString())).thenReturn(httpResponseMock);

        when(httpClientMock.request(new URL(urlString))).thenReturn(requestBuilderMock);

        return requestBuilderMock;
    }

    private void setUpHttpClientMockForApi(String urlString, Object response) {
        final RequestBuilder requestBuilderMock = mock(RequestBuilder.class);
        when(requestBuilderMock.body(any(), anyString())).thenReturn(requestBuilderMock);
        when(requestBuilderMock.addBearerToken(any())).thenReturn(requestBuilderMock);
        when(requestBuilderMock.header(anyString(), anyString())).thenReturn(requestBuilderMock);

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
        when(requestBuilderMock.header(anyString(), anyString())).thenReturn(requestBuilderMock);

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

    private static TokenResponse getTokenResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "\"access_token\":\""
                        + ACCESS_TOKEN
                        + "\",\n"
                        + "\"token_type\":\""
                        + TOKEN_TYPE
                        + "\",\n"
                        + "\"expires_in\":"
                        + TOKEN_EXPIRES_IN
                        + ",\n"
                        + "\"refresh_token\":\""
                        + REFRESH_TOKEN
                        + "\",\n"
                        + "\"scope\":\"xx\",\n"
                        + "\"state\":\"abc\"\n"
                        + "}",
                TokenResponse.class);
    }

    private static AccountsResponse getAccountsResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "\"accounts\": [\n"
                        + "    {\n"
                        + "      \"cashAccountType\": \"CACC\",\n"
                        + "      \"accountId\": {\n"
                        + "        \"iban\": \"FR7613807008043001965409135\"\n"
                        + "      },\n"
                        + "      \"resourceId\": \""
                        + RESOURCE_ID
                        + "\",\n"
                        + "      \"product\": \"COMPTE COURANT\",\n"
                        + "      \"_links\": {},\n"
                        + "      \"usage\": \"ORGA\",\n"
                        + "      \"psuStatus\": \"Account Holder\",\n"
                        + "      \"name\": \"Account\",\n"
                        + "      \"bicFi\": \"CCBPFRPPNAN\",\n"
                        + "      \"currency\": \"EUR\",\n"
                        + "      \"details\": \"det\"\n"
                        + "    }\n"
                        + "  ]\n"
                        + "}",
                AccountsResponse.class);
    }

    private static BalancesResponse getBalancesResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"balances\": [\n"
                        + "    {\n"
                        + "      \"balanceType\": \"VALU\",\n"
                        + "      \"name\": \"Bal1\",\n"
                        + "      \"balanceAmount\": {\n"
                        + "        \"amount\": \"4321.95\",\n"
                        + "        \"currency\": \"EUR\"\n"
                        + "      },\n"
                        + "      \"referenceDate\": \"2019-05-16\"\n"
                        + "    },\n"
                        + "    {\n"
                        + "      \"balanceType\": \"CLBD\",\n"
                        + "      \"name\": \"Bal2\",\n"
                        + "      \"balanceAmount\": {\n"
                        + "        \"amount\": \"4179.95\",\n"
                        + "        \"currency\": \"EUR\"\n"
                        + "      },\n"
                        + "      \"referenceDate\": \"2019-05-15\"\n"
                        + "    },\n"
                        + "    {\n"
                        + "      \"balanceType\": \"OTHR\",\n"
                        + "      \"name\": \"Bal3\",\n"
                        + "      \"balanceAmount\": {\n"
                        + "        \"amount\": \"4348.95\",\n"
                        + "        \"currency\": \"EUR\"\n"
                        + "      }\n"
                        + "    }\n"
                        + "  ]\n"
                        + "}",
                BalancesResponse.class);
    }
}
