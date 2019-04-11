package se.tink.backend.aggregation.provider.configuration.config;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import se.tink.backend.aggregation.provider.configuration.api.MonitoringService;
import se.tink.backend.aggregation.provider.configuration.api.ProviderService;
import se.tink.backend.aggregation.provider.configuration.cluster.jersey.JerseyClusterIdProvider;
import se.tink.backend.aggregation.provider.configuration.cluster.providers.ClusterIdProvider;
import se.tink.backend.aggregation.provider.configuration.controllers.ProviderServiceController;
import se.tink.backend.aggregation.provider.configuration.core.ProviderConfigurationDAO;
import se.tink.backend.aggregation.provider.configuration.http.resources.MonitoringServiceResource;
import se.tink.backend.aggregation.provider.configuration.http.resources.ProviderServiceResource;
import se.tink.backend.aggregation.provider.configuration.logging.ProviderLoggerRequestFilter;
import se.tink.backend.aggregation.provider.configuration.storage.ClusterProviderHandler;
import se.tink.backend.aggregation.provider.configuration.storage.ProviderConfigurationProvider;
import se.tink.libraries.http.client.RequestTracingFilter;
import se.tink.libraries.jersey.guice.JerseyResourceRegistrar;
import se.tink.libraries.jersey.logging.AccessLoggingFilter;
import se.tink.libraries.jersey.logging.ResourceTimerFilterFactory;

public class ProviderServiceModule extends AbstractModule {
    private final JerseyEnvironment jersey;

    ProviderServiceModule(JerseyEnvironment jersey) {
        this.jersey = jersey;
    }

    @Override
    protected void configure() {
        bind(ProviderService.class).to(ProviderServiceResource.class).in(Scopes.SINGLETON);
        bind(MonitoringService.class).to(MonitoringServiceResource.class).in(Scopes.SINGLETON);
        bind(ClusterIdProvider.class).in(Scopes.SINGLETON);
        bind(ProviderServiceController.class).in(Scopes.SINGLETON);
        bind(ClusterProviderHandler.class).in(Scopes.SINGLETON);
        bind(ProviderConfigurationDAO.class)
                .to(ProviderConfigurationProvider.class)
                .in(Scopes.SINGLETON);

        JerseyResourceRegistrar.build()
                .binder(binder())
                .jersey(jersey)
                .addFilterFactories(ResourceTimerFilterFactory.class)
                .addRequestFilters(
                        AccessLoggingFilter.class,
                        RequestTracingFilter.class,
                        ProviderLoggerRequestFilter.class)
                .addResponseFilters(AccessLoggingFilter.class, RequestTracingFilter.class)
                .addResources(ProviderService.class)
                .addResources(MonitoringService.class)
                .addResources(JerseyClusterIdProvider.class)
                .bind();
    }
}
