package se.tink.backend.connector.configuration.abn;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import se.tink.backend.connector.api.CategorizationConnectorService;
import se.tink.backend.connector.auth.ConnectorAuthorizationResourceFilterFactory;
import se.tink.backend.connector.resources.AbnAmroConnectorServiceResource;
import se.tink.backend.connector.resources.ConnectorServiceResource;
import se.tink.backend.connector.resources.VersionServiceResource;
import se.tink.backend.connector.transport.ConnectorCategorizationServiceJerseyTransport;
import se.tink.backend.connector.transport.ConnectorMonitoringTransport;
import se.tink.libraries.jersey.guice.JerseyResourceRegistrar;
import se.tink.libraries.jersey.logging.AccessLoggingFilter;
import se.tink.libraries.jersey.logging.ResourceTimerFilterFactory;

public class AbnAmroConnectorModule extends AbstractModule {

    private final JerseyEnvironment jersey;

    public AbnAmroConnectorModule(JerseyEnvironment jersey) {
        this.jersey = jersey;
    }

    @Override
    protected void configure() {
        bind(ConnectorServiceResource.class).to(AbnAmroConnectorServiceResource.class).in(Scopes.SINGLETON);
        bind(CategorizationConnectorService.class).to(ConnectorCategorizationServiceJerseyTransport.class).in(Scopes.SINGLETON);
        JerseyResourceRegistrar.build()
                .binder(binder())
                .jersey(jersey)
                .addResources(VersionServiceResource.class,
                        ConnectorMonitoringTransport.class,
                        ConnectorServiceResource.class,
                        CategorizationConnectorService.class)
                .addFilterFactories(
                        ResourceTimerFilterFactory.class,
                        ConnectorAuthorizationResourceFilterFactory.class)
                .addRequestFilters(AccessLoggingFilter.class)
                .addResponseFilters(AccessLoggingFilter.class)
                .bind();
    }
}
