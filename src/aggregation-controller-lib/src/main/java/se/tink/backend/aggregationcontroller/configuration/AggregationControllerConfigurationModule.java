package se.tink.backend.aggregationcontroller.configuration;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;
import se.tink.backend.common.VersionInformation;
import se.tink.libraries.endpoints.EndpointConfiguration;
import se.tink.backend.guice.annotations.AggregationConfiguration;
import se.tink.libraries.discovery.CoordinationConfiguration;
import se.tink.libraries.metrics.HeapDumpGauge;
import se.tink.libraries.metrics.MetricCollector;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.PrometheusConfiguration;
import se.tink.libraries.metrics.PrometheusExportServer;

public class AggregationControllerConfigurationModule extends AbstractModule {
    private final AggregationControllerConfiguration configuration;

    public AggregationControllerConfigurationModule(AggregationControllerConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void configure() {
        bind(MetricRegistry.class).in(Scopes.SINGLETON);
        bind(VersionInformation.class).in(Scopes.SINGLETON);
        bind(MetricCollector.class).in(Scopes.SINGLETON);
        bind(PrometheusExportServer.class).in(Scopes.SINGLETON);
        bind(HeapDumpGauge.class).in(Scopes.SINGLETON);

        bind(CoordinationConfiguration.class).toInstance(configuration.getCoordination());
        bind(PrometheusConfiguration.class).toInstance(configuration.getPrometheus());
        bind(AggregationClusterConfiguration.class).toInstance(configuration.getAggregationCluster());

        bind(EndpointConfiguration.class).annotatedWith(AggregationConfiguration.class)
                .toProvider(Providers.of(configuration.getEndpoints().getAggregation()));

        bindConstant().annotatedWith(Names.named("clusterName")).to(configuration.getClusterName());
        bindConstant().annotatedWith(Names.named("clusterEnvironment")).to(configuration.getClusterEnvironment());
    }
}
