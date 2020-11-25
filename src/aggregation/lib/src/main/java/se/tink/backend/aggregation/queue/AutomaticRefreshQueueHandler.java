package se.tink.backend.aggregation.queue;

import com.google.inject.Inject;
import java.io.IOException;
import java.util.concurrent.RejectedExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import se.tink.backend.aggregation.cluster.identification.ClientInfo;
import se.tink.backend.aggregation.queue.models.RefreshInformation;
import se.tink.backend.aggregation.storage.database.models.ClientConfiguration;
import se.tink.backend.aggregation.storage.database.providers.ClientConfigurationProvider;
import se.tink.backend.aggregation.workers.worker.AgentWorker;
import se.tink.backend.aggregation.workers.worker.AgentWorkerOperationFactory;
import se.tink.backend.aggregation.workers.worker.AgentWorkerRefreshOperationCreatorWrapper;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.queue.sqs.EncodingHandler;
import se.tink.libraries.queue.sqs.QueueMessageAction;
import se.tink.libraries.rate_limit_service.RateLimitService;

public class AutomaticRefreshQueueHandler implements QueueMessageAction {
    private AgentWorker agentWorker;
    private AgentWorkerOperationFactory agentWorkerCommandFactory;
    private EncodingHandler<RefreshInformation> encodingHandler;
    private static final Logger logger =
            LoggerFactory.getLogger(AutomaticRefreshQueueHandler.class);
    private final MetricRegistry metricRegistry;
    private ClientConfigurationProvider clientConfigurationProvider;
    private final MetricId metricId = MetricId.newId("aggregation_queue_consumes_by_provider");

    @Inject
    public AutomaticRefreshQueueHandler(
            AgentWorker agentWorker,
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
        String providerName = refreshInformation.getRequest().getProvider().getName();
        if (RateLimitService.INSTANCE.hasReceivedRateLimitNotificationRecently(providerName)) {
            throw new RejectedExecutionException(
                    String.format(
                            "Provider %s was rate limited recently. Rejecting execution to requeue.",
                            providerName));
        }
        metricRegistry
                .meter(
                        metricId.label(
                                "provider",
                                refreshInformation.getRequest().getProvider().getName()))
                .inc();

        try {
            ClientInfo clientInfo = null;
            if (refreshInformation.getClientName() != null) {
                clientInfo =
                        ClientInfo.of(
                                refreshInformation.getClientName(),
                                refreshInformation.getClusterId(),
                                refreshInformation.getAggregatorId(),
                                refreshInformation.getAppId());
            } else {
                /**
                 * Below logger is to verify if this else condition is ever met. Eventually will
                 * remove else condition if its not in use.
                 */
                logger.info(
                        "ClientName : {}, AppId : {}",
                        refreshInformation.getClientName(),
                        refreshInformation.getAppId());
                ClientConfiguration configuration =
                        clientConfigurationProvider.getClientConfiguration(
                                refreshInformation.getName(), refreshInformation.getEnvironment());

                /* passing appId as null as we don't store appId in data base entity ClientConfiguration */
                clientInfo =
                        ClientInfo.of(
                                configuration.getClientName(),
                                configuration.getClusterId(),
                                configuration.getAggregatorId(),
                                null);
            }
            AgentWorkerRefreshOperationCreatorWrapper agentWorkerRefreshOperationCreatorWrapper =
                    AgentWorkerRefreshOperationCreatorWrapper.of(
                            agentWorkerCommandFactory, refreshInformation.getRequest(), clientInfo);

            MDC.setContextMap(refreshInformation.getMDCContext());
            agentWorker.executeAutomaticRefresh(agentWorkerRefreshOperationCreatorWrapper);
        } catch (RejectedExecutionException rejectedExecution) {
            throw rejectedExecution;
        } catch (Exception e) {
            logger.error("Something went wrong with an automatic refresh from sqs.", e);
        } finally {
            MDC.clear();
        }
    }
}
