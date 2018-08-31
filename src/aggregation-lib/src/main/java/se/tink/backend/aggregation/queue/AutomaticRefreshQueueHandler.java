package se.tink.backend.aggregation.queue;

import com.google.inject.Inject;
import java.util.concurrent.RejectedExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import se.tink.backend.aggregation.cluster.identification.ClusterId;
import se.tink.backend.aggregation.cluster.identification.ClusterInfo;
import se.tink.backend.aggregation.models.RefreshInformation;
import se.tink.backend.aggregation.workers.AgentWorker;
import se.tink.backend.aggregation.workers.AgentWorkerOperationFactory;
import se.tink.backend.aggregation.workers.AgentWorkerRefreshOperationCreatorWrapper;
import se.tink.backend.queue.sqs.EncodingHandler;
import se.tink.backend.queue.sqs.QueueMessageAction;
import java.io.IOException;

public class AutomaticRefreshQueueHandler implements QueueMessageAction {
    private AgentWorker agentWorker;
    private AgentWorkerOperationFactory agentWorkerCommandFactory;
    private EncodingHandler<RefreshInformation> encodingHandler;
    private static final Logger logger = LoggerFactory.getLogger(AutomaticRefreshQueueHandler.class);


    @Inject
    public AutomaticRefreshQueueHandler(AgentWorker agentWorker, AgentWorkerOperationFactory agentWorkerOperationFactory, EncodingHandler encodingHandler) {
        this.agentWorker = agentWorker;
        this.agentWorkerCommandFactory = agentWorkerOperationFactory;
        this.encodingHandler = encodingHandler;
    }

    @Override
    public void handle(String message) throws IOException, RejectedExecutionException {
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

            MDC.setContextMap(refreshInformation.getMDCContext());
            agentWorker.executeAutomaticRefresh(agentWorkerRefreshOperationCreatorWrapper);
        } catch (RejectedExecutionException rejectedExecution) {
            throw rejectedExecution;
        } catch (Exception e) {
            logger.error("Something went wrong with an automatic refresh from sqs. \n"
                    + e.getMessage());
        }
    }
}
