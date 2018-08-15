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
import se.tink.backend.queue.sqs.EncodingHandler;
import se.tink.backend.queue.sqs.MessageHandler;

import java.io.IOException;

public class AutomaticRefreshQueueHandler implements MessageHandler {

    private AgentWorker agentWorker;
    private AgentWorkerOperationFactory agentWorkerCommandFactory;
    private EncodingHandler<RefreshInformation> encodingHandler;
    private Logger logger = LoggerFactory.getLogger(AutomaticRefreshQueueHandler.class);

    @Inject
    public AutomaticRefreshQueueHandler(AgentWorker agentWorker, AgentWorkerOperationFactory agentWorkerOperationFactory, EncodingHandler encodingHandler) {
        this.agentWorker = agentWorker;
        this.agentWorkerCommandFactory = agentWorkerOperationFactory;
        this.encodingHandler = encodingHandler;
    }

    //boolean
    @Override
    public AgentWorkerRefreshOperationCreatorWrapper handle(String message) throws IOException {
        RefreshInformation refreshInformation = encodingHandler.decode(message);
        try {
            AgentWorkerRefreshOperationCreatorWrapper agentWorkerRefreshOperationCreatorWrapper = AgentWorkerRefreshOperationCreatorWrapper.of(
                    agentWorkerCommandFactory,
                    refreshInformation.getRequest(),
                    ClusterInfo.createForAggregationCluster(
                            ClusterId.create(refreshInformation.getName(), refreshInformation.getEnvironment(),
                                    refreshInformation.getAggregator()),
                            refreshInformation.getAggregationControllerHost(),
                            refreshInformation.getApiToken(),
                            refreshInformation.getClientCertificate(),
                            refreshInformation.isDisableRequestCompression()
                    ));
            agentWorker.executeAutomaticRefresh(agentWorkerRefreshOperationCreatorWrapper);

            return agentWorkerRefreshOperationCreatorWrapper;
        } catch (Exception e) {
            logger.error("Something went wrong with an automatic refresh from sqs.");
            throw new IOException(e);
        }
    }

}
