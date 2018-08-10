package se.tink.backend.aggregation.guice.configuration;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import se.tink.backend.aggregation.queue.AutomaticRefreshQueueHandler;
import se.tink.backend.queue.QueueConsumer;
import se.tink.backend.queue.QueueProducer;
import se.tink.backend.queue.sqs.FakeProducer;
import se.tink.backend.queue.sqs.MessageHandler;
import se.tink.backend.queue.sqs.SqsConsumer;
import se.tink.backend.queue.sqs.SqsProducer;
import se.tink.backend.queue.sqs.SqsQueue;
import se.tink.backend.queue.sqs.configuration.SqsQueueConfiguration;


public class QueueModule  extends AbstractModule {
    private SqsQueueConfiguration sqsQueueConfiguration;

    public QueueModule(SqsQueueConfiguration sqsQueueConfiguration) {

        this.sqsQueueConfiguration = sqsQueueConfiguration;
    }

    @Override
    protected void configure() {
        if (sqsQueueConfiguration.isEnabled()) {
            bind(MessageHandler.class).to(AutomaticRefreshQueueHandler.class).in(Scopes.SINGLETON);
            bind(QueueConsumer.class).to(SqsConsumer.class).in(Scopes.SINGLETON);
            bind(QueueProducer.class).to(SqsProducer.class).in(Scopes.SINGLETON);
            bind(SqsQueue.class).in(Scopes.SINGLETON);
        } else {
            bind(QueueProducer.class).to(FakeProducer.class).in(Scopes.SINGLETON);
        }
    }
}
