package se.tink.backend.aggregation.resources.dispatcher;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.cluster.identification.ClientInfo;
import se.tink.backend.aggregation.workers.worker.AgentWorker;
import se.tink.backend.aggregation.workers.worker.AgentWorkerOperationFactory;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.credentials.service.UserAvailability;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.metrics.types.counters.Counter;
import se.tink.libraries.queue.QueueProducer;

@RunWith(MockitoJUnitRunner.class)
public class RefreshRequestDispatcherTest {
    private QueueProducer regularQueueProducer;
    private QueueProducer priorityQueueProducer = mock(QueueProducer.class);
    private AgentWorker agentWorker = mock(AgentWorker.class);
    private AgentWorkerOperationFactory agentWorkerCommandFactory =
            mock(AgentWorkerOperationFactory.class);
    private MetricRegistry metricRegistry;
    private ClientInfo clientInfo;

    @Before
    public void init() {
        metricRegistry = mock(MetricRegistry.class);
        when(metricRegistry.meter(any())).thenReturn(new Counter());
        regularQueueProducer = mock(QueueProducer.class);
        clientInfo = ClientInfo.of("clientName", "clusterId", "aggregatorId", "someIp");
    }

    @Test
    public void shouldPerformRefreshImmediatelyIfUserPresent() throws Exception {
        // given
        RefreshRequestDispatcher dispatcher =
                new RefreshRequestDispatcher(
                        regularQueueProducer,
                        priorityQueueProducer,
                        agentWorker,
                        agentWorkerCommandFactory,
                        metricRegistry);
        RefreshInformationRequest request = createRefreshInformationRequest(true);

        // when
        dispatcher.dispatchRefreshInformation(request, clientInfo);

        // then
        verify(agentWorker, times(1)).execute(any());
        verify(agentWorker, never()).executeAutomaticRefresh(any());
        verify(regularQueueProducer, never()).send(any());
        verify(priorityQueueProducer, never()).send(any());
    }

    @Test
    public void shouldUseRegularQueueForRefreshIfQueueAvailableAndUserAbsent() throws Exception {
        // given
        RefreshRequestDispatcher dispatcher =
                new RefreshRequestDispatcher(
                        regularQueueProducer,
                        priorityQueueProducer,
                        agentWorker,
                        agentWorkerCommandFactory,
                        metricRegistry);
        RefreshInformationRequest request = createRefreshInformationRequest(false);
        when(regularQueueProducer.isAvailable()).thenReturn(true);

        // when
        dispatcher.dispatchRefreshInformation(request, clientInfo);

        // then
        verify(agentWorker, never()).execute(any());
        verify(agentWorker, never()).executeAutomaticRefresh(any());
        verify(regularQueueProducer, times(1)).send(any());
        verify(priorityQueueProducer, never()).send(any());
    }

    @Test
    public void shouldUsePriorityQueueForRefreshIfQueueAvailableAndUserAbsent() throws Exception {
        // given
        RefreshRequestDispatcher dispatcher =
                new RefreshRequestDispatcher(
                        regularQueueProducer,
                        priorityQueueProducer,
                        agentWorker,
                        agentWorkerCommandFactory,
                        metricRegistry);
        when(priorityQueueProducer.isAvailable()).thenReturn(true);
        RefreshInformationRequest request = createRefreshInformationRequest(false);
        request.setRefreshPriority(10);

        // when
        dispatcher.dispatchRefreshInformation(request, clientInfo);

        // then
        verify(regularQueueProducer, never()).send(any());
        verify(priorityQueueProducer, times(1)).send(any());
    }

    @Test
    public void shouldPerformAutomaticRefreshIfQueueIsUnavailableAndUserAbsent() throws Exception {
        // given
        RefreshRequestDispatcher dispatcher =
                new RefreshRequestDispatcher(
                        regularQueueProducer,
                        priorityQueueProducer,
                        agentWorker,
                        agentWorkerCommandFactory,
                        metricRegistry);
        RefreshInformationRequest request = createRefreshInformationRequest(false);
        when(regularQueueProducer.isAvailable()).thenReturn(false);

        // when
        dispatcher.dispatchRefreshInformation(request, clientInfo);

        // then
        verify(agentWorker, never()).execute(any());
        verify(agentWorker, times(1)).executeAutomaticRefresh(any());
        verify(regularQueueProducer, never()).send(any());
        verify(priorityQueueProducer, never()).send(any());
    }

    private RefreshInformationRequest createRefreshInformationRequest(boolean userAvailable) {
        RefreshInformationRequest request = new RefreshInformationRequest();
        UserAvailability userAvailability = new UserAvailability();
        userAvailability.setUserPresent(userAvailable);
        request.setUserAvailability(userAvailability);
        request.setCredentials(new Credentials());
        return request;
    }
}
