package se.tink.backend.aggregation.workers.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import se.tink.backend.aggregation.workers.concurrency.InterProcessSemaphoreMutexFactoryStub;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.metrics.collection.MetricCollector;
import se.tink.libraries.metrics.registry.MetricRegistry;

public class LockAgentWorkerCommandTest {

    private LockAgentWorkerCommand command;
    private AgentWorkerCommandContext context;
    private InterProcessSemaphoreMutexFactoryStub lock;

    @Before
    public void setUp() {
        context = mock(AgentWorkerCommandContext.class, Answers.RETURNS_DEEP_STUBS);
        when(context.getMetricRegistry()).thenReturn(new MetricRegistry(new MetricCollector()));

        lock = new InterProcessSemaphoreMutexFactoryStub();
        command = new LockAgentWorkerCommand(context, "lock-test", lock);
    }

    @Test
    public void doExecuteShouldContinue() throws Exception {
        // when
        AgentWorkerCommandResult result = command.doExecute();

        // then
        assertThat(result).isEqualTo(AgentWorkerCommandResult.CONTINUE);

        command.doPostProcess();
    }
}
