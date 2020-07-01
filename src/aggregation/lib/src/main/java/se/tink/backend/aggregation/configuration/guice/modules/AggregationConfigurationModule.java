package se.tink.backend.aggregation.configuration.guice.modules;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.configuration.models.AggregationDevelopmentConfiguration;
import se.tink.backend.aggregation.configuration.models.AggregationServiceConfiguration;
import se.tink.backend.aggregation.configuration.models.CacheConfiguration;
import se.tink.backend.aggregation.configuration.models.ProviderConfigurationServiceConfiguration;
import se.tink.backend.aggregation.configuration.models.ProviderTierConfiguration;
import se.tink.backend.aggregation.configuration.models.configuration.S3StorageConfiguration;
import se.tink.backend.aggregation.workers.concurrency.InterProcessSemaphoreMutexFactory;
import se.tink.backend.aggregation.workers.concurrency.InterProcessSemaphoreMutexFactoryImpl;
import se.tink.backend.integration.tpp_secrets_service.client.configuration.TppSecretsServiceConfiguration;
import se.tink.libraries.discovery.CoordinationConfiguration;
import se.tink.libraries.metrics.prometheus.PrometheusConfiguration;
import se.tink.libraries.queue.sqs.configuration.SqsQueueConfiguration;

public class AggregationConfigurationModule extends AbstractModule {

    private final AggregationServiceConfiguration configuration;

    public AggregationConfigurationModule(AggregationServiceConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void configure() {
        bind(S3StorageConfiguration.class)
                .toProvider(Providers.of(configuration.getS3StorageConfiguration()));

        bindConstant()
                .annotatedWith(Names.named("developmentMode"))
                .to(configuration.isDevelopmentMode());
        bindConstant()
                .annotatedWith(Names.named("productionMode"))
                .to(!configuration.isDevelopmentMode());
        bindConstant()
                .annotatedWith(Names.named("queueAvailable"))
                .to(configuration.getSqsQueueConfiguration().isEnabled());
        bindConstant()
                .annotatedWith(Names.named("isMultiClientDevelopment"))
                .to(configuration.isMultiClientDevelopment());
        bindConstant()
                .annotatedWith(Names.named("sendDataTrackingEvents"))
                .to(configuration.isSendDataTrackingEvents());
        bindConstant()
                .annotatedWith(Names.named("sendAgentLoginCompletedEvents"))
                .to(configuration.isSendAgentLoginCompletedEvents());
        bindConstant()
                .annotatedWith(Names.named("sendAgentRefreshEvents"))
                .to(configuration.isSendAgentRefreshEvents());

        bind(CacheConfiguration.class)
                .toProvider(Providers.of(configuration.getCacheConfiguration()));
        bind(SqsQueueConfiguration.class)
                .toProvider(Providers.of(configuration.getSqsQueueConfiguration()));
        bind(AggregationServiceConfiguration.class).toInstance(configuration);
        bind(ProviderTierConfiguration.class)
                .toInstance(configuration.getProviderTierConfiguration());
        bind(AgentsServiceConfiguration.class)
                .toInstance(configuration.getAgentsServiceConfiguration());

        bind(TppSecretsServiceConfiguration.class)
                .toInstance(
                        configuration
                                .getAgentsServiceConfiguration()
                                .getTppSecretsServiceConfiguration());
        bind(InterProcessSemaphoreMutexFactory.class)
                .to(InterProcessSemaphoreMutexFactoryImpl.class);
        bind(ProviderConfigurationServiceConfiguration.class)
                .toInstance(configuration.getProviderConfigurationServiceConfiguration());
        bind(EidasProxyConfiguration.class)
                .toInstance(configuration.getAgentsServiceConfiguration().getEidasProxy());

        if (configuration.isDevelopmentMode()
                && configuration.getDevelopmentConfiguration().isValid()) {
            bind(AggregationDevelopmentConfiguration.class)
                    .toProvider(Providers.of(configuration.getDevelopmentConfiguration()));
        }

        // Tink public library configurations
        bind(CoordinationConfiguration.class)
                .toProvider(Providers.of(configuration.getCoordination()));
        bind(PrometheusConfiguration.class).toInstance(configuration.getPrometheus());
    }
}
