package se.tink.backend.aggregation.workers.metrics;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Provider;
import se.tink.libraries.credentials.service.CredentialsRequestType;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.Timer;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MetricActionTest {
    private static final MetricId ACTION_NAME = MetricId.newId("test_action");

    private AgentWorkerCommandMetricState state;
    private MetricCacheLoader loader;
    private MetricAction action;
    private Credentials credentials;

    @Before
    public void setup() {
        loader = mock(MetricCacheLoader.class);
        state = mockMetricState();
        credentials = mockCredentials();
        action = new MetricAction(state, loader, credentials, ACTION_NAME);
    }

    private AgentWorkerCommandMetricState mockMetricState() {
        Provider provider = mock(Provider.class);
        Credentials credentials = mockCredentials();
        CredentialsRequestType requestType = CredentialsRequestType.UPDATE;

        return new AgentWorkerCommandMetricState(provider, credentials, loader, requestType);
    }

    private Credentials mockCredentials() {
        Credentials credentials = mock(Credentials.class);
        when(credentials.getType()).thenReturn(CredentialsTypes.MOBILE_BANKID);
        when(credentials.getStatus()).thenReturn(CredentialsStatus.UNCHANGED);

        return credentials;
    }

    private void mockNextTimer() {
        Timer.Context timer = mock(Timer.Context.class);
        when(loader.startTimer(ACTION_NAME)).thenReturn(timer);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureInstantiationThrowsException_whenState_isNull() {
        new MetricAction(null, loader, credentials, MetricId.newId("invalid-instantiation"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureInstantiationThrowsException_whenMetricLoader_isNull() {
        new MetricAction(state, null, credentials, MetricId.newId("invalid-instantiation"));
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
        verify(loader).startTimer(MetricId.newId("test_action_duration"));
    }

    @Test
    public void ensureLoadedActionName_andActionOutcome_isUsed_whenMarking() {
        action.completed();
        verify(loader).mark(markerName("completed"));

        action.failed();
        verify(loader).mark(markerName("failed"));

        action.cancelled();
        verify(loader).mark(markerName("cancelled"));
    }

    private MetricId markerName(String name) {
        return ACTION_NAME
                .label("outcome", name);
    }
}
