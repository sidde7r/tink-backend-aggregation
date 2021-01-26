package se.tink.backend.aggregation.workers.operation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.cache.LoadingCache;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import org.junit.Test;
import org.mockito.InOrder;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.workers.operation.AgentWorkerOperation.AgentWorkerOperationState;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.metrics.core.MetricBuckets;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.types.timers.Timer;

public final class AgentWorkerOperationTest {

    @Test
    public void whenRunWithNoCommandsContextStartAndStopAreInvoked() throws Exception {
        // given
        AgentWorkerContext context = mock(AgentWorkerContext.class);
        AgentWorkerOperation operation =
                new AgentWorkerOperation(
                        mock(AgentWorkerOperationState.class),
                        "myOperationMetricName",
                        mock(CredentialsRequest.class),
                        Collections.emptyList(),
                        context);

        // when
        operation.run();

        // then
        verify(context, times(1)).start();
        verify(context, times(1)).stop();
    }

    @Test
    public void whenRunWithNoopCommandCredentialsStatusStaysTheSame() throws ExecutionException {
        // given
        CredentialsRequest credentialsRequest = mock(CredentialsRequest.class);
        Credentials credentials = new Credentials();
        final CredentialsStatus SAMPLE_STATUS = CredentialsStatus.UPDATING;
        credentials.setStatus(SAMPLE_STATUS);
        AgentWorkerCommand command = mock(AgentWorkerCommand.class);
        when(credentialsRequest.getCredentials()).thenReturn(credentials);
        AgentWorkerOperationState state = mock(AgentWorkerOperationState.class);
        LoadingCache<MetricId.MetricLabels, Timer> loadingCache = mock(LoadingCache.class);
        doReturn(loadingCache).when(state).getCommandExecutionsTimers();
        Timer timer = new Timer(new MetricBuckets(Collections.emptyList()));
        when(loadingCache.get(any())).thenReturn(timer);
        AgentWorkerOperation operation =
                new AgentWorkerOperation(
                        state,
                        "myOperationMetricName",
                        credentialsRequest,
                        Collections.singletonList(command),
                        mock(AgentWorkerContext.class));

        // when
        operation.run();

        // then
        assertThat(credentials.getStatus()).isEqualTo(SAMPLE_STATUS);
    }

    @Test
    public void whenRunWithCommandFailingUponExecutionCredentialsStatusBecomesTemporaryError()
            throws Exception {
        // given
        CredentialsRequest credentialsRequest = mock(CredentialsRequest.class);
        Credentials credentials = new Credentials();
        credentials.setStatus(CredentialsStatus.UPDATING);
        AgentWorkerCommand command = mock(AgentWorkerCommand.class);
        when(credentialsRequest.getCredentials()).thenReturn(credentials);
        when(command.execute()).thenThrow(new IllegalStateException("sample exception"));
        AgentWorkerOperationState state = mock(AgentWorkerOperationState.class);
        LoadingCache<MetricId.MetricLabels, Timer> loadingCache = mock(LoadingCache.class);
        doReturn(loadingCache).when(state).getCommandExecutionsTimers();
        Timer timer = new Timer(new MetricBuckets(Collections.emptyList()));
        when(loadingCache.get(any())).thenReturn(timer);
        AgentWorkerOperation operation =
                new AgentWorkerOperation(
                        state,
                        "myOperationMetricName",
                        credentialsRequest,
                        Collections.singletonList(command),
                        mock(AgentWorkerContext.class));

        // when
        operation.run();

        // then
        assertThat(credentials.getStatus()).isEqualTo(CredentialsStatus.TEMPORARY_ERROR);
    }

    @Test
    public void whenRunWithTwoCommandsTheFirstCommandIsExecutedBeforeTheSecond() throws Exception {
        // given
        CredentialsRequest credentialsRequest = mock(CredentialsRequest.class);
        Credentials credentials = new Credentials();
        AgentWorkerCommand command1 = mock(AgentWorkerCommand.class);
        AgentWorkerCommand command2 = mock(AgentWorkerCommand.class);
        when(credentialsRequest.getCredentials()).thenReturn(credentials);
        AgentWorkerOperationState state = mock(AgentWorkerOperationState.class);
        LoadingCache<MetricId.MetricLabels, Timer> loadingCache = mock(LoadingCache.class);
        doReturn(loadingCache).when(state).getCommandExecutionsTimers();
        Timer timer = new Timer(new MetricBuckets(Collections.emptyList()));
        when(loadingCache.get(any())).thenReturn(timer);
        AgentWorkerOperation operation =
                new AgentWorkerOperation(
                        state,
                        "myOperationMetricName",
                        credentialsRequest,
                        Arrays.asList(command1, command2),
                        mock(AgentWorkerContext.class));
        InOrder inOrder = inOrder(command1, command2);

        // when
        operation.run();

        // then
        inOrder.verify(command1).execute();
        inOrder.verify(command2).execute();
    }
}
