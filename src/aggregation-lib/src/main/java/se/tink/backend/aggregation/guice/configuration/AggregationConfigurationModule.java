package se.tink.backend.aggregation.guice.configuration;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.common.config.AggregationDevelopmentConfiguration;
import se.tink.backend.common.config.CacheConfiguration;
import se.tink.backend.common.config.S3StorageConfiguration;
import se.tink.backend.aggregation.configurations.ServiceConfiguration;
import se.tink.backend.queue.sqs.configuration.SqsQueueConfiguration;
import se.tink.libraries.discovery.CoordinationConfiguration;
import se.tink.libraries.metrics.PrometheusConfiguration;

public class AggregationConfigurationModule extends AbstractModule {

    private final ServiceConfiguration configuration;

    public AggregationConfigurationModule(ServiceConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void configure() {
        bind(S3StorageConfiguration.class).toProvider(Providers.of(configuration.getS3StorageConfiguration()));

        bindConstant().annotatedWith(Names.named("developmentMode")).to(configuration.isDevelopmentMode());
        bindConstant().annotatedWith(Names.named("productionMode")).to(!configuration.isDevelopmentMode());
        bindConstant().annotatedWith(Names.named("queueAvailable"))
                .to(configuration.getSqsQueueConfiguration().isEnabled());
        bindConstant().annotatedWith(Names.named("isMultiClientDevelopment"))
                .to(configuration.isMultiClientDevelopment());

        // Tink monolith (common-lib and main-api) configurations
        bind(CacheConfiguration.class).toProvider(Providers.of(configuration.getCacheConfiguration()));
        bind(SqsQueueConfiguration.class).toProvider(Providers.of(configuration.getSqsQueueConfiguration()));
        bind(ServiceConfiguration.class).toInstance(configuration);
        bind(AgentsServiceConfiguration.class).toInstance(configuration.getAgentsServiceConfiguration());

        if (configuration.isDevelopmentMode() &&
                configuration.getDevelopmentConfiguration().isValid()) {
            bind(AggregationDevelopmentConfiguration.class).toProvider(
                    Providers.of(configuration.getDevelopmentConfiguration()));
        }

        // Tink public library configurations
        bind(CoordinationConfiguration.class).toProvider(Providers.of(configuration.getCoordination()));
        bind(PrometheusConfiguration.class).toInstance(configuration.getPrometheus());
    }

}
