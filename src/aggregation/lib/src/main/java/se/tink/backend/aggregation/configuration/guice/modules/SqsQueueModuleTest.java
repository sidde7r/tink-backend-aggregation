package se.tink.backend.aggregation.configuration.guice.modules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;

import com.google.inject.AbstractModule;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import org.junit.Test;
import se.tink.backend.aggregation.configuration.models.AggregationServiceConfiguration;
import se.tink.backend.aggregation.storage.database.providers.ClientConfigurationProvider;
import se.tink.backend.aggregation.workers.worker.AgentWorkerOperationFactory;
import se.tink.libraries.queue.QueueProducer;

public class SqsQueueModuleTest {

    @Test
    public void shouldReturnRegularQueueProducer() {
        Injector injector = init();
        QueueProducer regularQueueProducer =
                injector.getInstance(
                        Key.get(QueueProducer.class, Names.named("regularQueueProducer")));
        assertThat(regularQueueProducer).isNotNull();
    }

    @Test
    public void shouldReturnPriorityQueueProducer() {
        Injector injector = init();
        QueueProducer priorityQueueProducer =
                injector.getInstance(
                        Key.get(QueueProducer.class, Names.named("priorityQueueProducer")));
        assertThat(priorityQueueProducer).isNotNull();
    }

    @Test
    public void shouldReturnPriorityRetryQueueProducer() {
        Injector injector = init();
        QueueProducer priorityQueueProducer =
                injector.getInstance(
                        Key.get(QueueProducer.class, Names.named("priorityRetryQueueProducer")));
        assertThat(priorityQueueProducer).isNotNull();
    }

    @Test
    public void shouldThrowIfTryingToGetNonExistingBean() {
        Injector injector = init();
        Throwable t =
                catchThrowable(
                        () ->
                                injector.getInstance(
                                        Key.get(QueueProducer.class, Names.named("foo bar"))));
        assertThat(t).isExactlyInstanceOf(ConfigurationException.class);
    }

    private Injector init() {
        return Guice.createInjector(
                new SqsQueueModule(),
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        // defining bindings below is not optimal but injecting other modules
                        // requires
                        // pulling in the whole universe but we just want to test SqsQueueModule
                        AggregationServiceConfiguration aggregationServiceConfiguration =
                                new AggregationServiceConfiguration();
                        bind(AggregationServiceConfiguration.class)
                                .toInstance(aggregationServiceConfiguration);
                        bindConstant()
                                .annotatedWith(Names.named("queueAvailable"))
                                .to(
                                        aggregationServiceConfiguration
                                                .getRegularSqsQueueConfiguration()
                                                .isEnabled());
                        bind(AgentWorkerOperationFactory.class)
                                .toInstance(mock(AgentWorkerOperationFactory.class));
                        bind(ClientConfigurationProvider.class)
                                .toInstance(mock(ClientConfigurationProvider.class));
                    }
                });
    }
}
