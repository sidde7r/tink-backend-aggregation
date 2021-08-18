package se.tink.backend.aggregation.configuration.guice.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.extern.slf4j.Slf4j;
import se.tink.libraries.queue.QueueConsumer;
import se.tink.libraries.queue.QueueProducer;
import se.tink.libraries.queue.sqs.*;

@Slf4j
public class FakeQueueModule extends AbstractModule {

    public FakeQueueModule() {}

    @Override
    protected void configure() {
        bind(QueueMessageAction.class).to(FakeHandler.class).in(Scopes.SINGLETON);
        bind(QueueConsumer.class).to(FakeConsumer.class).in(Scopes.SINGLETON);
        log.info("Configuring FakeQueueModule");
    }

    @Provides
    @Singleton
    @Named("regularQueueProducer")
    QueueProducer provideRegularQueueProducer() {
        return new FakeProducer();
    }

    @Provides
    @Singleton
    @Named("priorityQueueProducer")
    QueueProducer providePriorityQueueProducer() {
        return new FakeProducer();
    }
}
