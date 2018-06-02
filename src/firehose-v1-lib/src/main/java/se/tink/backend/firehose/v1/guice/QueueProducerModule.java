package se.tink.backend.firehose.v1.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import se.tink.backend.common.config.TasksQueueConfiguration;
import se.tink.backend.firehose.v1.queue.DummyFirehoseQueueProducer;
import se.tink.backend.firehose.v1.queue.FirehoseQueueProducer;
import se.tink.backend.firehose.v1.queue.RealFirehoseQueueProducer;
import se.tink.backend.queue.LoggingQueueProducer;
import se.tink.backend.queue.QueueProducer;
import se.tink.backend.queue.kafka.KafkaQueueProducer;

public class QueueProducerModule extends AbstractModule {
    @Override
    protected void configure() {
        // Deliberately left empty because of the @Provides methods below.
    }

    @Provides
    @Singleton
    public FirehoseQueueProducer provideFirehoseQueueProducer(TasksQueueConfiguration configuration, QueueProducer queueProducer) {
        if (configuration != null && configuration.isFirehoseEnabled()) {
            return new RealFirehoseQueueProducer(queueProducer);
        } else {
            return new DummyFirehoseQueueProducer();
        }
    }

    @Provides
    @Singleton
    public QueueProducer provideQueueProducer(TasksQueueConfiguration configuration) {
        if (configuration != null && configuration.getHosts() != null && configuration.isFirehoseEnabled()) {
            return new KafkaQueueProducer(configuration.getHosts());
        } else {
            return new LoggingQueueProducer();
        }
    }
}
