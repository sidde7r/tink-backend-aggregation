package se.tink.backend.aggregation.guice.configuration;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import se.tink.backend.aggregation.queue.AutomaticRefreshQueueHandler;
import se.tink.backend.queue.QueueConsumer;
import se.tink.backend.queue.QueueProducer;
import se.tink.backend.queue.sqs.MessageHandler;
import se.tink.backend.queue.sqs.SqsConsumer;
import se.tink.backend.queue.sqs.SqsProducer;
import se.tink.backend.queue.sqs.SqsQueue;


public class QueueModule  extends AbstractModule {
    @Override
    protected void configure() {
        bind(MessageHandler.class).to(AutomaticRefreshQueueHandler.class).in(Scopes.SINGLETON);
        bind(QueueConsumer.class).to(SqsConsumer.class).in(Scopes.SINGLETON);
        bind(QueueProducer.class).to(SqsProducer.class).in(Scopes.SINGLETON);
        bind(SqsQueue.class).in(Scopes.SINGLETON);
    }
}