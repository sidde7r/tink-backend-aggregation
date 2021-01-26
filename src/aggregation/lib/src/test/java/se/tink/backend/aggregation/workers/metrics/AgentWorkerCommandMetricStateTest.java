package se.tink.backend.aggregation.workers.metrics;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.workers.commands.metrics.MetricsCommand;
import se.tink.backend.aggregation.workers.operation.type.AgentWorkerOperationMetricType;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsRequestType;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.metrics.types.counters.Counter;
import se.tink.libraries.metrics.types.timers.Timer;
import se.tink.libraries.provider.ProviderDto.ProviderTypes;

public class AgentWorkerCommandMetricStateTest {
    private AgentWorkerCommandMetricState metrics;
    private MetricRegistry metricRegistry;
    private Provider provider;
    private Credentials credentials;
    private CredentialsRequestType requestType;

    @Before
    public void setup() {
        MetricsCommand command = mockCommand();

        metricRegistry = mock(MetricRegistry.class);
        when(metricRegistry.timer(any())).thenReturn(mock(Timer.class));
        when(metricRegistry.meter(any())).thenReturn(mock(Counter.class));
        provider = mockProvider();
        credentials = mockCredentials();
        requestType = CredentialsRequestType.UPDATE;

        metrics =
                new AgentWorkerCommandMetricState(
                        provider, credentials, metricRegistry, requestType);
        metrics.init(command);
    }

    private void mockNextTimer() {
        Timer.Context timer = mock(Timer.Context.class);
        when(metricRegistry.timer(any(MetricId.class)).time()).thenReturn(timer);
    }

    private MetricAction buildAction(String action) {
        return metrics.buildAction(new MetricId.MetricLabels().add("action", action));
    }

    private MetricsCommand mockCommand() {
        MetricsCommand command = mock(MetricsCommand.class);
        when(command.getMetricName()).thenReturn("test_command");

        return command;
    }

    private Credentials mockCredentials() {
        Credentials credentials = mock(Credentials.class);
        when(credentials.getType()).thenReturn(CredentialsTypes.MOBILE_BANKID);
        when(credentials.getStatus()).thenReturn(CredentialsStatus.UNCHANGED);
        when(credentials.getMetricTypeName()).thenReturn("mobile_bankid");

        return credentials;
    }

    private Provider mockProvider() {
        Provider provider = mock(Provider.class);
        when(provider.getType()).thenReturn(ProviderTypes.BANK);
        when(provider.getName()).thenReturn("test_provider");
        when(provider.getMetricTypeName()).thenReturn("bank");

        return provider;
    }

    @Test(expected = IllegalStateException.class)
    public void ensureExceptionIsThrown_whenMetricsState_notInitiatedByCommand_onStart() {
        metrics =
                new AgentWorkerCommandMetricState(
                        provider, credentials, metricRegistry, requestType);
        metrics.start(AgentWorkerOperationMetricType.EXECUTE_COMMAND);
    }

    @Test(expected = IllegalStateException.class)
    public void ensureExceptionIsThrown_whenMetricsState_startedTwice() {
        metrics.start(AgentWorkerOperationMetricType.EXECUTE_COMMAND);
        metrics.start(AgentWorkerOperationMetricType.EXECUTE_COMMAND);
    }

    @Test(expected = IllegalStateException.class)
    public void ensureExceptionIsThrown_whenMetricsState_notInitiatedByCommand_onGetAction() {
        metrics =
                new AgentWorkerCommandMetricState(
                        provider, credentials, metricRegistry, requestType);
        buildAction("should-throw-exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureExceptionIsThrown_whenType_isNull_onStart() {
        metrics.start(null);
    }

    @Test(expected = IllegalStateException.class)
    public void ensureExceptionIsThrown_whenNonOngoingBaseAction_isStopped() {
        metrics.stop();
    }

    @Test(expected = IllegalStateException.class)
    public void ensureExceptionIsThrown_whenOngoingActionsRemaining_onStop() {
        mockNextTimer();
        metrics.start(AgentWorkerOperationMetricType.EXECUTE_COMMAND);

        mockNextTimer();
        MetricAction action = buildAction("test_action");
        action.start();

        metrics.stop();
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureExceptionIsThrown_whenTryingToAddAction_thatIsNull() {
        metrics.add(null);
    }

    @Test(expected = IllegalStateException.class)
    public void
            ensureExceptionIsThrown_whenTryingToAddAction_alreadyPresentInListOfOngoingActions() {
        MetricAction action = mock(MetricAction.class);
        metrics.add(action);
        metrics.add(action);
    }

    @Test
    public void ensureActionsDoesNotNeedToBeClosed_ifActionTimerIsNotStarted() {
        mockNextTimer();
        metrics.start(AgentWorkerOperationMetricType.EXECUTE_COMMAND);

        mockNextTimer();
        MetricAction action = buildAction("test_action");
        action.completed();

        metrics.stop();
    }

    @Test
    public void ensureNoExceptionsAreThrown_whenEverythingClosedProperly() {
        mockNextTimer();
        metrics.start(AgentWorkerOperationMetricType.EXECUTE_COMMAND);

        mockNextTimer();
        MetricAction action = buildAction("test_action1");
        action.start();
        action.completed();
        action.stop();

        mockNextTimer();
        action = buildAction("test_action2");
        action.start();
        action.completed();
        action.stop();

        metrics.stop();
    }

    @Test
    public void ensureItsOk_toOverlapActions() {
        mockNextTimer();
        metrics.start(AgentWorkerOperationMetricType.EXECUTE_COMMAND);

        MetricAction action1 = buildAction("test_action1");
        MetricAction action2 = buildAction("test_action2");
        MetricAction action3 = buildAction("test_action3");

        mockNextTimer();
        action1.start();

        mockNextTimer();
        action2.start();

        action1.stop();

        mockNextTimer();
        action3.start();
        action3.stop();

        action2.stop();

        metrics.stop();
    }
}
