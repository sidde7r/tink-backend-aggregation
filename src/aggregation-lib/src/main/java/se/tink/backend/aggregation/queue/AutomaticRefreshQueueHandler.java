package se.tink.backend.aggregation.queue;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.cluster.identification.ClusterId;
import se.tink.backend.aggregation.cluster.identification.ClusterInfo;
import se.tink.backend.aggregation.models.RefreshInformation;
import se.tink.backend.aggregation.workers.AgentWorker;
import se.tink.backend.aggregation.workers.AgentWorkerOperationFactory;
import se.tink.backend.aggregation.workers.AgentWorkerRefreshOperationCreatorWrapper;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.queue.sqs.MessageHandler;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class AutomaticRefreshQueueHandler implements MessageHandler {

    private AgentWorker agentWorker;
    private AgentWorkerOperationFactory agentWorkerCommandFactory;
    private Logger logger = LoggerFactory.getLogger(AutomaticRefreshQueueHandler.class);

    @Inject
    public AutomaticRefreshQueueHandler(AgentWorker agentWorker, AgentWorkerOperationFactory agentWorkerOperationFactory) {
        this.agentWorker = agentWorker;
        this.agentWorkerCommandFactory = agentWorkerOperationFactory;
    }

    @Override
    public void handle(byte[] message) {
        RefreshInformation refreshInformation = SerializationUtils.deserializeFromBinary(message, RefreshInformation.class);
        try {
            agentWorker.executeAutomaticRefresh(AgentWorkerRefreshOperationCreatorWrapper.of(
                    agentWorkerCommandFactory,
                    refreshInformation.getRequest(),
                    ClusterInfo.createForAggregationCluster(
                            ClusterId.create(refreshInformation.getName(), refreshInformation.getEnvironment(), refreshInformation.getAggregator()),
                            refreshInformation.getAggregationControllerHost(),
                            refreshInformation.getApiToken(),
                            refreshInformation.getClientCertificate(),
                            refreshInformation.isDisableRequestCompression()
                    )));
        } catch (Exception e) {
            logger.error("Something went wrong with an automatic refresh from sqs.");
        }

    }

}
