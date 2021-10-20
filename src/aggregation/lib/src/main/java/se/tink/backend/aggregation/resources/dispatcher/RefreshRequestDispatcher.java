package se.tink.backend.aggregation.resources.dispatcher;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.cluster.identification.ClientInfo;
import se.tink.backend.aggregation.queue.models.RefreshInformation;
import se.tink.backend.aggregation.workers.worker.AgentWorker;
import se.tink.backend.aggregation.workers.worker.AgentWorkerOperationFactory;
import se.tink.backend.aggregation.workers.worker.AgentWorkerRefreshOperationCreatorWrapper;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.queue.QueueProducer;

@Slf4j
public class RefreshRequestDispatcher {
    private static final MetricId USER_AVAILABILITY_VALUES =
            MetricId.newId("aggregation_user_availability_values");

    private static final Set<String> priorityAppIdsForTest =
            ImmutableSet.of(
                    "a68dd285648141f19a4268f0cd508f0c", // Piotr stg app
                    "ef2d8c482ad54ec99811ec79f7207e66", // Piotr prod app
                    "3c759efb06d04530bc365a338e4a0e7f" // Xero production
                    );

    private final QueueProducer regularQueueProducer;
    private final QueueProducer priorityQueueProducer;
    private final AgentWorker agentWorker;
    private final AgentWorkerOperationFactory agentWorkerCommandFactory;
    private final MetricRegistry metricRegistry;

    @Inject
    public RefreshRequestDispatcher(
            @Named("regularQueueProducer") QueueProducer regularQueueProducer,
            @Named("priorityQueueProducer") QueueProducer priorityQueueProducer,
            AgentWorker agentWorker,
            AgentWorkerOperationFactory agentWorkerCommandFactory,
            MetricRegistry metricRegistry) {
        this.regularQueueProducer = regularQueueProducer;
        this.priorityQueueProducer = priorityQueueProducer;
        this.agentWorker = agentWorker;
        this.agentWorkerCommandFactory = agentWorkerCommandFactory;
        this.metricRegistry = metricRegistry;
    }

    public void dispatchRefreshInformation(
            final RefreshInformationRequest request, final ClientInfo clientInfo) throws Exception {
        if (isHighPrioRequest(request)) {
            agentWorker.execute(
                    agentWorkerCommandFactory.createOperationRefresh(request, clientInfo));
        } else {
            QueueProducer queueProducer = getQueueProducer(request, clientInfo);
            if (queueProducer.isAvailable()) {
                queueProducer.send(new RefreshInformation(request, clientInfo));
            } else {
                log.info(
                        "Handling bg refresh request directly due to queue producer being unavailable, credentialId: {}",
                        request.getCredentials().getId());
                agentWorker.executeAutomaticRefresh(
                        AgentWorkerRefreshOperationCreatorWrapper.of(
                                agentWorkerCommandFactory, request, clientInfo));
            }
        }
    }

    private QueueProducer getQueueProducer(
            final RefreshInformationRequest request, final ClientInfo clientInfo) {
        if (clientInfo != null && priorityAppIdsForTest.contains(clientInfo.getAppId())) {
            log.info(
                    "Selecting priority queue for refreshId: {}, credentialsId: {}, appId: {}",
                    request.getRefreshId(),
                    request.getCredentials().getId(),
                    clientInfo.getAppId());
            return priorityQueueProducer;
        }
        return regularQueueProducer;
    }

    private boolean isHighPrioRequest(CredentialsRequest request) {
        // The UserAvailability object and data that follows is the new (2021-03-15) way of taking
        // decisions on Aggregation Service in relation to the User. For instance, we want to
        // high-prioritize all requests where the user is present.
        if (request.getUserAvailability() != null) {
            metricRegistry
                    .meter(
                            USER_AVAILABILITY_VALUES
                                    // redundancy was left for backward compatibility
                                    .label("manual", request.isUserPresent())
                                    .label("present", request.getUserAvailability().isUserPresent())
                                    .label(
                                            "available_for_interaction",
                                            request.getUserAvailability()
                                                    .isUserAvailableForInteraction()))
                    .inc();
        }
        return request.isUserPresent();
    }
}
