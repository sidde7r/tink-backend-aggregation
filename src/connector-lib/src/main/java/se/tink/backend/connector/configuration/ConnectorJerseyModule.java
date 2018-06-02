package se.tink.backend.connector.configuration;

import com.google.inject.AbstractModule;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import se.tink.backend.connector.api.ConnectorAccountService;
import se.tink.backend.connector.api.ConnectorBatchService;
import se.tink.backend.connector.api.ConnectorTransactionService;
import se.tink.backend.connector.api.ConnectorUserService;
import se.tink.backend.connector.api.ConnectorWebhookService;
import se.tink.backend.connector.auth.ConnectorAuthorizationResourceFilterFactory;
import se.tink.backend.connector.resources.VersionServiceResource;
import se.tink.backend.connector.transport.ConnectorMonitoringTransport;
import se.tink.libraries.jersey.guice.JerseyResourceRegistrar;
import se.tink.libraries.jersey.logging.AccessLoggingFilter;
import se.tink.libraries.jersey.logging.ResourceTimerFilterFactory;

public class ConnectorJerseyModule extends AbstractModule {

    private final JerseyEnvironment jersey;

    ConnectorJerseyModule(JerseyEnvironment jersey) {
        this.jersey = jersey;
    }

    @Override
    protected void configure() {
        JerseyResourceRegistrar.build()
                .binder(binder())
                .jersey(jersey)
                .addResources(VersionServiceResource.class,
                        ConnectorMonitoringTransport.class,
                        ConnectorUserService.class,
                        ConnectorAccountService.class,
                        ConnectorTransactionService.class,
                        ConnectorBatchService.class,
                        ConnectorWebhookService.class)
                .addFilterFactories(ResourceTimerFilterFactory.class,
                        ConnectorAuthorizationResourceFilterFactory.class)
                .addRequestFilters(AccessLoggingFilter.class)
                .addResponseFilters(AccessLoggingFilter.class)
                .bind();
    }
}
