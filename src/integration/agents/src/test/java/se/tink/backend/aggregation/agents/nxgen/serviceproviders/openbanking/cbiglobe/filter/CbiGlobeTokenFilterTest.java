package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.HttpHeaders;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiUrlProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.TestDataReader;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.CbiGlobeConfiguration;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.TokenResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class CbiGlobeTokenFilterTest {

    private static final String TEST_CLIENT_ID = "test_client_id";
    private static final String TEST_CLIENT_SECRET = "test_client_secret";

    private static final URL TEST_NOT_TOKEN_URL =
            new URL("https://example.com/test/definitelyNotTokenUrl");
    private static final URL TEST_TOKEN_URL =
            new URL("https://example.com/auth/oauth/v2/token?asdf=zxcv");
    private static final OAuth2Token VALID_TOKEN =
            new OAuth2Token(
                    "Bearer",
                    "accessTokenFromStatic",
                    null,
                    null,
                    1000000L,
                    0L,
                    System.currentTimeMillis() / 1000L);

    private static final OAuth2Token INVALID_TOKEN =
            new OAuth2Token("Bearer", "54321", null, null, 1L, 0L, 1L);

    private TinkHttpClient mockClient;
    private CbiStorage storage;

    private CbiGlobeTokenFilter cbiGlobeTokenFilter;

    // Due to Filter classes a bit wonky for testing, this is a bit ugly :/
    @Before
    public void setup() {
        mockClient = mock(TinkHttpClient.class);

        // None of the things tested here are saved in anything but session storage, so if NPE
        // happens, it will definitely indicate an error
        storage = new CbiStorage(null, new SessionStorage(), null);

        CbiGlobeConfiguration mockConfiguration = mock(CbiGlobeConfiguration.class);
        when(mockConfiguration.getClientId()).thenReturn(TEST_CLIENT_ID);
        when(mockConfiguration.getClientSecret()).thenReturn(TEST_CLIENT_SECRET);

        CbiUrlProvider testUrlProvider = new CbiUrlProvider("https://example.com");

        cbiGlobeTokenFilter =
                new CbiGlobeTokenFilter(mockClient, storage, mockConfiguration, testUrlProvider);

        HttpResponse mockHttpResponse = mock(HttpResponse.class);
        cbiGlobeTokenFilter.setNext(
                new Filter() {
                    @Override
                    public HttpResponse handle(HttpRequest httpRequest) {
                        when(mockHttpResponse.getRequest()).thenReturn(httpRequest);
                        return mockHttpResponse;
                    }
                });
    }

    @Test
    public void shouldGetNewTokenIfNoneInStorage() {
        // given
        RequestBuilder requestBuilder = testRequestBuilderForTokenCall();
        when(mockClient.request(new URL("https://example.com/auth/oauth/v2/token")))
                .thenReturn(requestBuilder);

        // when
        HttpResponse response =
                cbiGlobeTokenFilter.handle(new HttpRequestImpl(HttpMethod.GET, TEST_NOT_TOKEN_URL));

        // then
        assertThat(storage.getToken()).isPresent();
        assertThat(storage.getToken().get().getExpiresInSeconds()).isEqualTo(1225522L);
        assertThat(storage.getToken().get().getAccessToken()).isEqualTo("accessTokenFromFile");
        // then
        assertThat(response.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .isEqualTo("Bearer accessTokenFromFile");
    }

    @Test
    public void shouldGetNewTokenIfOneInStorageIsNotValid() {
        // given
        storage.saveToken(INVALID_TOKEN);

        RequestBuilder requestBuilder = testRequestBuilderForTokenCall();
        when(mockClient.request(new URL("https://example.com/auth/oauth/v2/token")))
                .thenReturn(requestBuilder);

        // when
        HttpResponse response =
                cbiGlobeTokenFilter.handle(new HttpRequestImpl(HttpMethod.GET, TEST_NOT_TOKEN_URL));

        // then
        assertThat(storage.getToken()).isPresent();
        assertThat(storage.getToken().get().getExpiresInSeconds()).isEqualTo(1225522L);
        assertThat(storage.getToken().get().getAccessToken()).isEqualTo("accessTokenFromFile");
        // then
        assertThat(response.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .isEqualTo("Bearer accessTokenFromFile");
    }

    private RequestBuilder testRequestBuilderForTokenCall() {
        RequestBuilder mockRequestBuilder = mock(RequestBuilder.class);
        when(mockRequestBuilder.addBasicAuth(TEST_CLIENT_ID, TEST_CLIENT_SECRET))
                .thenReturn(mockRequestBuilder);
        when(mockRequestBuilder.queryParam(any(), any())).thenReturn(mockRequestBuilder);
        when(mockRequestBuilder.type(any(String.class))).thenReturn(mockRequestBuilder);
        when(mockRequestBuilder.post(TokenResponse.class, null))
                .thenReturn(TestDataReader.readFromFile(TestDataReader.TOKEN, TokenResponse.class));
        return mockRequestBuilder;
    }

    @Test
    public void shouldAddAuthHeaderToAnyCallButTokenCallWhenTokenInStorageValid() {
        // given
        storage.saveToken(VALID_TOKEN);

        // when
        HttpResponse response =
                cbiGlobeTokenFilter.handle(new HttpRequestImpl(HttpMethod.GET, TEST_NOT_TOKEN_URL));

        // then
        assertThat(response.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .isEqualTo("Bearer accessTokenFromStatic");
    }

    @Test
    public void shouldNotAddAuthHeaderToAcquireTokenCall() {
        // given
        storage.saveToken(VALID_TOKEN);

        // when
        HttpResponse response =
                cbiGlobeTokenFilter.handle(new HttpRequestImpl(HttpMethod.GET, TEST_TOKEN_URL));

        // then
        assertThat(response.getRequest().getHeaders()).doesNotContainKey(HttpHeaders.AUTHORIZATION);
    }
}
