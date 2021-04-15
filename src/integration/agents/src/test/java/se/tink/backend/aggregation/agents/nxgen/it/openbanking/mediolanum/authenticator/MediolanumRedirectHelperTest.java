package se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.MediolanumApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.MediolanumConfiguration;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.MediolanumStorage;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.TestDataReader;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.authenticator.data.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.authenticator.data.UnauthorizedResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class MediolanumRedirectHelperTest {

    private static final String TEST_CODE = "test_code";
    private static final String TEST_STATE = "test_state";
    private static final String TEST_CLIENT_ID = "test_client_id";
    private static final String TEST_CLIENT_SECRET = "test_client_secret";
    private static final String TEST_REDIRECT_URL = "test_redirect_url";

    private MediolanumStorage mockStorage;
    private MediolanumApiClient mockApiClient;
    private MediolanumConfiguration mockConfiguration;

    private MediolanumRedirectHelper redirectHelper;

    @Before
    public void setup() {
        mockStorage = mock(MediolanumStorage.class);
        mockApiClient = mock(MediolanumApiClient.class);
        mockConfiguration = mock(MediolanumConfiguration.class);

        redirectHelper =
                new MediolanumRedirectHelper(mockStorage, mockApiClient, mockConfiguration);

        when(mockConfiguration.getClientId()).thenReturn(TEST_CLIENT_ID);
        when(mockConfiguration.getRedirectUrl()).thenReturn(TEST_REDIRECT_URL);
        when(mockConfiguration.getClientSecret()).thenReturn(TEST_CLIENT_SECRET);
    }

    @Test
    public void shouldBuildAuthorizeUrlProperly() {
        // given
        when(mockApiClient.getRedirectUrl())
                .thenReturn(
                        TestDataReader.readFromFile(
                                TestDataReader.UNAUTHORIZED, UnauthorizedResponse.class));
        // when
        URL url = redirectHelper.buildAuthorizeUrl(TEST_STATE);
        // then
        assertThat(url.toString())
                .isEqualTo(
                        "https://www.test.com/ecm/login?abi=03062&lang=IT&cancel_link=https://tpp.psd2.test.com/&d=asdf&client_id=test_client_id&client_secret=test_client_secret&redirect_uri=test_redirect_url&scope=aisp.base&state=test_state");
    }

    @Test
    public void shouldBuildExchangeTokenRequestBodyProperlyAndMapResponse() {
        // given
        when(mockApiClient.sendToken(any()))
                .thenReturn(TestDataReader.readFromFile(TestDataReader.TOKEN, TokenResponse.class));
        // when
        OAuth2Token oAuth2Token = redirectHelper.exchangeAuthorizationCode(TEST_CODE);
        // then

        verify(mockApiClient)
                .sendToken(
                        "code=test_code&client_id=test_client_id&client_secret=test_client_secret&redirect_uri=test_redirect_url&grant_type=authorization_code");
        assertThat(oAuth2Token.getTokenType()).isEqualTo("Bearer");
        assertThat(oAuth2Token.getAccessToken()).isEqualTo("1234567890");
        assertThat(oAuth2Token.getRefreshToken().isPresent()).isFalse();
        assertThat(oAuth2Token.getExpiresInSeconds()).isEqualTo(7776000);
    }
}
