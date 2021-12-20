package se.tink.backend.aggregation.configuration.guice.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.configuration.models.AggregationServiceConfiguration;
import se.tink.backend.aggregation.queue.AutomaticRefreshQueueEncoder;
import se.tink.backend.aggregation.queue.AutomaticRefreshQueueHandler;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.queue.QueueConsumerService;
import se.tink.libraries.queue.QueueProducer;
import se.tink.libraries.queue.sqs.EncodingHandler;
import se.tink.libraries.queue.sqs.QueueMessageAction;
import se.tink.libraries.queue.sqs.SqsConsumer;
import se.tink.libraries.queue.sqs.SqsConsumerService;
import se.tink.libraries.queue.sqs.SqsProducer;
import se.tink.libraries.queue.sqs.SqsQueue;
import se.tink.libraries.queue.sqs.configuration.SqsQueueConfiguration;

@Slf4j
public class SqsQueueModule extends AbstractModule {
    public SqsQueueModule() {}

    @Override
    protected void configure() {
        requireBinding(AggregationServiceConfiguration.class);
        requireBinding(MetricRegistry.class);
        bind(QueueMessageAction.class).to(AutomaticRefreshQueueHandler.class).in(Scopes.SINGLETON);
        bind(QueueConsumerService.class).to(SqsConsumerService.class).in(Scopes.SINGLETON);
        bind(EncodingHandler.class).to(AutomaticRefreshQueueEncoder.class).in(Scopes.SINGLETON);
        bindConstant().annotatedWith(Names.named("regularQueueMinConsumption")).to(0.05f);
        log.info("Configuring SqsQueueModule");
    }

    @Provides
    @Singleton
    @Named("regularQueueProducer")
    QueueProducer provideRegularQueueProducer(
            @Named("regularSqsQueue") SqsQueue sqsQueue, EncodingHandler encodingHandler) {
        return new SqsProducer(sqsQueue, encodingHandler);
    }

    @Provides
    @Singleton
    @Named("priorityQueueProducer")
    QueueProducer providePriorityQueueProducer(
            @Named("prioritySqsQueue") SqsQueue sqsQueue, EncodingHandler encodingHandler) {
        return new SqsProducer(sqsQueue, encodingHandler);
    }

    @Provides
    @Singleton
    @Named("regularSqsQueue")
    SqsQueue provideRegularSqsQueue(
            @Named("regularSqsQueueConfiguration") SqsQueueConfiguration configuration,
            MetricRegistry metricRegistry) {
        return new SqsQueue(configuration, metricRegistry);
    }

    @Provides
    @Singleton
    @Named("prioritySqsQueue")
    SqsQueue providePrioritySqsQueue(
            @Named("prioritySqsQueueConfiguration") SqsQueueConfiguration configuration,
            MetricRegistry metricRegistry) {
        try {
            return new SqsQueue(configuration, metricRegistry);
        } catch (Exception e) {
            throw new IllegalStateException("Could not create Priority Queue", e);
        }
    }

    @Provides
    @Singleton
    @Named("regularSqsQueueConfiguration")
    SqsQueueConfiguration provideRegularSqsQueueConfiguration(
            AggregationServiceConfiguration configuration) {
        return configuration.getRegularSqsQueueConfiguration();
    }

    @Provides
    @Singleton
    @Named("prioritySqsQueueConfiguration")
    SqsQueueConfiguration providePrioritySqsQueueConfiguration(
            AggregationServiceConfiguration configuration) {
        return configuration.getPrioritySqsQueueConfiguration();
    }

    @Provides
    @Singleton
    @Named("regularSqsConsumer")
    SqsConsumer provideRegularSqsConsumer(
            @Named("regularSqsQueue") SqsQueue regularSqsQueue,
            @Named("regularQueueProducer") QueueProducer regularQueueProducer,
            QueueMessageAction queueMessageAction) {
        return new SqsConsumer(
                regularSqsQueue, regularQueueProducer, queueMessageAction, "Regular");
    }

    @Provides
    @Singleton
    @Named("prioritySqsConsumer")
    SqsConsumer providePrioritySqsConsumer(
            @Named("prioritySqsQueue") SqsQueue prioritySqsQueue,
            @Named("priorityQueueProducer") QueueProducer priorityQueueProducer,
            QueueMessageAction queueMessageAction) {
        return new SqsConsumer(
                prioritySqsQueue, priorityQueueProducer, queueMessageAction, "Priority");
    }
}
