package se.tink.backend.aggregation.workers.commands.login;

import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.metrics.MetricAction;
import se.tink.backend.aggregation.workers.metrics.MetricActionIface;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;

public class AuthorizationLoginExceptionHandlerTest {

    private AuthorizationLoginExceptionHandler objectUnderTest;
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
                new AuthorizationLoginExceptionHandler(
                        statusUpdater, context, agentLoginEventPublisherService);
    }

    @Test
    public void whenExceptionIsNotAnAuthenticationShouldReturnEmptyResult() {
        // given
        Exception ex = Mockito.mock(BankServiceException.class);
        // when
        Optional<AgentWorkerCommandResult> result = objectUnderTest.handle(ex, metricAction);
        // then
        Assertions.assertThat(result.isPresent()).isFalse();
        Mockito.verifyZeroInteractions(agentLoginEventPublisherService);
        Mockito.verifyZeroInteractions(metricAction);
    }

    @Test
    public void whenExceptionIsAuthenticationShouldReturnAbortStatus() {
        // given
        AuthorizationException ex = Mockito.mock(AuthorizationException.class);
        // when
        Optional<AgentWorkerCommandResult> result = objectUnderTest.handle(ex, metricAction);
        // then
        Assertions.assertThat(result.isPresent()).isTrue();
        Assertions.assertThat(result.get()).isEqualTo(AgentWorkerCommandResult.ABORT);
        Mockito.verify(metricAction).cancelled();
        Mockito.verify(agentLoginEventPublisherService).publishLoginAuthorizationErrorEvent(ex);
    }
}
