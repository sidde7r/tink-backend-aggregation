package se.tink.backend.grpc.v1.guice.configuration.queue;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import java.util.List;
import se.tink.backend.common.config.FirehoseConfiguration;
import se.tink.backend.common.config.TasksQueueConfiguration;
import se.tink.backend.firehose.v1.queue.FirehoseTopics;
import se.tink.backend.firehose.v1.rpc.FirehoseMessage;
import se.tink.backend.grpc.v1.streaming.StreamingQueueConsumerHandler;
import se.tink.backend.queue.MockQueueConsumer;
import se.tink.backend.queue.QueueConsumer;
import se.tink.backend.queue.kafka.KafkaQueueConsumer;
import se.tink.backend.queue.kafka.KafkaQueueConsumerProperties;
import se.tink.libraries.metrics.MetricRegistry;

public class QueueConsumerModule extends AbstractModule {
    @Override
    protected void configure() {
    }

    @Provides
    @Singleton
    public QueueConsumer provideKafkaQueueConsumer(TasksQueueConfiguration configuration,
            FirehoseConfiguration firehoseConfiguration,
            MetricRegistry metricRegistry, StreamingQueueConsumerHandler queueConsumerHandler) {
        if (configuration == null || !configuration.isFirehoseEnabled()) {
            return new MockQueueConsumer();
        } else {
            List<KafkaQueueConsumer.KafkaQueueConsumerSubscriber> subscribers = ImmutableList.of(
                    new KafkaQueueConsumer.KafkaQueueConsumerSubscriber<>(FirehoseTopics.ACCOUNTS,
                            FirehoseMessage.parser(), queueConsumerHandler),
                    new KafkaQueueConsumer.KafkaQueueConsumerSubscriber<>(FirehoseTopics.CREDENTIALS,
                            FirehoseMessage.parser(), queueConsumerHandler),
                    new KafkaQueueConsumer.KafkaQueueConsumerSubscriber<>(FirehoseTopics.FOLLOW_ITEMS,
                            FirehoseMessage.parser(), queueConsumerHandler),
                    new KafkaQueueConsumer.KafkaQueueConsumerSubscriber<>(FirehoseTopics.PERIODS,
                            FirehoseMessage.parser(), queueConsumerHandler),
                    new KafkaQueueConsumer.KafkaQueueConsumerSubscriber<>(FirehoseTopics.SIGNABLE_OPERATIONS,
                            FirehoseMessage.parser(), queueConsumerHandler),
                    new KafkaQueueConsumer.KafkaQueueConsumerSubscriber<>(FirehoseTopics.STATISTICS,
                            FirehoseMessage.parser(), queueConsumerHandler),
                    new KafkaQueueConsumer.KafkaQueueConsumerSubscriber<>(FirehoseTopics.TRANSACTIONS,
                            FirehoseMessage.parser(), queueConsumerHandler),
                    new KafkaQueueConsumer.KafkaQueueConsumerSubscriber<>(FirehoseTopics.USER_CONFIGURATION,
                            FirehoseMessage.parser(), queueConsumerHandler));

            KafkaQueueConsumerProperties properties = new KafkaQueueConsumerProperties();
            properties.setGroupId(configuration.getGroupId());
            properties.setHosts(configuration.getHosts());
            properties.setStartFromLastMessage(firehoseConfiguration.shouldConsumerStartFromLatestMessage());
            properties.setMaxPollRecords(firehoseConfiguration.getMaxPollMessages());

            return new KafkaQueueConsumer(metricRegistry, subscribers, properties);
        }
    }

    @Provides
    @Singleton
    public StreamingQueueConsumerHandler provideQueueConsumerHandler(MetricRegistry metricRegistry) {
        return new StreamingQueueConsumerHandler(
                (deviceId, streamingResponseHandler) -> streamingResponseHandler.complete(), metricRegistry);
    }
}
