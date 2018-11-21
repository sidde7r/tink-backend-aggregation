package se.tink.backend.aggregation.queue;

import com.google.inject.Inject;
import java.util.concurrent.RejectedExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import se.tink.backend.aggregation.cluster.identification.ClientInfo;
import se.tink.backend.aggregation.cluster.identification.ClusterId;
import se.tink.backend.aggregation.cluster.identification.ClusterInfo;
import se.tink.backend.aggregation.queue.models.RefreshInformation;
import se.tink.backend.aggregation.workers.AgentWorker;
import se.tink.backend.aggregation.workers.AgentWorkerOperationFactory;
import se.tink.backend.aggregation.workers.AgentWorkerRefreshOperationCreatorWrapper;
import se.tink.backend.queue.sqs.EncodingHandler;
import se.tink.backend.queue.sqs.QueueMessageAction;
import java.io.IOException;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;

public class AutomaticRefreshQueueHandler implements QueueMessageAction {
    private AgentWorker agentWorker;
    private AgentWorkerOperationFactory agentWorkerCommandFactory;
    private EncodingHandler<RefreshInformation> encodingHandler;
    private static final Logger logger = LoggerFactory.getLogger(AutomaticRefreshQueueHandler.class);
    private final MetricRegistry metricRegistry;
    private final MetricId metricId = MetricId.newId("aggregation_queue_consumes_by_provider");

    @Inject
    public AutomaticRefreshQueueHandler(AgentWorker agentWorker,
            AgentWorkerOperationFactory agentWorkerOperationFactory,
            EncodingHandler encodingHandler,
            MetricRegistry metricRegistry) {
        this.agentWorker = agentWorker;
        this.agentWorkerCommandFactory = agentWorkerOperationFactory;
        this.encodingHandler = encodingHandler;
        this.metricRegistry = metricRegistry;
    }

    @Override
    public void handle(String message) throws IOException, RejectedExecutionException {
        RefreshInformation refreshInformation = encodingHandler.decode(message);
        metricRegistry.meter(metricId.label("provider",
                refreshInformation.getRequest().getProvider().getName())).inc();

        try {
            AgentWorkerRefreshOperationCreatorWrapper agentWorkerRefreshOperationCreatorWrapper = AgentWorkerRefreshOperationCreatorWrapper.of(
                    agentWorkerCommandFactory,
                    refreshInformation.getRequest(),
                    ClientInfo.of(
                            refreshInformation.getClientName(),
                            refreshInformation.getClusterId(),
                            refreshInformation.getAggregatorId()
                    ));

            MDC.setContextMap(refreshInformation.getMDCContext());
            agentWorker.executeAutomaticRefresh(agentWorkerRefreshOperationCreatorWrapper);
        } catch (RejectedExecutionException rejectedExecution) {
            throw rejectedExecution;
        } catch (Exception e) {
            logger.error("Something went wrong with an automatic refresh from sqs.", e);
        }
    }
}
