package se.tink.backend.guice.configuration;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import se.tink.backend.aggregation.client.AggregationServiceFactory;
import se.tink.backend.aggregation.client.InProcessAggregationServiceFactory;
import se.tink.backend.categorization.client.FastTextServiceFactory;
import se.tink.backend.client.InProcessServiceFactory;
import se.tink.backend.client.ServiceFactory;
import se.tink.backend.common.client.FastTextServiceFactoryProvider;
import se.tink.backend.common.client.GdprExportServiceFactoryProvider;
import se.tink.backend.common.client.InsightsServiceFactoryProvider;
import se.tink.backend.common.client.ProductExecutorServiceFactoryProvider;
import se.tink.backend.common.tasks.interfaces.TaskSubmitter;
import se.tink.backend.common.tasks.kafka.KafkaTaskSubmitter;
import se.tink.backend.encryption.client.EncryptionServiceFactory;
import se.tink.backend.encryption.client.InProcessEncryptionServiceFactory;
import se.tink.backend.export.client.GdprExportServiceFactory;
import se.tink.backend.firehose.v1.queue.DummyFirehoseQueueProducer;
import se.tink.backend.firehose.v1.queue.FirehoseQueueProducer;
import se.tink.backend.insights.client.InsightsServiceFactory;
import se.tink.backend.product.execution.ProductExecutorServiceFactory;
import se.tink.backend.system.client.InProcessSystemServiceFactory;
import se.tink.backend.system.client.SystemServiceFactory;

public class TestServicesModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ServiceFactory.class).to(InProcessServiceFactory.class).asEagerSingleton();
        bind(SystemServiceFactory.class).to(InProcessSystemServiceFactory.class).asEagerSingleton();
        bind(AggregationServiceFactory.class).to(InProcessAggregationServiceFactory.class).asEagerSingleton();
        bind(EncryptionServiceFactory.class).to(InProcessEncryptionServiceFactory.class).asEagerSingleton();
        bind(TaskSubmitter.class).to(KafkaTaskSubmitter.class).in(Scopes.SINGLETON);
        bind(FirehoseQueueProducer.class).to(DummyFirehoseQueueProducer.class);
        bind(FastTextServiceFactory.class).toProvider(FastTextServiceFactoryProvider.class);
        bind(InsightsServiceFactory.class).toProvider(InsightsServiceFactoryProvider.class);
        bind(GdprExportServiceFactory.class).toProvider(GdprExportServiceFactoryProvider.class);
        bind(ProductExecutorServiceFactory.class).toProvider(ProductExecutorServiceFactoryProvider.class).in(Scopes.SINGLETON);
    }
}
