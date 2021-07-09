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
import se.tink.backend.aggregation.workers.abort.OperationAbortHandler;
import se.tink.backend.aggregation.workers.operation.OperationStatus;
import se.tink.backend.aggregation.workers.worker.AgentWorker;
import se.tink.backend.aggregation.workers.worker.AgentWorkerOperationFactory;
import se.tink.libraries.draining.ApplicationDrainMode;
import se.tink.libraries.queue.QueueProducer;

public class AggregationServiceResourceTest {

    @Test
    public void pingShouldReturnPong() {
        // given
        Injector injector = Guice.createInjector(new TestModule(mock(OperationAbortHandler.class)));
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
        OperationAbortHandler operationAbortHandler = mock(OperationAbortHandler.class);
        String operationId = "a0d573a7-0ddb-4314-bc42-377425029b5b";
        when(operationAbortHandler.handle(eq(operationId))).thenReturn(Optional.empty());
        Injector injector = Guice.createInjector(new TestModule(operationAbortHandler));
        AggregationServiceResource resource =
                injector.getInstance(AggregationServiceResource.class);

        try {
            // when
            resource.abortTransfer(operationId);
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
        OperationAbortHandler operationAbortHandler = mock(OperationAbortHandler.class);
        String operationId = "a0d573a7-0ddb-4314-bc42-377425029b5b";
        when(operationAbortHandler.handle(eq(operationId)))
                .thenReturn(Optional.of(OperationStatus.TRYING_TO_ABORT));
        Injector injector = Guice.createInjector(new TestModule(operationAbortHandler));
        AggregationServiceResource resource =
                injector.getInstance(AggregationServiceResource.class);

        // when
        Response response = resource.abortTransfer(operationId);

        // then
        assertNotNull(response);
        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatus());
    }

    @Test
    public void abortTransferShouldReturnOkResponseWhenStatusIsImpossibleToAbort() {
        // given
        OperationAbortHandler operationAbortHandler = mock(OperationAbortHandler.class);
        String operationId = "a0d573a7-0ddb-4314-bc42-377425029b5b";
        when(operationAbortHandler.handle(eq(operationId)))
                .thenReturn(Optional.of(OperationStatus.IMPOSSIBLE_TO_ABORT));
        Injector injector = Guice.createInjector(new TestModule(operationAbortHandler));
        AggregationServiceResource resource =
                injector.getInstance(AggregationServiceResource.class);

        // when
        Response response = resource.abortTransfer(operationId);

        // then
        assertNotNull(response);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    private static class TestModule extends AbstractModule {

        private final OperationAbortHandler operationAbortHandler;

        public TestModule(OperationAbortHandler operationAbortHandler) {
            this.operationAbortHandler = operationAbortHandler;
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
            bind(OperationAbortHandler.class).toInstance(operationAbortHandler);
        }
    }
}
