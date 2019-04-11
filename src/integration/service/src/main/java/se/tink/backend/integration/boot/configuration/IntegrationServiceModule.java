package se.tink.backend.integration.boot.configuration;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import se.tink.libraries.metrics.MetricCollector;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.PrometheusConfiguration;
import se.tink.libraries.metrics.PrometheusExportServer;

public class IntegrationServiceModule extends AbstractModule {

    private Configuration configuration;
    private SensitiveConfiguration sensitiveConfiguration;

    public IntegrationServiceModule(
            Configuration configuration, SensitiveConfiguration sensitiveConfiguration) {
        this.configuration = configuration;
        this.sensitiveConfiguration = sensitiveConfiguration;
    }

    @Override
    protected void configure() {
        bind(Configuration.class).toInstance(configuration);
        bind(SensitiveConfiguration.class).toInstance(sensitiveConfiguration);

        bind(MetricCollector.class).in(Scopes.SINGLETON);
        bind(MetricRegistry.class).in(Scopes.SINGLETON);
        bind(PrometheusExportServer.class).in(Scopes.SINGLETON);
        bind(PrometheusConfiguration.class).toInstance(() -> 9130);
    }
}
