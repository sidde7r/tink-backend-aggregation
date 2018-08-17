package se.tink.backend.guice.configuration;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;
import se.tink.backend.common.config.CacheConfiguration;
import se.tink.backend.common.config.GrpcConfiguration;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.guice.annotations.AggregationConfiguration;
import se.tink.backend.guice.annotations.AggregationControllerConfiguration;
import se.tink.backend.guice.annotations.EncryptionConfiguration;
import se.tink.backend.guice.annotations.MainConfiguration;
import se.tink.backend.guice.annotations.SystemConfiguration;
import se.tink.backend.guice.annotations.ProviderConfiguration;
import se.tink.libraries.abnamro.config.AbnAmroConfiguration;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.discovery.CoordinationConfiguration;
import se.tink.libraries.endpoints.EndpointConfiguration;
import se.tink.libraries.endpoints.EndpointsConfiguration;
import se.tink.libraries.metrics.PrometheusConfiguration;

public class ConfigurationModule extends AbstractModule {

    private final ServiceConfiguration configuration;

    public ConfigurationModule(ServiceConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void configure() {
        bind(EndpointConfiguration.class).annotatedWith(SystemConfiguration.class)
                .toProvider(Providers.of(configuration.getEndpoints().getSystem()));
        bind(EndpointConfiguration.class).annotatedWith(MainConfiguration.class)
                .toProvider(Providers.of(configuration.getEndpoints().getMain()));
        bind(EndpointConfiguration.class).annotatedWith(AggregationConfiguration.class)
                .toProvider(Providers.of(configuration.getEndpoints().getAggregation()));
        bind(EndpointConfiguration.class).annotatedWith(EncryptionConfiguration.class)
                .toProvider(Providers.of(configuration.getEndpoints().getEncryption()));
        bind(EndpointConfiguration.class).annotatedWith(Names.named("CategorizerConfiguration"))
                .toProvider(Providers.of(configuration.getEndpoints().getCategorization()));
        bind(EndpointConfiguration.class).annotatedWith(AggregationControllerConfiguration.class)
                .toProvider(Providers.of(configuration.getEndpoints().getAggregationcontroller()));
        bind(EndpointConfiguration.class).annotatedWith(ProviderConfiguration.class)
                .toProvider(Providers.of(configuration.getEndpoints().getProviderConfiguration()));
        bind(EndpointsConfiguration.class).toProvider(Providers.of(configuration.getEndpoints()));

        bindConstant().annotatedWith(Names.named("developmentMode")).to(configuration.isDevelopmentMode());
        bindConstant().annotatedWith(Names.named("productionMode")).to(!configuration.isDevelopmentMode());
        bindConstant().annotatedWith(Names.named("useAggregationController"))
                .to(configuration.isUseAggregationController());
        bindConstant().annotatedWith(Names.named("isAggregationCluster"))
                .to(configuration.isAggregationCluster());
        bindConstant().annotatedWith(Names.named("isProvidersOnAggregation"))
                .to(configuration.isProvidersOnAggregation());
        bindConstant().annotatedWith(Names.named("clusterName")).to(configuration.getClusterName());
        bindConstant().annotatedWith(Names.named("clusterEnvironment")).to(configuration.getClusterEnvironment());

        // Tink monolith (common-lib and main-api) configurations
        bind(AbnAmroConfiguration.class).toProvider(Providers.of(configuration.getAbnAmro()));
        bind(CacheConfiguration.class).toProvider(Providers.of(configuration.getCacheConfiguration()));
        bind(Cluster.class).toInstance(configuration.getCluster());
        bind(ServiceConfiguration.class).toInstance(configuration);
        bind(GrpcConfiguration.class).toInstance(configuration.getGrpc());

        // Tink public library configurations
        bind(CoordinationConfiguration.class).toProvider(Providers.of(configuration.getCoordination()));
        bind(PrometheusConfiguration.class).toInstance(configuration.getPrometheus());
    }

}
