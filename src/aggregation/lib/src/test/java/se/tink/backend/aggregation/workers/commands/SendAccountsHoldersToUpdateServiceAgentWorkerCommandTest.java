package se.tink.backend.aggregation.workers.commands;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.metrics.AgentWorkerCommandMetricState;
import se.tink.backend.aggregation.workers.metrics.MetricAction;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.metrics.core.MetricId;

public class SendAccountsHoldersToUpdateServiceAgentWorkerCommandTest {

    private AgentWorkerCommandContext context;
    private AgentWorkerCommandMetricState metrics;
    private MetricAction metricAction;
    private SendAccountsHoldersToUpdateServiceAgentWorkerCommand command;

    @Before
    public void setUp() {
        context = Mockito.mock(AgentWorkerCommandContext.class);
        metrics = Mockito.mock(AgentWorkerCommandMetricState.class);

        metrics = Mockito.mock(AgentWorkerCommandMetricState.class);
        Mockito.when(metrics.init(Mockito.any())).thenReturn(metrics);

        metricAction = Mockito.mock(MetricAction.class);
        Mockito.when(
                        metrics.buildAction(
                                Mockito.eq(
                                        new MetricId.MetricLabels()
                                                .add(
                                                        "action",
                                                        SendAccountsHoldersToUpdateServiceAgentWorkerCommand
                                                                .METRIC_ACTION))))
                .thenReturn(metricAction);

        command = new SendAccountsHoldersToUpdateServiceAgentWorkerCommand(context, metrics);
    }

    @Test
    public void commandReturnContinueWhenContextThrowException() throws Exception {
        // given
        Mockito.doThrow(RuntimeException.class)
                .when(context)
                .sendAllCachedAccountsHoldersToUpdateService();
        // when
        AgentWorkerCommandResult result = command.execute();

        // then
        Assert.assertEquals(AgentWorkerCommandResult.CONTINUE, result);
        Mockito.verify(metricAction, Mockito.times(1)).failed();
    }

    @Test
    public void commandCompletedWhenContextSuccessful() throws Exception {
        // when
        AgentWorkerCommandResult result = command.execute();

        // then
        Assert.assertEquals(AgentWorkerCommandResult.CONTINUE, result);
        Mockito.verify(metricAction, Mockito.times(1)).completed();
    }
}
