package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.apiclient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.BpceGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.configuration.BpceGroupConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.signature.BpceGroupSignatureHeaderGenerator;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BpceGroupApiClientTest {

    private static final String TOKEN_URL = "http://token-url";
    private static final String EXCHANGE_CODE = "exchange_code";
    private static final String ACCESS_TOKEN = "1234";
    private static final String REFRESH_TOKEN = "2345";
    private static final String TOKEN_TYPE = "Bearer";
    private static final long TOKEN_EXPIRES_IN = 3600L;

    private BpceGroupApiClient bpceGroupApiClient;

    private TinkHttpClient httpClientMock;

    @Before
    public void setUp() {
        final BpceGroupConfiguration configurationMock = mock(BpceGroupConfiguration.class);
        when(configurationMock.getClientId()).thenReturn("cId");
        when(configurationMock.getRedirectUrl()).thenReturn("http://redirect-url");
        when(configurationMock.getTokenUrl()).thenReturn(TOKEN_URL);

        httpClientMock = mock(TinkHttpClient.class);

        final SessionStorage sessionStorageMock = mock(SessionStorage.class);
        final BpceGroupSignatureHeaderGenerator bpceGroupSignatureHeaderGenerator =
                mock(BpceGroupSignatureHeaderGenerator.class);

        bpceGroupApiClient =
                new BpceGroupApiClient(
                        httpClientMock,
                        sessionStorageMock,
                        configurationMock,
                        bpceGroupSignatureHeaderGenerator);
    }

    @Test
    public void shouldExchangeAuthorizationToken() {
        // given
        final TokenResponse expectedTokenResponse = getTokenResponse();
        setUpHttpClientMock(TOKEN_URL, expectedTokenResponse);

        // when
        final TokenResponse returnedResponse =
                bpceGroupApiClient.exchangeAuthorizationToken(EXCHANGE_CODE);

        // then
        assertThat(returnedResponse).isEqualTo(expectedTokenResponse);
    }

    @Test
    public void shouldExchangeRefreshToken() {
        // given
        final TokenResponse expectedTokenResponse = getTokenResponse();
        setUpHttpClientMock(TOKEN_URL, expectedTokenResponse);

        // when
        final TokenResponse returnedResponse =
                bpceGroupApiClient.exchangeRefreshToken(EXCHANGE_CODE);

        // then
        assertThat(returnedResponse).isEqualTo(expectedTokenResponse);
    }

    private void setUpHttpClientMock(String url, Object response) {
        final RequestBuilder requestBuilderMock = mock(RequestBuilder.class);
        when(requestBuilderMock.body(any(), anyString())).thenReturn(requestBuilderMock);
        when(requestBuilderMock.accept(anyString())).thenReturn(requestBuilderMock);

        final HttpResponse httpResponseMock = mock(HttpResponse.class);
        when(httpResponseMock.getBody(any())).thenReturn(response);

        when(requestBuilderMock.post(any())).thenReturn(httpResponseMock);
        when(requestBuilderMock.post(any(), anyString())).thenReturn(httpResponseMock);

        when(httpClientMock.request(new URL(url))).thenReturn(requestBuilderMock);
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
}
