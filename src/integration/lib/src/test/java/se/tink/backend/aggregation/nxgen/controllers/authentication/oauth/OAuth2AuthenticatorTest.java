package se.tink.backend.aggregation.nxgen.controllers.authentication.oauth;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SteppableAuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SteppableAuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementalWaitRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;

public class OAuth2AuthenticatorTest {

    private OAuth2AuthorizationServerClient authorizationServerClient;
    private OAuth2TokenStorage tokenStorage;
    private OAuth2Authenticator objectUnderTest;

    @Before
    public void init() {
        authorizationServerClient = Mockito.mock(OAuth2AuthorizationServerClient.class);
        tokenStorage = Mockito.mock(OAuth2TokenStorage.class);
        objectUnderTest = new OAuth2Authenticator(authorizationServerClient, tokenStorage);
    }

    @Test
    public void shouldRequestForAuthorizationWhenTokenIsNotPresent()
            throws AuthenticationException, AuthorizationException {
        // given
        ThirdPartyAppAuthenticationPayload authenticationPayload =
                Mockito.mock(ThirdPartyAppAuthenticationPayload.class);
        SupplementalWaitRequest waitRequest =
                new SupplementalWaitRequest("key", 60, TimeUnit.MINUTES);
        Mockito.when(tokenStorage.fetchToken()).thenReturn(Optional.empty());
        Mockito.when(authorizationServerClient.getAuthorizationEndpointPayload())
                .thenReturn(authenticationPayload);
        Mockito.when(authorizationServerClient.getWaitingForResponseConfiguration())
                .thenReturn(waitRequest);
        SteppableAuthenticationRequest steppableAuthenticationRequest =
                SteppableAuthenticationRequest.initialRequest(Mockito.mock(Credentials.class));
        // when
        SteppableAuthenticationResponse result =
                objectUnderTest.processAuthentication(steppableAuthenticationRequest);
        // then
        Assert.assertEquals(
                authenticationPayload,
                result.getSupplementInformationRequester().getThirdPartyAppPayload().get());
        Assert.assertEquals(
                waitRequest,
                result.getSupplementInformationRequester().getSupplementalWaitRequest().get());
        Assert.assertEquals(
                OAuth2Authenticator.AUTHORIZATION_SERVER_THIRD_PARTY_APP_STEP_ID,
                result.getStepIdentifier().get());
        Assert.assertFalse(result.getSupplementInformationRequester().getFields().isPresent());
    }

    @Test
    public void shouldHandleAuthorizationResponseAndIssueToken()
            throws AuthenticationException, AuthorizationException {
        // given
        OAuth2Token token = Mockito.mock(OAuth2Token.class);
        Map<String, String> callbackData = new HashMap<>();
        callbackData.put("code", "32423423");
        SteppableAuthenticationRequest steppableAuthenticationRequest =
                SteppableAuthenticationRequest.subsequentRequest(
                        OAuth2Authenticator.AUTHORIZATION_SERVER_THIRD_PARTY_APP_STEP_ID,
                        new AuthenticationRequest(Mockito.mock(Credentials.class))
                                .withCallbackData(callbackData));
        Mockito.when(authorizationServerClient.handleAuthorizationResponse(Mockito.anyMap()))
                .thenReturn(token);
        // when
        SteppableAuthenticationResponse result =
                objectUnderTest.processAuthentication(steppableAuthenticationRequest);
        // then
        Mockito.verify(tokenStorage).storeToken(token);
        Assert.assertFalse(result.getStepIdentifier().isPresent());
    }

    @Test
    public void shouldUseStoredAccessTokenToAuthenticate()
            throws AuthenticationException, AuthorizationException {
        // given
        OAuth2Token token = Mockito.mock(OAuth2Token.class);
        Mockito.when(token.isValid()).thenReturn(true);
        Mockito.when(token.hasAccessExpired()).thenReturn(false);
        Mockito.when(tokenStorage.fetchToken()).thenReturn(Optional.of(token));
        SteppableAuthenticationRequest steppableAuthenticationRequest =
                SteppableAuthenticationRequest.initialRequest(Mockito.mock(Credentials.class));
        // when
        SteppableAuthenticationResponse result =
                objectUnderTest.processAuthentication(steppableAuthenticationRequest);
        // then
        Assert.assertFalse(result.getStepIdentifier().isPresent());
    }

    @Test
    public void shouldRefreshToken() throws AuthenticationException, AuthorizationException {
        // given
        final String refreshToken = "fakeRefreshToken";
        OAuth2Token token = Mockito.mock(OAuth2Token.class);
        Mockito.when(token.isValid()).thenReturn(false);
        Mockito.when(token.hasAccessExpired()).thenReturn(true);
        Mockito.when(token.canRefresh()).thenReturn(true);
        Mockito.when(token.getRefreshToken()).thenReturn(refreshToken);
        Mockito.when(tokenStorage.fetchToken()).thenReturn(Optional.of(token));
        OAuth2Token newToken = Mockito.mock(OAuth2Token.class);
        ;
        Mockito.when(authorizationServerClient.refreshAccessToken(refreshToken))
                .thenReturn(newToken);
        SteppableAuthenticationRequest steppableAuthenticationRequest =
                SteppableAuthenticationRequest.initialRequest(Mockito.mock(Credentials.class));
        // when
        SteppableAuthenticationResponse result =
                objectUnderTest.processAuthentication(steppableAuthenticationRequest);
        // then
        Assert.assertFalse(result.getStepIdentifier().isPresent());
        Mockito.verify(tokenStorage).storeToken(newToken);
    }

    @Test
    public void shouldRequestForAuthorizationWhenTokeCanNotBeRefreshed()
            throws AuthenticationException, AuthorizationException {
        // given
        OAuth2Token token = Mockito.mock(OAuth2Token.class);
        Mockito.when(token.isValid()).thenReturn(false);
        Mockito.when(token.hasAccessExpired()).thenReturn(true);
        Mockito.when(token.canRefresh()).thenReturn(false);
        ThirdPartyAppAuthenticationPayload authenticationPayload =
                Mockito.mock(ThirdPartyAppAuthenticationPayload.class);
        SupplementalWaitRequest waitRequest =
                new SupplementalWaitRequest("key", 60, TimeUnit.MINUTES);
        Mockito.when(authorizationServerClient.getAuthorizationEndpointPayload())
                .thenReturn(authenticationPayload);
        Mockito.when(authorizationServerClient.getWaitingForResponseConfiguration())
                .thenReturn(waitRequest);
        SteppableAuthenticationRequest steppableAuthenticationRequest =
                SteppableAuthenticationRequest.initialRequest(Mockito.mock(Credentials.class));
        // when
        SteppableAuthenticationResponse result =
                objectUnderTest.processAuthentication(steppableAuthenticationRequest);
        // then
        Assert.assertEquals(
                authenticationPayload,
                result.getSupplementInformationRequester().getThirdPartyAppPayload().get());
        Assert.assertEquals(
                waitRequest,
                result.getSupplementInformationRequester().getSupplementalWaitRequest().get());
        Assert.assertEquals(
                OAuth2Authenticator.AUTHORIZATION_SERVER_THIRD_PARTY_APP_STEP_ID,
                result.getStepIdentifier().get());
        Assert.assertFalse(result.getSupplementInformationRequester().getFields().isPresent());
    }
}
