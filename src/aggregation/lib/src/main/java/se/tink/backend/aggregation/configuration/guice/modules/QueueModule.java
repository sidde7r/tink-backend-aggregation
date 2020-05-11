package se.tink.backend.aggregation.configuration.guice.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import se.tink.backend.aggregation.queue.AutomaticRefreshQueueEncoder;
import se.tink.backend.aggregation.queue.AutomaticRefreshQueueHandler;
import se.tink.libraries.queue.QueueConsumer;
import se.tink.libraries.queue.QueueProducer;
import se.tink.libraries.queue.sqs.EncodingHandler;
import se.tink.libraries.queue.sqs.FakeHandler;
import se.tink.libraries.queue.sqs.FakeProducer;
import se.tink.libraries.queue.sqs.QueueMessageAction;
import se.tink.libraries.queue.sqs.SqsConsumer;
import se.tink.libraries.queue.sqs.SqsProducer;
import se.tink.libraries.queue.sqs.configuration.SqsQueueConfiguration;

public class QueueModule extends AbstractModule {
    private SqsQueueConfiguration sqsQueueConfiguration;

    public QueueModule(SqsQueueConfiguration sqsQueueConfiguration) {
        this.sqsQueueConfiguration = sqsQueueConfiguration;
    }

    @Override
    protected void configure() {
        if (sqsQueueConfiguration.isEnabled()) {
            bind(QueueMessageAction.class)
                    .to(AutomaticRefreshQueueHandler.class)
                    .in(Scopes.SINGLETON);
            bind(QueueProducer.class).to(SqsProducer.class).in(Scopes.SINGLETON);
            bind(QueueConsumer.class).to(SqsConsumer.class).in(Scopes.SINGLETON);
            bind(EncodingHandler.class).to(AutomaticRefreshQueueEncoder.class).in(Scopes.SINGLETON);
        } else {
            bind(QueueProducer.class).to(FakeProducer.class).in(Scopes.SINGLETON);
            bind(QueueMessageAction.class).to(FakeHandler.class).in(Scopes.SINGLETON);
        }
    }
}
