package se.tink.backend.nasa.boot.configuration;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import se.tink.backend.nasa.metrics.MetricCollector;
import se.tink.backend.nasa.metrics.MetricRegistry;
import se.tink.backend.nasa.metrics.PrometheusConfiguration;
import se.tink.backend.nasa.metrics.PrometheusExportServer;

public class NASAServiceModule extends AbstractModule {
    private Configuration configuration;
    private SensitiveConfiguration sensitiveConfiguration;

    public NASAServiceModule(Configuration configuration, SensitiveConfiguration sensitiveConfiguration) {
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
