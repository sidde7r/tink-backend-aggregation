package se.tink.backend.aggregation.queue;

import com.google.inject.Inject;
import java.util.concurrent.RejectedExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import se.tink.backend.aggregation.cluster.identification.ClientInfo;
import se.tink.backend.aggregation.queue.models.RefreshInformation;
import se.tink.backend.aggregation.storage.database.models.ClientConfiguration;
import se.tink.backend.aggregation.storage.database.providers.ClientConfigurationProvider;
import se.tink.backend.aggregation.workers.AgentWorker;
import se.tink.backend.aggregation.workers.AgentWorkerOperationFactory;
import se.tink.backend.aggregation.workers.AgentWorkerRefreshOperationCreatorWrapper;
import se.tink.libraries.queue.sqs.EncodingHandler;
import se.tink.libraries.queue.sqs.QueueMessageAction;
import java.io.IOException;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;

public class AutomaticRefreshQueueHandler implements QueueMessageAction {
    private AgentWorker agentWorker;
    private AgentWorkerOperationFactory agentWorkerCommandFactory;
    private EncodingHandler<RefreshInformation> encodingHandler;
    private static final Logger logger = LoggerFactory.getLogger(AutomaticRefreshQueueHandler.class);
    private final MetricRegistry metricRegistry;
    private ClientConfigurationProvider clientConfigurationProvider;
    private final MetricId metricId = MetricId.newId("aggregation_queue_consumes_by_provider");

    @Inject
    public AutomaticRefreshQueueHandler(AgentWorker agentWorker,
            AgentWorkerOperationFactory agentWorkerOperationFactory,
            EncodingHandler encodingHandler,
            MetricRegistry metricRegistry,
            ClientConfigurationProvider clientConfigurationProvider) {
        this.agentWorker = agentWorker;
        this.agentWorkerCommandFactory = agentWorkerOperationFactory;
        this.encodingHandler = encodingHandler;
        this.metricRegistry = metricRegistry;
        this.clientConfigurationProvider = clientConfigurationProvider;
    }

    @Override
    public void handle(String message) throws IOException, RejectedExecutionException {
        RefreshInformation refreshInformation = encodingHandler.decode(message);
        metricRegistry.meter(metricId.label("provider",
                refreshInformation.getRequest().getProvider().getName())).inc();

        try {
        ClientInfo clientInfo = null;
        if (refreshInformation.getClientName() != null) {
            clientInfo = ClientInfo.of(
                    refreshInformation.getClientName(),
                    refreshInformation.getClusterId(),
                    refreshInformation.getAggregatorId()
            );
        } else {
            ClientConfiguration configuration = clientConfigurationProvider.getClientConfiguration(refreshInformation.getName(), refreshInformation.getEnvironment());
            clientInfo = ClientInfo.of(
                    configuration.getClientName(),
                    configuration.getClusterId(),
                    configuration.getAggregatorId()
            );
        }
            AgentWorkerRefreshOperationCreatorWrapper agentWorkerRefreshOperationCreatorWrapper = AgentWorkerRefreshOperationCreatorWrapper.of(
                    agentWorkerCommandFactory,
                    refreshInformation.getRequest(),
                    clientInfo
                    );

            MDC.setContextMap(refreshInformation.getMDCContext());
            agentWorker.executeAutomaticRefresh(agentWorkerRefreshOperationCreatorWrapper);
        } catch (RejectedExecutionException rejectedExecution) {
            throw rejectedExecution;
        } catch (Exception e) {
            logger.error("Something went wrong with an automatic refresh from sqs.", e);
        }
    }
}
