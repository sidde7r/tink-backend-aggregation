package se.tink.backend.aggregation.resources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import java.util.Optional;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.junit.Test;
import se.tink.backend.aggregation.client.provider_configuration.ProviderConfigurationService;
import se.tink.backend.aggregation.controllers.ProviderSessionCacheController;
import se.tink.backend.aggregation.controllers.SupplementalInformationController;
import se.tink.backend.aggregation.startupchecks.StartupChecksHandler;
import se.tink.backend.aggregation.workers.abort.RequestAbortHandler;
import se.tink.backend.aggregation.workers.operation.RequestStatus;
import se.tink.backend.aggregation.workers.worker.AgentWorker;
import se.tink.backend.aggregation.workers.worker.AgentWorkerOperationFactory;
import se.tink.libraries.draining.ApplicationDrainMode;
import se.tink.libraries.queue.QueueProducer;

public class AggregationServiceResourceTest {

    @Test
    public void pingShouldReturnPong() {
        // given
        Injector injector = Guice.createInjector(new TestModule(mock(RequestAbortHandler.class)));
        AggregationServiceResource resource =
                injector.getInstance(AggregationServiceResource.class);

        // when
        String response = resource.ping();

        // then
        assertThat(response).isEqualTo("pong");
    }

    @Test
    public void abortTransferShouldReturnNotFoundResponseWhenStatusIsEmpty() {
        // given
        RequestAbortHandler requestAbortHandler = mock(RequestAbortHandler.class);
        String requestId = "a0d573a7-0ddb-4314-bc42-377425029b5b";
        when(requestAbortHandler.handle(eq(requestId))).thenReturn(Optional.empty());
        Injector injector = Guice.createInjector(new TestModule(requestAbortHandler));
        AggregationServiceResource resource =
                injector.getInstance(AggregationServiceResource.class);

        try {
            // when
            resource.abortTransfer(requestId);
        } catch (WebApplicationException e) {
            // then
            Response response = e.getResponse();
            assertNotNull(response);
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
            return;
        }

        throw new AssertionError("Expected exception was not thrown");
    }

    @Test
    public void abortTransferShouldReturnAcceptedResponseWhenStatusIsTryingToAbort() {
        // given
        RequestAbortHandler requestAbortHandler = mock(RequestAbortHandler.class);
        String requestId = "a0d573a7-0ddb-4314-bc42-377425029b5b";
        when(requestAbortHandler.handle(eq(requestId)))
                .thenReturn(Optional.of(RequestStatus.TRYING_TO_ABORT));
        Injector injector = Guice.createInjector(new TestModule(requestAbortHandler));
        AggregationServiceResource resource =
                injector.getInstance(AggregationServiceResource.class);

        // when
        Response response = resource.abortTransfer(requestId);

        // then
        assertNotNull(response);
        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatus());
    }

    @Test
    public void abortTransferShouldReturnOkResponseWhenStatusIsImpossibleToAbort() {
        // given
        RequestAbortHandler requestAbortHandler = mock(RequestAbortHandler.class);
        String requestId = "a0d573a7-0ddb-4314-bc42-377425029b5b";
        when(requestAbortHandler.handle(eq(requestId)))
                .thenReturn(Optional.of(RequestStatus.COMPLETED));
        Injector injector = Guice.createInjector(new TestModule(requestAbortHandler));
        AggregationServiceResource resource =
                injector.getInstance(AggregationServiceResource.class);

        // when
        Response response = resource.abortTransfer(requestId);

        // then
        assertNotNull(response);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    private static class TestModule extends AbstractModule {

        private final RequestAbortHandler requestAbortHandler;

        public TestModule(RequestAbortHandler requestAbortHandler) {
            this.requestAbortHandler = requestAbortHandler;
        }

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
            bind(RequestAbortHandler.class).toInstance(requestAbortHandler);
        }
    }
}
