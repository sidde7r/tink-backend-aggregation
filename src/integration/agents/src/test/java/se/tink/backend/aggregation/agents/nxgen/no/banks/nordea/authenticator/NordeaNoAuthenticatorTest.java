package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdTestUtils.mockProxyResponseWithHeaders;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdTestUtils.verifyThatFromUsersPerspectiveThrowableIsTheSameAsGivenAgentException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.nio.file.Paths;
import java.util.Optional;
import javax.annotation.Nullable;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.NordeaNoStorage;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator.rpc.OauthTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.client.AuthenticationClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdIframeAuthenticationResult;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.proxy.ResponseFromProxy;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@RunWith(JUnitParamsRunner.class)
public class NordeaNoAuthenticatorTest {

    private static final String TEST_DATA_DIR =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/banks/nordea/resources";
    private static final File OAUTH_TOKEN_RESPONSE_FILE =
            Paths.get(TEST_DATA_DIR, "oauthTokenResponse.json").toFile();

    /*
    Mocks
     */
    private AuthenticationClient authenticationClient;
    private NordeaNoStorage storage;

    private InOrder mocksToVerifyInOrder;

    /*
    Real
     */
    private NordeaNoAuthenticator authenticator;

    @Before
    public void setup() {
        authenticationClient = mock(AuthenticationClient.class);
        storage = mock(NordeaNoStorage.class);

        mocksToVerifyInOrder = inOrder(authenticationClient, storage);

        authenticator = new NordeaNoAuthenticator(authenticationClient, storage);
    }

    @Test
    public void should_auto_authenticate_using_refresh_token_from_storage() {
        // given
        OAuth2Token storageToken = mockOAuth2TokenWithRefreshToken("CURRENT_REFRESH_TOKEN");
        when(storage.retrieveOauthToken()).thenReturn(Optional.of(storageToken));

        OauthTokenResponse tokenResponse =
                deserializeFromFile(OAUTH_TOKEN_RESPONSE_FILE, OauthTokenResponse.class);
        when(authenticationClient.refreshAccessToken(any())).thenReturn(tokenResponse);

        // when
        authenticator.autoAuthenticate();

        // then
        mocksToVerifyInOrder.verify(storage).retrieveOauthToken();
        mocksToVerifyInOrder
                .verify(authenticationClient)
                .refreshAccessToken("CURRENT_REFRESH_TOKEN");
        mocksToVerifyInOrder
                .verify(storage)
                .storeOauthToken(tokenResponse.toOauthToken().orElse(null));
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_throw_session_expired_exception_when_there_is_no_current_token() {
        // given
        when(storage.retrieveOauthToken()).thenReturn(Optional.empty());

        // when
        Throwable throwable = catchThrowable(() -> authenticator.autoAuthenticate());

        // then
        verifyThatFromUsersPerspectiveThrowableIsTheSameAsGivenAgentException(
                throwable, SessionError.SESSION_EXPIRED.exception());

        mocksToVerifyInOrder.verify(storage).retrieveOauthToken();
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_throw_session_expired_exception_when_current_token_has_no_refresh_token() {
        // given
        OAuth2Token storageToken = mockOAuth2TokenWithRefreshToken(null);
        when(storage.retrieveOauthToken()).thenReturn(Optional.of(storageToken));

        // when
        Throwable throwable = catchThrowable(() -> authenticator.autoAuthenticate());

        // then
        verifyThatFromUsersPerspectiveThrowableIsTheSameAsGivenAgentException(
                throwable, SessionError.SESSION_EXPIRED.exception());

        mocksToVerifyInOrder.verify(storage).retrieveOauthToken();
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    @Parameters(method = "invalidRefreshTokenResponses")
    public void should_throw_session_expired_exception_when_refresh_token_response_is_invalid(
            OauthTokenResponse tokenResponse) {
        // given
        OAuth2Token storageToken = mockOAuth2TokenWithRefreshToken("CURRENT_REFRESH_TOKEN123");
        when(storage.retrieveOauthToken()).thenReturn(Optional.of(storageToken));

        when(authenticationClient.refreshAccessToken(any())).thenReturn(tokenResponse);

        // when
        Throwable throwable = catchThrowable(() -> authenticator.autoAuthenticate());

        // then
        verifyThatFromUsersPerspectiveThrowableIsTheSameAsGivenAgentException(
                throwable, SessionError.SESSION_EXPIRED.exception());

        mocksToVerifyInOrder.verify(storage).retrieveOauthToken();
        mocksToVerifyInOrder
                .verify(authenticationClient)
                .refreshAccessToken("CURRENT_REFRESH_TOKEN123");
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @SuppressWarnings("unused")
    private static Object[] invalidRefreshTokenResponses() {
        OauthTokenResponse tokenWithoutAccessToken =
                deserializeFromFile(OAUTH_TOKEN_RESPONSE_FILE, OauthTokenResponse.class);
        tokenWithoutAccessToken.setAccessToken(null);

        OauthTokenResponse tokenWithoutRefreshToken =
                deserializeFromFile(OAUTH_TOKEN_RESPONSE_FILE, OauthTokenResponse.class);
        tokenWithoutRefreshToken.setRefreshToken(null);

        OauthTokenResponse tokenExpired =
                deserializeFromFile(OAUTH_TOKEN_RESPONSE_FILE, OauthTokenResponse.class);
        tokenExpired.setExpiresIn(0);

        return new Object[] {tokenWithoutAccessToken, tokenWithoutRefreshToken, tokenExpired};
    }

    @Test
    public void should_extract_authorization_code_and_finish_manual_authentication() {
        // given
        OauthTokenResponse tokenResponse =
                deserializeFromFile(OAUTH_TOKEN_RESPONSE_FILE, OauthTokenResponse.class);
        when(authenticationClient.getOathToken(any(), any())).thenReturn(tokenResponse);

        when(storage.retrieveCodeVerifier()).thenReturn("STORAGE_CODE_VERIFIER");

        // when
        ResponseFromProxy responseFromProxy =
                mockProxyResponseWithHeaders(
                        ImmutableMap.of(
                                "someKey1", "someValue1",
                                "Location",
                                        "http://redirect.url?key1=value1&code=AUTH_CODE&key2=value2",
                                "someKey2", "someValue2"));
        BankIdIframeAuthenticationResult iframeAuthenticationResult =
                mock(BankIdIframeAuthenticationResult.class);
        when(iframeAuthenticationResult.getProxyResponseFromAuthFinishUrl())
                .thenReturn(responseFromProxy);

        authenticator.handleBankIdAuthenticationResult(iframeAuthenticationResult);

        // then
        mocksToVerifyInOrder.verify(storage).retrieveCodeVerifier();
        mocksToVerifyInOrder
                .verify(authenticationClient)
                .getOathToken("AUTH_CODE", "STORAGE_CODE_VERIFIER");
        mocksToVerifyInOrder
                .verify(storage)
                .storeOauthToken(tokenResponse.toOauthToken().orElse(null));
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void should_throw_illegal_state_exception_on_missing_redirect_url_in_proxy_response() {
        // given
        OauthTokenResponse tokenResponse =
                deserializeFromFile(OAUTH_TOKEN_RESPONSE_FILE, OauthTokenResponse.class);
        when(authenticationClient.getOathToken(any(), any())).thenReturn(tokenResponse);

        // when
        ResponseFromProxy responseFromProxy =
                mockProxyResponseWithHeaders(
                        ImmutableMap.of(
                                "someKey1", "someValue1",
                                "someKey2", "someValue2"));
        BankIdIframeAuthenticationResult iframeAuthenticationResult =
                mock(BankIdIframeAuthenticationResult.class);
        when(iframeAuthenticationResult.getProxyResponseFromAuthFinishUrl())
                .thenReturn(responseFromProxy);

        Throwable throwable =
                catchThrowable(
                        () ->
                                authenticator.handleBankIdAuthenticationResult(
                                        iframeAuthenticationResult));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot find Location header in proxy response");

        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void
            should_throw_illegal_state_exception_on_missing_authorization_code_in_redirect_url() {
        // given
        OauthTokenResponse tokenResponse =
                deserializeFromFile(OAUTH_TOKEN_RESPONSE_FILE, OauthTokenResponse.class);
        when(authenticationClient.getOathToken(any(), any())).thenReturn(tokenResponse);

        // when
        ResponseFromProxy responseFromProxy =
                mockProxyResponseWithHeaders(
                        ImmutableMap.of(
                                "someKey1", "someValue1",
                                "Location", "http://redirect.url?key1=value1&key2=value2",
                                "someKey2", "someValue2"));
        BankIdIframeAuthenticationResult iframeAuthenticationResult =
                mock(BankIdIframeAuthenticationResult.class);
        when(iframeAuthenticationResult.getProxyResponseFromAuthFinishUrl())
                .thenReturn(responseFromProxy);

        Throwable throwable =
                catchThrowable(
                        () ->
                                authenticator.handleBankIdAuthenticationResult(
                                        iframeAuthenticationResult));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot extract authorization code from proxy response");

        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    private static OAuth2Token mockOAuth2TokenWithRefreshToken(@Nullable String refreshToken) {
        OAuth2Token token = mock(OAuth2Token.class);
        when(token.getRefreshToken()).thenReturn(Optional.ofNullable(refreshToken));
        return token;
    }

    @SneakyThrows
    @SuppressWarnings("SameParameterValue")
    private static <T> T deserializeFromFile(File file, Class<T> tClass) {
        return new ObjectMapper().readValue(file, tClass);
    }
}
