package se.tink.backend.aggregation.workers.commands.login;

import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.metrics.MetricAction;
import se.tink.backend.aggregation.workers.metrics.MetricActionIface;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;

public class DefaultLoginExceptionHandlerTest {

    private DefaultLoginExceptionHandler objectUnderTest;
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
                new DefaultLoginExceptionHandler(
                        statusUpdater, context, agentLoginEventPublisherService);
    }

    @Test
    public void shouldReturnAbortResult() {
        // given
        Exception ex = Mockito.mock(Exception.class);
        // when
        Optional<AgentWorkerCommandResult> result = objectUnderTest.handle(ex, metricAction);
        // then
        Assertions.assertThat(result.isPresent()).isTrue();
        Assertions.assertThat(result.get()).isEqualTo(AgentWorkerCommandResult.ABORT);
        Mockito.verify(metricAction).failed();
        Mockito.verify(agentLoginEventPublisherService).publishLoginErrorUnknown();
    }
}
