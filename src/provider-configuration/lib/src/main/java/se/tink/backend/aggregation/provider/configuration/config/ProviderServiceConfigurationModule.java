package se.tink.backend.aggregation.provider.configuration.config;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import se.tink.libraries.service.version.VersionInformation;
import se.tink.libraries.metrics.HeapDumpGauge;
import se.tink.libraries.metrics.MetricCollector;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.PrometheusConfiguration;
import se.tink.libraries.metrics.PrometheusExportServer;

public class ProviderServiceConfigurationModule extends AbstractModule {
    private final ProviderServiceConfiguration configuration;

    public ProviderServiceConfigurationModule(ProviderServiceConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void configure() {
        bind(MetricRegistry.class).in(Scopes.SINGLETON);
        bind(VersionInformation.class).in(Scopes.SINGLETON);
        bind(MetricCollector.class).in(Scopes.SINGLETON);
        bind(PrometheusExportServer.class).in(Scopes.SINGLETON);
        bind(HeapDumpGauge.class).in(Scopes.SINGLETON);

        bind(ProviderServiceConfiguration.class).toInstance(configuration);
        bind(PrometheusConfiguration.class).toInstance(configuration.getPrometheus());
    }
}
