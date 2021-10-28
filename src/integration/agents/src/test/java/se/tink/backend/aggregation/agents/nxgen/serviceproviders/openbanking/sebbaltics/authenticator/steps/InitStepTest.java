package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.authenticator.steps;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import agents_platform_agents_framework.org.springframework.test.util.ReflectionTestUtils;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.SebBalticsApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.SebBalticsConstants.AuthStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.authenticator.SebBalticsDecoupledAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.authenticator.rpc.DecoupledAuthResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.configuration.SebBalticsConfiguration;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;

@RunWith(JUnitParamsRunner.class)
public class InitStepTest {
    private SebBalticsApiClient apiClient;
    private InitStep initStep;
    private DecoupledAuthResponse decoupledAuthResponse;

    @Before
    public void setUp() {
        SebBalticsDecoupledAuthenticator authenticator =
                mock(SebBalticsDecoupledAuthenticator.class);
        SessionStorage sessionStorage = mock(SessionStorage.class);
        SebBalticsConfiguration configuration = mock(SebBalticsConfiguration.class);
        CredentialsRequest credentialsRequest = mock(CredentialsRequest.class);
        String bankBIC = "abc";
        apiClient = mock(SebBalticsApiClient.class);
        initStep =
                new InitStep(
                        authenticator,
                        apiClient,
                        sessionStorage,
                        configuration,
                        credentialsRequest,
                        bankBIC);
        decoupledAuthResponse = mock(DecoupledAuthResponse.class);
    }

    @Test
    public void shouldNotCheckStatusAgainWhenStatusFinalized() {
        // when
        when(apiClient.getDecoupledAuthStatus(Mockito.any())).thenReturn(decoupledAuthResponse);
        when(decoupledAuthResponse.getStatus()).thenReturn(AuthStatus.FINALIZED);
        ReflectionTestUtils.invokeMethod(initStep, "poll");

        // then
        verify(apiClient, times(1)).getDecoupledAuthStatus(Mockito.any());
    }

    @Test
    public void shouldCheckStatusAgainWhenStatusStarted() {
        // when
        when(apiClient.getDecoupledAuthStatus(Mockito.any())).thenReturn(decoupledAuthResponse);
        when(decoupledAuthResponse.getStatus())
                .thenReturn(AuthStatus.STARTED, AuthStatus.STARTED, AuthStatus.FINALIZED);
        ReflectionTestUtils.invokeMethod(initStep, "poll");

        // then
        verify(apiClient, times(3)).getDecoupledAuthStatus(Mockito.any());
    }

    @Test
    @Parameters(method = "authStatusAndErrorDescription")
    public void shouldThrowAuthenticationErrorWhenAuthStatusUnknown(
            String authStatus, String errorMessage) {
        // when
        when(apiClient.getDecoupledAuthStatus(Mockito.any())).thenReturn(decoupledAuthResponse);
        when(decoupledAuthResponse.getStatus()).thenReturn(authStatus);

        // then
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(initStep, "poll"))
                .isInstanceOf(ThirdPartyAppError.AUTHENTICATION_ERROR.exception().getClass())
                .hasMessage(errorMessage);
    }

    private Object[] authStatusAndErrorDescription() {
        return new Object[] {
            new Object[] {AuthStatus.FAILED, "Authentication failed"},
            new Object[] {"", "Unknown"},
        };
    }
}
