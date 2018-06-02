package se.tink.backend.connector.configuration.seb;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import se.tink.libraries.jersey.guice.JerseyResourceRegistrar;
import se.tink.libraries.jersey.logging.AccessLoggingFilter;
import se.tink.libraries.jersey.logging.ResourceTimerFilterFactory;
import se.tink.backend.connector.auth.ConnectorAuthorizationResourceFilterFactory;
import se.tink.backend.connector.resources.ConnectorServiceResource;
import se.tink.backend.connector.resources.SEBConnectorServiceResource;
import se.tink.backend.connector.resources.VersionServiceResource;
import se.tink.backend.connector.transport.ConnectorMonitoringTransport;

public class SebConnectorModule extends AbstractModule {

    private final JerseyEnvironment jersey;

    public SebConnectorModule(JerseyEnvironment jersey) {
        this.jersey = jersey;
    }

    @Override
    protected void configure() {
        bind(ConnectorServiceResource.class).to(SEBConnectorServiceResource.class).in(Scopes.SINGLETON);
        JerseyResourceRegistrar.build()
                .binder(binder())
                .jersey(jersey)
                .addResources(VersionServiceResource.class,
                        ConnectorMonitoringTransport.class,
                        ConnectorServiceResource.class)
                .addFilterFactories(
                        ResourceTimerFilterFactory.class,
                        ConnectorAuthorizationResourceFilterFactory.class)
                .addRequestFilters(AccessLoggingFilter.class)
                .addResponseFilters(AccessLoggingFilter.class)
                .bind();
    }

}
