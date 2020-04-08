package se.tink.backend.aggregation.resources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Test;
import se.tink.backend.aggregation.client.provider_configuration.ProviderConfigurationService;
import se.tink.backend.aggregation.controllers.ProviderSessionCacheController;
import se.tink.backend.aggregation.controllers.SupplementalInformationController;
import se.tink.backend.aggregation.startupchecks.StartupChecksHandler;
import se.tink.backend.aggregation.workers.worker.AgentWorker;
import se.tink.backend.aggregation.workers.worker.AgentWorkerOperationFactory;
import se.tink.libraries.draining.ApplicationDrainMode;
import se.tink.libraries.queue.QueueProducer;

public class AggregationServiceResourceTest {

    @Test
    public void pingShouldReturnPong() {
        // given
        Injector injector = Guice.createInjector(new TestModule());
        AggregationServiceResource resource =
                injector.getInstance(AggregationServiceResource.class);

        // when
        String response = resource.ping();

        // then
        assertThat(response).isEqualTo("pong");
    }

    private static class TestModule extends AbstractModule {

        @Override
        protected void configure() {
            // AggregationServiceResource
            bind(AgentWorker.class).toInstance(mock(AgentWorker.class));
            bind(QueueProducer.class).toInstance(mock(QueueProducer.class));
            bind(AgentWorkerOperationFactory.class)
                    .toInstance(mock(AgentWorkerOperationFactory.class));
            bind(SupplementalInformationController.class)
                    .toInstance(mock(SupplementalInformationController.class));
            bind(ProviderSessionCacheController.class)
                    .toInstance(mock(ProviderSessionCacheController.class));
            bind(ApplicationDrainMode.class).toInstance(mock(ApplicationDrainMode.class));
            bind(ProviderConfigurationService.class)
                    .toInstance(mock(ProviderConfigurationService.class));
            bind(StartupChecksHandler.class).toInstance(mock(StartupChecksHandler.class));
        }
    }
}
