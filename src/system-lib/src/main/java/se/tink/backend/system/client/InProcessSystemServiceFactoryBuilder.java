package se.tink.backend.system.client;

import com.google.common.base.Preconditions;
import com.google.inject.Provider;
import io.dropwizard.setup.Environment;
import java.util.Optional;

import se.tink.backend.categorization.client.FastTextServiceFactory;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.search.client.ElasticSearchClient;
import se.tink.backend.common.tasks.kafka.KafkaQueueResetter;
import se.tink.backend.core.ClusterCategories;
import se.tink.backend.firehose.v1.queue.FirehoseQueueProducer;
import se.tink.backend.insights.client.InsightsServiceFactory;
import se.tink.backend.system.LeaderCandidate;
import se.tink.backend.system.cronjob.CronJobManager;
import se.tink.backend.system.resources.CronServiceResource;
import se.tink.backend.system.resources.ProcessServiceResource;
import se.tink.backend.system.resources.UpdateServiceResource;
import se.tink.backend.system.workers.processor.chaining.DefaultUserChainFactoryCreator;
import se.tink.backend.system.workers.processor.formatting.MarketDescriptionExtractorFactory;
import se.tink.backend.system.workers.processor.formatting.MarketDescriptionFormatterFactory;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.metrics.MetricRegistry;

public class InProcessSystemServiceFactoryBuilder {
    private final Optional<LeaderCandidate> leaderCandidate;
    private final CronServiceResource cronService;
    private final Optional<Environment> environment;
    private final ServiceContext serviceContext;
    private final InsightsServiceFactory insightsServiceFactory;

    private static final LogUtils log = new LogUtils(InProcessSystemServiceFactoryBuilder.class);
    private MetricRegistry metricRegistry;

    public InProcessSystemServiceFactoryBuilder(ServiceContext serviceContext, InsightsServiceFactory insightsServiceFactory, Optional<Environment> environment,
            Optional<LeaderCandidate> leaderCandidate, CronServiceResource cronService,
            MetricRegistry metricRegistry) {

        this.serviceContext = serviceContext;
        this.insightsServiceFactory = insightsServiceFactory;
        this.environment = environment;
        this.leaderCandidate = leaderCandidate;
        this.cronService = cronService;
        this.metricRegistry = metricRegistry;
    }

    public InProcessSystemServiceFactory buildAndRegister(KafkaQueueResetter queueResetter,
            FirehoseQueueProducer firehoseQueueProducer,
            Provider<MarketDescriptionFormatterFactory> descriptionFormatterFactory,
            Provider<MarketDescriptionExtractorFactory> descriptionExtractorFactory,
            ClusterCategories categories, FastTextServiceFactory fastTextServiceFactory,
            ElasticSearchClient elasticSearchClient,
            DefaultUserChainFactoryCreator defaultUserChainFactoryCreator) {

        Preconditions.checkNotNull(serviceContext);

        InProcessSystemServiceFactory inProcessServiceFactory = (InProcessSystemServiceFactory) serviceContext
                .getSystemServiceFactory();

        UpdateServiceResource updateService = new UpdateServiceResource(serviceContext, firehoseQueueProducer,
                metricRegistry);
        ProcessServiceResource processService = new ProcessServiceResource(serviceContext, firehoseQueueProducer,
                queueResetter, descriptionFormatterFactory, descriptionExtractorFactory, metricRegistry, categories,
                fastTextServiceFactory, elasticSearchClient, insightsServiceFactory, defaultUserChainFactoryCreator);

        inProcessServiceFactory.setUpdateService(updateService);
        inProcessServiceFactory.setProcessService(processService);
        inProcessServiceFactory.setCronService(cronService);

        if (environment.isPresent()) {
            environment.get().lifecycle().manage(processService);
            environment.get().lifecycle().manage(inProcessServiceFactory.getNotificationGatewayService());
            environment.get().lifecycle().manage(cronService);

            environment.get().jersey().register(updateService);
            environment.get().jersey().register(processService);
            environment.get().jersey().register(inProcessServiceFactory.getNotificationGatewayService());
            environment.get().jersey().register(cronService);

            if (leaderCandidate.isPresent()) {
                CronJobManager cronJobManager = new CronJobManager(serviceContext, leaderCandidate.get(),
                        metricRegistry);
                environment.get().lifecycle().manage(cronJobManager);
            }
        } else {
            // Start services for tests. We are not expecting empty `Environment` in another case
            // TODO: When we upgrade Dropwizard, we should remove this and require an `Environment` (test Environment)
            try {
                processService.start();
                inProcessServiceFactory.getNotificationGatewayService().start();
                cronService.start();
            } catch (Exception e) {
                log.error("Cannot start services", e);
            }
        }

        // Avoid lazy-loading stuff on the first couple of requests. This makes the system instance more responsive
        // immediately after its service is being discoverable.
        processService.warmUp(serviceContext);

        return inProcessServiceFactory;
    }
}
