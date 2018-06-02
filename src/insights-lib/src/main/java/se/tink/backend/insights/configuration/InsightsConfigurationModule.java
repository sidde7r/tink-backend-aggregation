package se.tink.backend.insights.configuration;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;
import java.util.concurrent.TimeUnit;
import se.tink.backend.common.config.AuthenticationConfiguration;
import se.tink.backend.common.config.CacheConfiguration;
import se.tink.backend.common.config.DistributedDatabaseConfiguration;
import se.tink.backend.guice.annotations.AggregationControllerConfiguration;
import se.tink.libraries.cluster.Cluster;
import se.tink.backend.guice.configuration.ProviderCacheConfiguration;
import se.tink.libraries.discovery.CoordinationConfiguration;
import se.tink.libraries.endpoints.EndpointConfiguration;
import se.tink.libraries.metrics.PrometheusConfiguration;
import se.tink.libraries.endpoints.EndpointsConfiguration;

public class InsightsConfigurationModule extends AbstractModule {

    private final ActionableInsightsConfiguration configuration;

    public InsightsConfigurationModule(ActionableInsightsConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void configure() {
        bindConstant().annotatedWith(Names.named("distributedBatchSize"))
                .to(configuration.getDistributedDatabase().getBatchSize());
        bindConstant().annotatedWith(Names.named("developmentMode")).to(true);
        bindConstant().annotatedWith(Names.named("productionMode")).to(false);
        bindConstant().annotatedWith(Names.named("isProvidersOnAggregation"))
                .to(configuration.isProvidersOnAggregation());

        bind(EndpointConfiguration.class).annotatedWith(AggregationControllerConfiguration.class)
                .toProvider(Providers.of(configuration.getEndpoints().getAggregationcontroller()));

        bind(EndpointsConfiguration.class).toProvider(Providers.of(configuration.getEndpoints()));
        bind(CacheConfiguration.class).toProvider(Providers.of(configuration.getCache()));
        bind(Cluster.class).toInstance(configuration.getCluster());
        bind(CoordinationConfiguration.class).toInstance(configuration.getCoordination());
        bind(DistributedDatabaseConfiguration.class).toProvider(Providers.of(configuration.getDistributedDatabase()));
        bind(PrometheusConfiguration.class).toInstance(configuration.getPrometheus());
        bind(ProviderCacheConfiguration.class).toInstance(new ProviderCacheConfiguration(5, TimeUnit.MINUTES));
        bind(AuthenticationConfiguration.class).toInstance(configuration.getAuthentication());
    }
}
