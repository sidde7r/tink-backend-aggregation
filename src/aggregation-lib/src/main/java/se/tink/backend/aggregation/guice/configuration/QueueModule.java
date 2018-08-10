package se.tink.backend.aggregation.guice.configuration;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import se.tink.backend.aggregation.queue.AutomaticRefreshQueueEncoder;
import se.tink.backend.aggregation.queue.AutomaticRefreshQueueHandler;
import se.tink.backend.queue.QueueProducer;
import se.tink.backend.queue.sqs.EncodingHandler;
import se.tink.backend.queue.sqs.FakeProducer;
import se.tink.backend.queue.sqs.MessageHandler;
import se.tink.backend.queue.sqs.SqsConsumer;
import se.tink.backend.queue.sqs.SqsProducer;
import se.tink.backend.queue.sqs.SqsQueue;
import se.tink.backend.queue.sqs.configuration.SqsQueueConfiguration;


public class QueueModule  extends AbstractModule {
    private SqsQueueConfiguration sqsQueueConfiguration;
    private LifecycleEnvironment lifecycle;

    public QueueModule(SqsQueueConfiguration sqsQueueConfiguration, LifecycleEnvironment lifecycle) {

        this.sqsQueueConfiguration = sqsQueueConfiguration;
        this.lifecycle = lifecycle;
    }

    @Override
    protected void configure() {
        if (sqsQueueConfiguration.isEnabled()) {
            bind(MessageHandler.class).to(AutomaticRefreshQueueHandler.class).in(Scopes.SINGLETON);
            bind(QueueProducer.class).to(SqsProducer.class).in(Scopes.SINGLETON);
            bind(SqsQueue.class).in(Scopes.SINGLETON);
            bind(EncodingHandler.class).to(AutomaticRefreshQueueEncoder.class).in(Scopes.SINGLETON);
        } else {
            bind(QueueProducer.class).to(FakeProducer.class).in(Scopes.SINGLETON);
        }
    }

    @Provides
    @Singleton
    public SqsConsumer manageQueueThread(SqsQueue sqsQueue, MessageHandler messageHandler) {
        SqsConsumer sqsConsumer = new SqsConsumer(sqsQueue, messageHandler);
        lifecycle.manage(sqsConsumer);
        return sqsConsumer;
    }
}
