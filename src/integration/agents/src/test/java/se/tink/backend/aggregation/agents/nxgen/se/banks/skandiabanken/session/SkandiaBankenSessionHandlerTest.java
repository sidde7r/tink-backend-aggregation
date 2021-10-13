package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.session;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import agents_platform_agents_framework.org.springframework.test.util.ReflectionTestUtils;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.authenticator.rpc.OAuth2TokenResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(MockitoJUnitRunner.class)
public class SkandiaBankenSessionHandlerTest {
    private SkandiaBankenSessionHandler objectUnderTest;
    private SkandiaBankenApiClient apiClient;
    @Mock private HttpResponse httpResponse;
    @Mock private HttpResponseException httpResponseException;

    @Before
    public void setUp() {
        apiClient = mock(SkandiaBankenApiClient.class);
        objectUnderTest = new SkandiaBankenSessionHandler(apiClient, new PersistentStorage());
    }

    @Test
    public void shouldReturnTokenOnSuccessfulTokenRefresh() {
        // given
        when(apiClient.refreshToken(any())).thenReturn(TOKEN_RESPONSE);

        // when
        final OAuth2Token oAuth2Token =
                ReflectionTestUtils.invokeMethod(objectUnderTest, "getOAuth2Token", "refreshToken");

        // then
        assertNotNull(oAuth2Token);
    }

    @Test
    public void shouldThrowSessionErrorOnExpectedExpiredTokenResponse() {
        // given
        when(httpResponse.getStatus()).thenReturn(401);
        when(httpResponseException.getResponse()).thenReturn(httpResponse);
        when(apiClient.refreshToken(any())).thenThrow(httpResponseException);

        // when
        final ThrowingCallable callable =
                () ->
                        ReflectionTestUtils.invokeMethod(
                                objectUnderTest, "getOAuth2Token", "refreshToken");

        // then
        assertThatThrownBy(callable).isInstanceOf(SessionException.class);
    }

    @Test
    public void shouldThrowSessionErrorOnUnexpectedErrorResponse() {
        // given
        when(httpResponse.getStatus()).thenReturn(500);
        when(httpResponseException.getResponse()).thenReturn(httpResponse);
        when(apiClient.refreshToken(any())).thenThrow(httpResponseException);

        // when
        final ThrowingCallable callable =
                () ->
                        ReflectionTestUtils.invokeMethod(
                                objectUnderTest, "getOAuth2Token", "refreshToken");

        // then
        assertThatThrownBy(callable).isInstanceOf(SessionException.class);
    }

    private static final OAuth2TokenResponse TOKEN_RESPONSE =
            SerializationUtils.deserializeFromString(
                    "{\n"
                            + "  \"access_token\": \"mockedAccessToken\",\n"
                            + "  \"token_type\": \"Bearer\",\n"
                            + "  \"expires_in\": 900,\n"
                            + "  \"refresh_token\": \"mockedRefreshToken\",\n"
                            + "  \"offline_token\": \"mockedOfflineToken\",\n"
                            + "  \"offline_refresh_token\": \"mockedOfflineRefreshToken\"\n"
                            + "}",
                    OAuth2TokenResponse.class);
}
