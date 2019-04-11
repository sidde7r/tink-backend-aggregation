package se.tink.backend.aggregation.provider.configuration;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.provider.configuration.api.MonitoringService;
import se.tink.backend.aggregation.provider.configuration.api.ProviderService;
import se.tink.backend.aggregation.provider.configuration.cluster.providers.ClusterIdProvider;
import se.tink.backend.aggregation.provider.configuration.controllers.ProviderServiceController;
import se.tink.backend.aggregation.provider.configuration.core.ProviderConfigurationDAO;
import se.tink.backend.aggregation.provider.configuration.http.resources.MonitoringServiceResource;
import se.tink.backend.aggregation.provider.configuration.http.resources.ProviderServiceResource;
import se.tink.backend.aggregation.provider.configuration.storage.ProviderConfigurationProvider;
import se.tink.libraries.metrics.MetricCollector;
import se.tink.libraries.metrics.MetricRegistry;

public class TestServiceModule extends AbstractModule {
    private static final Logger LOG = LoggerFactory.getLogger(TestServiceModule.class);

    @Override
    protected void configure() {
        bind(ProviderService.class).to(ProviderServiceResource.class).in(Scopes.SINGLETON);
        bind(MonitoringService.class).to(MonitoringServiceResource.class).in(Scopes.SINGLETON);
        bind(ClusterIdProvider.class).in(Scopes.SINGLETON);
        bind(ProviderServiceController.class).in(Scopes.SINGLETON);
        bind(ProviderConfigurationDAO.class)
                .to(ProviderConfigurationProvider.class)
                .in(Scopes.SINGLETON);

        bind(MetricCollector.class).in(Scopes.SINGLETON);
        bind(MetricRegistry.class).in(Scopes.SINGLETON);
    }
}
