package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.AccessCodeStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.OAuth2ThirdPartyAppRequestParamsProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.ThirdPartyAppCallbackProcessor;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.ThirdPartyAppAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class ThirdPartyAppStepTest extends StepTestBase {

    private static final String STATE = "state";
    private static final URL AUTHORIZE_URL = new URL("https://authorize-url");
    private static final String ACCESS_CODE = "code1, code2";

    private ThirdPartyAppAuthenticationStep thirdPartyAppAuthenticationStep;

    private ThirdPartyAppCallbackProcessor thirdPartyAppCallbackProcessorMock;

    private AccessCodeStorage accessCodeStorageMock;

    @Before
    public void setUp() {
        final StrongAuthenticationState strongAuthenticationStateMock =
                mock(StrongAuthenticationState.class);
        when(strongAuthenticationStateMock.getState()).thenReturn(STATE);

        final OAuth2ThirdPartyAppRequestParamsProvider
                oAuth2ThirdPartyAppRequestParamsProviderMock =
                        mock(OAuth2ThirdPartyAppRequestParamsProvider.class);
        when(oAuth2ThirdPartyAppRequestParamsProviderMock.getAuthorizeUrl(STATE))
                .thenReturn(AUTHORIZE_URL);

        thirdPartyAppCallbackProcessorMock = mock(ThirdPartyAppCallbackProcessor.class);

        when(thirdPartyAppCallbackProcessorMock.getOAuth2ThirdPartyAppRequestParamsProvider())
                .thenReturn(oAuth2ThirdPartyAppRequestParamsProviderMock);
        accessCodeStorageMock = mock(AccessCodeStorage.class);

        thirdPartyAppAuthenticationStep =
                new ThirdPartyAppAuthenticationStepCreator(
                                thirdPartyAppCallbackProcessorMock,
                                accessCodeStorageMock,
                                strongAuthenticationStateMock)
                        .create();
    }

    @Test
    public void shouldReturnExecuteNextStepWhenThirdPartyAuthorizationIsSuccessful()
            throws AuthenticationException, AuthorizationException {
        // given
        final ImmutableMap<String, String> callbackData = ImmutableMap.of("key", "value");
        final AuthenticationRequest authenticationRequest =
                createAuthenticationRequest(callbackData);
        when(thirdPartyAppCallbackProcessorMock.isThirdPartyAppLoginSuccessful(callbackData))
                .thenReturn(Boolean.TRUE);
        when(thirdPartyAppCallbackProcessorMock.getAccessCodeFromCallbackData(callbackData))
                .thenReturn(ACCESS_CODE);

        // when
        final AuthenticationStepResponse response =
                executeStepAndGetResponse(thirdPartyAppAuthenticationStep, authenticationRequest);

        // then
        assertThat(response.getNextStepId().isPresent()).isFalse();
        assertThat(response.isAuthenticationFinished()).isFalse();
        verify(accessCodeStorageMock).storeAccessCodeInSession(ACCESS_CODE);
    }

    @Test
    public void shouldThrowExceptionWhenThirdPartyAppLoginIsNotSuccessful() {
        // given
        final ImmutableMap<String, String> callbackData = ImmutableMap.of("key", "value");
        final AuthenticationRequest authenticationRequest =
                createAuthenticationRequest(callbackData);
        when(thirdPartyAppCallbackProcessorMock.isThirdPartyAppLoginSuccessful(callbackData))
                .thenReturn(Boolean.FALSE);

        // when
        final Throwable thrown =
                catchThrowable(
                        () ->
                                executeStepAndGetResponse(
                                        thirdPartyAppAuthenticationStep, authenticationRequest));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(AuthorizationException.class)
                .hasMessage("Authorization failed.");
        verify(accessCodeStorageMock, never()).storeAccessCodeInSession(anyString());
    }

    private static AuthenticationRequest createAuthenticationRequest(
            ImmutableMap<String, String> callbackData) {
        final AuthenticationRequest authenticationRequest =
                new AuthenticationRequest(mock(Credentials.class));

        return authenticationRequest.withCallbackData(callbackData);
    }
}
