package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenStorage;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.MockRandomValueGenerator;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class SparkassenRedirectHelperTest {

    private static final String TEST_CLIENT_ID = "test_client_id";
    private static final String TEST_STATE = "test_state";
    private static final String TEST_CODE = "test_code";
    private static final String TEST_CODE_VERIFIER = "test_code_verifier";
    private static final String TEST_REFRESH_TOKEN = "test_refresh_token";

    private static final String TOKEN_ENDPOINT =
            "https://xs2a.f-i-apim.de:8443/fixs2aop-env/oauth/75050000/token";

    private SparkassenStorage mockStorage;
    private SparkassenApiClient mockApiClient;

    private SparkassenRedirectHelper sparkassenRedirectHelper;

    @Before
    public void setup() {
        mockStorage = mock(SparkassenStorage.class);
        mockApiClient = mock(SparkassenApiClient.class);

        sparkassenRedirectHelper =
                new SparkassenRedirectHelper(
                        new MockRandomValueGenerator(), mockStorage, mockApiClient, TEST_CLIENT_ID);
    }

    @Test
    public void shouldBuildAuthorizeUrlProperly() {
        // given
        when(mockApiClient.createConsent()).thenReturn(AuthenticatorTestData.CONSENT_RESPONSE_OK);
        when(mockApiClient.getOauthEndpoints(AuthenticatorTestData.TEST_SCA_OAUTH_URL))
                .thenReturn(AuthenticatorTestData.OAUTH_ENDPOINTS);
        // when
        URL url = sparkassenRedirectHelper.buildAuthorizeUrl(TEST_STATE);

        // then
        assertThat(url.toString())
                .isEqualTo(
                        "https://www.sparkasse-regensburg.de/services/xs2a/authorize?response_type=code&client_id=test_client_id&scope=AIS%3A147852369&state=test_state&code_challenge=zuJE2Zn4z0nypO5NiWlRMMnJXDNTjO3wMGiB69QnFNI&code_challenge_method=S256");
    }

    @Test
    public void shouldBuildExchangeTokenProperly() {
        // given
        String expectedBody =
                "code=test_code&client_id=test_client_id&code_verifier=test_code_verifier&grant_type=authorization_code";
        when(mockStorage.getCodeVerifier()).thenReturn(TEST_CODE_VERIFIER);
        when(mockStorage.getTokenEndpoint()).thenReturn(TOKEN_ENDPOINT);
        when(mockApiClient.sendToken(TOKEN_ENDPOINT, expectedBody)).thenReturn(new TokenResponse());

        // when
        sparkassenRedirectHelper.exchangeAuthorizationCode(TEST_CODE);

        // then
        verify(mockApiClient).sendToken(TOKEN_ENDPOINT, expectedBody);
    }

    @Test
    public void shouldBuildRefreshTokenProperly() {
        // given
        String expectedBody = "grant_type=refresh_token&refresh_token=test_refresh_token";
        when(mockStorage.getTokenEndpoint()).thenReturn(TOKEN_ENDPOINT);
        when(mockApiClient.sendToken(TOKEN_ENDPOINT, expectedBody)).thenReturn(new TokenResponse());

        // when
        sparkassenRedirectHelper.refreshAccessToken(TEST_REFRESH_TOKEN);

        // then
        verify(mockApiClient).sendToken(TOKEN_ENDPOINT, expectedBody);
    }
}
