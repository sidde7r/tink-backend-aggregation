package se.tink.backend.aggregation.provider.configuration.config;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.util.Providers;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import se.tink.backend.aggregation.cluster.JerseyClusterInfoProvider;
import se.tink.backend.aggregation.cluster.provider.ClusterInfoProvider;
import se.tink.backend.aggregation.provider.configuration.api.MonitoringService;
import se.tink.backend.aggregation.provider.configuration.api.ProviderService;
import se.tink.backend.aggregation.provider.configuration.controllers.ProviderConfigurationProvider;
import se.tink.backend.aggregation.provider.configuration.controllers.ProviderServiceController;
import se.tink.backend.aggregation.provider.configuration.resources.MonitoringServiceResource;
import se.tink.backend.aggregation.provider.configuration.resources.ProviderServiceResource;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.libraries.discovery.CoordinationConfiguration;
import se.tink.libraries.jersey.guice.JerseyResourceRegistrar;
import se.tink.libraries.jersey.logging.AccessLoggingFilter;
import se.tink.libraries.jersey.logging.ResourceTimerFilterFactory;
import se.tink.libraries.metrics.PrometheusConfiguration;

public class ProviderServiceModule extends AbstractModule {
    private ServiceConfiguration configuration;
    private final JerseyEnvironment jersey;

    ProviderServiceModule(ServiceConfiguration configuration, JerseyEnvironment jersey) {
        this.configuration = configuration;
        this.jersey = jersey;
    }

    @Override
    protected void configure() {
        bind(ProviderService.class).to(ProviderServiceResource.class).in(Scopes.SINGLETON);
        bind(MonitoringService.class).to(MonitoringServiceResource.class).in(Scopes.SINGLETON);
        bind(ProviderServiceController.class).in(Scopes.SINGLETON);
        bind(ClusterInfoProvider.class).in(Scopes.SINGLETON);
        bind(ProviderConfigurationProvider.class).in(Scopes.SINGLETON);

        bind(CoordinationConfiguration.class).toProvider(Providers.of(configuration.getCoordination()));
        bind(PrometheusConfiguration.class).toInstance(configuration.getPrometheus());

        JerseyResourceRegistrar.build()
                .binder(binder())
                .jersey(jersey)
                .addFilterFactories(ResourceTimerFilterFactory.class)
                .addRequestFilters(AccessLoggingFilter.class)
                .addResponseFilters(AccessLoggingFilter.class)
                .addResources(ProviderService.class)
                .addResources(MonitoringService.class)
                .addResources(JerseyClusterInfoProvider.class)
                .bind();
    }
}
