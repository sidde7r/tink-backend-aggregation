package se.tink.backend.aggregation.workers.commands.login;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.agentplatform.authentication.result.error.AgentPlatformAuthenticationProcessError;
import se.tink.backend.aggregation.agents.agentplatform.authentication.result.error.AgentPlatformAuthenticationProcessException;
import se.tink.backend.aggregation.agents.agentplatform.authentication.result.error.NoUserInteractionResponseError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.workers.commands.login.handler.result.AgentPlatformLoginErrorResult;
import se.tink.backend.aggregation.workers.commands.login.handler.result.LoginAuthenticationErrorResult;
import se.tink.backend.aggregation.workers.metrics.MetricActionIface;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;

public class LoginMetricLoginResultVisitorTest {

    private Credentials credentials;
    private MetricActionIface metricActionIface;
    private LoginMetricLoginResultVisitor objectUnderTest;

    @Before
    public void init() {
        credentials = Mockito.mock(Credentials.class);
        metricActionIface = Mockito.mock(MetricActionIface.class);
        objectUnderTest = new LoginMetricLoginResultVisitor(metricActionIface, credentials);
    }

    @Test
    public void shouldAddCancelledDueToThirdPartyAppTimeoutMetricForLegacyTimeoutError() {
        // given
        Mockito.when(credentials.getStatus())
                .thenReturn(CredentialsStatus.AWAITING_THIRD_PARTY_APP_AUTHENTICATION);
        LoginAuthenticationErrorResult loginAuthenticationErrorResult =
                Mockito.mock(LoginAuthenticationErrorResult.class);
        Mockito.when(loginAuthenticationErrorResult.getException())
                .thenReturn(ThirdPartyAppError.TIMED_OUT.exception());

        // when
        objectUnderTest.visit(loginAuthenticationErrorResult);

        // then
        Mockito.verify(metricActionIface).cancelledDueToThirdPartyAppTimeout();
        Mockito.verifyNoMoreInteractions(metricActionIface);
    }

    @Test
    public void
            shouldAddCancelledMetricForLegacyAgentWhenTheCredentialStatusIsNotAwaitingThirdPartyAppAuthentication() {
        // given
        Mockito.when(credentials.getStatus())
                .thenReturn(CredentialsStatus.AWAITING_SUPPLEMENTAL_INFORMATION);
        LoginAuthenticationErrorResult loginAuthenticationErrorResult =
                Mockito.mock(LoginAuthenticationErrorResult.class);
        Mockito.when(loginAuthenticationErrorResult.getException())
                .thenReturn(ThirdPartyAppError.TIMED_OUT.exception());

        // when
        objectUnderTest.visit(loginAuthenticationErrorResult);

        // then
        Mockito.verify(metricActionIface).cancelled();
        Mockito.verifyNoMoreInteractions(metricActionIface);
    }

    @Test
    public void
            shouldAddCancelledDueToThirdPartyAppTimeoutMetricForAgentPlatformAgentWhenThereIsTimeoutError() {
        // given
        Mockito.when(credentials.getStatus())
                .thenReturn(CredentialsStatus.AWAITING_THIRD_PARTY_APP_AUTHENTICATION);
        AgentPlatformLoginErrorResult agentPlatformLoginErrorResult =
                Mockito.mock(AgentPlatformLoginErrorResult.class);
        AgentPlatformAuthenticationProcessException agentPlatformAuthenticationProcessException =
                new AgentPlatformAuthenticationProcessException(
                        new AgentPlatformAuthenticationProcessError(
                                new NoUserInteractionResponseError()),
                        "");
        Mockito.when(agentPlatformLoginErrorResult.getException())
                .thenReturn(agentPlatformAuthenticationProcessException);

        // when
        objectUnderTest.visit(agentPlatformLoginErrorResult);

        // then
        Mockito.verify(metricActionIface).cancelledDueToThirdPartyAppTimeout();
        Mockito.verifyNoMoreInteractions(metricActionIface);
    }

    @Test
    public void
            shouldAddCancelledMetricForAgentPlatformAgentWhenTheCredentialStatusIsNotAwaitingThirdPartyAppAuthentication() {
        // given
        Mockito.when(credentials.getStatus())
                .thenReturn(CredentialsStatus.AWAITING_SUPPLEMENTAL_INFORMATION);
        AgentPlatformLoginErrorResult agentPlatformLoginErrorResult =
                Mockito.mock(AgentPlatformLoginErrorResult.class);
        AgentPlatformAuthenticationProcessException agentPlatformAuthenticationProcessException =
                new AgentPlatformAuthenticationProcessException(
                        new AgentPlatformAuthenticationProcessError(
                                new NoUserInteractionResponseError()),
                        "");
        Mockito.when(agentPlatformLoginErrorResult.getException())
                .thenReturn(agentPlatformAuthenticationProcessException);

        // when
        objectUnderTest.visit(agentPlatformLoginErrorResult);

        // then
        Mockito.verify(metricActionIface).cancelled();
        Mockito.verifyNoMoreInteractions(metricActionIface);
    }
}
