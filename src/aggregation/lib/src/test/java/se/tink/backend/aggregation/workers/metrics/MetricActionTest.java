package se.tink.backend.aggregation.workers.metrics;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsRequestType;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.metrics.types.counters.Counter;
import se.tink.libraries.metrics.types.timers.Timer;

public class MetricActionTest {
    private static final MetricId ACTION_NAME = MetricId.newId("test_action");

    private AgentWorkerCommandMetricState state;
    private Timer timerMock;
    private Counter counterMock;
    private MetricRegistry metricRegistry;
    private MetricAction action;

    @Before
    public void setup() {
        metricRegistry = mock(MetricRegistry.class);
        timerMock = mock(Timer.class);
        when(metricRegistry.timer(any())).thenReturn(timerMock);
        counterMock = mock(Counter.class);
        when(metricRegistry.meter(any())).thenReturn(counterMock);
        state = mockMetricState();
        action = new MetricAction(state, metricRegistry, ACTION_NAME);
    }

    private AgentWorkerCommandMetricState mockMetricState() {
        Provider provider = mock(Provider.class);
        Credentials credentials = mockCredentials();
        CredentialsRequestType requestType = CredentialsRequestType.UPDATE;

        return new AgentWorkerCommandMetricState(
                provider, credentials, metricRegistry, requestType);
    }

    private Credentials mockCredentials() {
        Credentials credentials = mock(Credentials.class);
        when(credentials.getType()).thenReturn(CredentialsTypes.MOBILE_BANKID);
        when(credentials.getStatus()).thenReturn(CredentialsStatus.UNCHANGED);

        return credentials;
    }

    private void mockNextTimer() {
        Timer.Context timer = mock(Timer.Context.class);
        when(metricRegistry.timer(ACTION_NAME).time()).thenReturn(timer);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureInstantiationThrowsException_whenState_isNull() {
        new MetricAction(null, metricRegistry, MetricId.newId("invalid-instantiation"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureInstantiationThrowsException_whenMetricLoader_isNull() {
        new MetricAction(state, null, MetricId.newId("invalid-instantiation"));
    }

    @Test(expected = IllegalStateException.class)
    public void ensureExceptionIsThrown_whenStarting_action_alreadyInProgress() {
        mockNextTimer();

        action.start();
        action.start();
    }

    @Test(expected = IllegalStateException.class)
    public void ensureExceptionIsThrown_whenStopping_action_notInProgress() {
        action.stop();
    }

    @Test
    public void ensureLoadedActionName_isUsed_whenStartingTimer() {
        action.start();
        verify(metricRegistry).timer(MetricId.newId("test_action_duration"));
        verify(timerMock).time();
    }

    @Test
    public void ensureLoadedActionName_andActionOutcome_isUsed_whenMarking() {
        action.completed();
        verify(metricRegistry).meter(markerName("completed"));
        verify(counterMock).inc();
        reset(counterMock);

        action.failed();
        verify(metricRegistry).meter(markerName("failed"));
        verify(counterMock).inc();
        reset(counterMock);

        action.cancelled();
        verify(metricRegistry).meter(markerName("cancelled"));
        verify(counterMock).inc();
    }

    private MetricId markerName(String name) {
        return ACTION_NAME.label("outcome", name);
    }
}
