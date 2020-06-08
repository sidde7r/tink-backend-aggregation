package se.tink.backend.aggregation.configuration.guice.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import se.tink.libraries.queue.QueueConsumer;
import se.tink.libraries.queue.QueueProducer;
import se.tink.libraries.queue.sqs.*;

public class FakeQueueModule extends AbstractModule {

    public FakeQueueModule() {}

    @Override
    protected void configure() {
        bind(QueueProducer.class).to(FakeProducer.class).in(Scopes.SINGLETON);
        bind(QueueMessageAction.class).to(FakeHandler.class).in(Scopes.SINGLETON);
        bind(QueueConsumer.class).to(FakeConsumer.class).in(Scopes.SINGLETON);
    }
}
