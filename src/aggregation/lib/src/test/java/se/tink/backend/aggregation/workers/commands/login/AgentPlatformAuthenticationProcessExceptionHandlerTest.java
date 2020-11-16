package se.tink.backend.aggregation.workers.commands.login;

import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.aggregation.agents.agentplatform.authentication.result.error.AgentPlatformAuthenticationProcessException;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentException;
import se.tink.backend.aggregation.agentsplatform.framework.error.AuthenticationError;
import se.tink.backend.aggregation.agentsplatform.framework.error.AuthorizationError;
import se.tink.backend.aggregation.agentsplatform.framework.error.ServerError;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.metrics.MetricAction;
import se.tink.backend.aggregation.workers.metrics.MetricActionIface;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.eventproducerservice.events.grpc.AgentLoginCompletedEventProto;

public class AgentPlatformAuthenticationProcessExceptionHandlerTest {

    private AgentPlatformAuthenticationProcessExceptionHandler objectUnderTest;
    private StatusUpdater statusUpdater;
    private AgentWorkerCommandContext context;
    private AgentLoginEventPublisherService agentLoginEventPublisherService;
    private MetricActionIface metricAction;

    @Before
    public void init() {
        statusUpdater = Mockito.mock(StatusUpdater.class);
        context = Mockito.mock(AgentWorkerCommandContext.class);
        agentLoginEventPublisherService = Mockito.mock(AgentLoginEventPublisherService.class);
        metricAction = Mockito.mock(MetricAction.class);
        objectUnderTest =
                new AgentPlatformAuthenticationProcessExceptionHandler(
                        statusUpdater, context, agentLoginEventPublisherService);
    }

    @Test
    public void shouldHandleAgentPlatformServerError() {
        // given
        ServerError serverError = new ServerError();
        AgentPlatformAuthenticationProcessException ex =
                Mockito.mock(AgentPlatformAuthenticationProcessException.class);
        Mockito.when(ex.getSourceAgentPlatformError()).thenReturn(serverError);
        // when
        Optional<AgentWorkerCommandResult> result = objectUnderTest.handle(ex, metricAction);
        // then
        Assertions.assertThat(result.isPresent()).isTrue();
        Assertions.assertThat(result.get()).isEqualTo(AgentWorkerCommandResult.ABORT);
        Assertions.assertThat(objectUnderTest.getCredentialsStatus())
                .isEqualTo(CredentialsStatus.TEMPORARY_ERROR);
        Mockito.verify(metricAction).unavailable();
        Mockito.verify(agentLoginEventPublisherService)
                .publishLoginResultEvent(
                        AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult
                                .BANK_SERVICE_ERROR_UNKNOWN);
    }

    @Test
    public void shouldHandleAgentPlatformAuthenticationError() {
        // given
        AuthenticationError error = new AuthenticationError();
        AgentPlatformAuthenticationProcessException ex =
                Mockito.mock(AgentPlatformAuthenticationProcessException.class);
        Mockito.when(ex.getSourceAgentPlatformError()).thenReturn(error);
        // when
        Optional<AgentWorkerCommandResult> result = objectUnderTest.handle(ex, metricAction);
        // then
        Assertions.assertThat(result.isPresent()).isTrue();
        Assertions.assertThat(result.get()).isEqualTo(AgentWorkerCommandResult.ABORT);
        Assertions.assertThat(objectUnderTest.getCredentialsStatus())
                .isEqualTo(CredentialsStatus.AUTHENTICATION_ERROR);
        Mockito.verify(metricAction).cancelled();
        Mockito.verify(agentLoginEventPublisherService)
                .publishLoginResultEvent(
                        AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult
                                .LOGIN_ERROR_UNKNOWN);
    }

    @Test
    public void shouldHandleAgentPlatformAuthorizationError() {
        // given
        AuthorizationError error = new AuthorizationError();
        AgentPlatformAuthenticationProcessException ex =
                Mockito.mock(AgentPlatformAuthenticationProcessException.class);
        Mockito.when(ex.getSourceAgentPlatformError()).thenReturn(error);
        // when
        Optional<AgentWorkerCommandResult> result = objectUnderTest.handle(ex, metricAction);
        // then
        Assertions.assertThat(result.isPresent()).isTrue();
        Assertions.assertThat(result.get()).isEqualTo(AgentWorkerCommandResult.ABORT);
        Assertions.assertThat(objectUnderTest.getCredentialsStatus())
                .isEqualTo(CredentialsStatus.AUTHENTICATION_ERROR);
        Mockito.verify(metricAction).cancelled();
        Mockito.verify(agentLoginEventPublisherService)
                .publishLoginResultEvent(
                        AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult
                                .AUTHORIZATION_ERROR_UNKNOWN);
    }

    @Test
    public void
            shouldNotHandleForExceptionDifferentThanAgentPlatformAuthenticationProcessException() {
        // when
        Optional<AgentWorkerCommandResult> result =
                objectUnderTest.handle(Mockito.mock(AgentException.class), metricAction);
        // then
        Assertions.assertThat(result.isPresent()).isFalse();
        Mockito.verifyZeroInteractions(metricAction);
        Mockito.verifyZeroInteractions(agentLoginEventPublisherService);
    }
}
