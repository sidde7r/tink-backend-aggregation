package se.tink.backend.aggregation.configuration.guice.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;
import io.dropwizard.setup.Environment;
import java.util.Collections;
import java.util.Map;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.aggregationcontroller.AggregationControllerAggregationClient;
import se.tink.backend.aggregation.aggregationcontroller.AggregationControllerAggregationClientStub;
import se.tink.backend.aggregation.api.AggregationService;
import se.tink.backend.aggregation.api.CreditSafeService;
import se.tink.backend.aggregation.api.MonitoringService;
import se.tink.backend.aggregation.client.provider_configuration.ProviderConfigurationService;
import se.tink.backend.aggregation.cluster.jersey.JerseyClientProvider;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.models.AggregationServiceConfiguration;
import se.tink.backend.aggregation.configuration.models.CacheConfiguration;
import se.tink.backend.aggregation.configuration.models.ProviderConfigurationServiceConfiguration;
import se.tink.backend.aggregation.configuration.models.S3StorageConfiguration;
import se.tink.backend.aggregation.log.AggregationLoggerRequestFilter;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.agentcontext.factory.AgentContextProviderFactory;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.agentcontext.factory.AgentContextProviderFactoryImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.GeneratedValueProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.GeneratedValueProviderImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ActualLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGeneratorImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation.factory.SupplementalInformationProviderFactory;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation.factory.SupplementalInformationProviderFactoryImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient.factory.NextGenTinkHttpClientProviderFactory;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient.factory.TinkHttpClientProviderFactory;
import se.tink.backend.aggregation.resources.AggregationServiceResource;
import se.tink.backend.aggregation.resources.FakeCreditSafeService;
import se.tink.backend.aggregation.resources.FakeProviderConfigurationService;
import se.tink.backend.aggregation.resources.MonitoringServiceResource;
import se.tink.backend.aggregation.storage.database.daos.CryptoConfigurationDao;
import se.tink.backend.aggregation.storage.database.models.AggregatorConfiguration;
import se.tink.backend.aggregation.storage.database.models.ClientConfiguration;
import se.tink.backend.aggregation.storage.database.models.ClusterConfiguration;
import se.tink.backend.aggregation.storage.database.providers.AggregatorInfoProvider;
import se.tink.backend.aggregation.storage.database.providers.ClientConfigurationProvider;
import se.tink.backend.aggregation.storage.database.providers.ControllerWrapperProvider;
import se.tink.backend.aggregation.storage.database.repositories.CryptoConfigurationsRepository;
import se.tink.backend.aggregation.storage.debug.AgentDebugLocalStorage;
import se.tink.backend.aggregation.storage.debug.AgentDebugStorageHandler;
import se.tink.backend.aggregation.workers.AgentWorker;
import se.tink.backend.aggregation.workers.AgentWorkerOperation;
import se.tink.backend.aggregation.workers.commands.state.CircuitBreakerAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.commands.state.DebugAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.commands.state.InstantiateAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.commands.state.LoginAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.commands.state.ReportProviderMetricsAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.concurrency.InterProcessSemaphoreMutexFactory;
import se.tink.backend.aggregation.workers.concurrency.InterProcessSemaphoreMutexFactoryStub;
import se.tink.backend.integration.agent_data_availability_tracker.client.AgentDataAvailabilityTrackerClient;
import se.tink.backend.integration.agent_data_availability_tracker.client.AgentDataAvailabilityTrackerClientMockImpl;
import se.tink.backend.integration.agent_data_availability_tracker.client.configuration.AgentDataAvailabilityTrackerConfiguration;
import se.tink.backend.integration.tpp_secrets_service.client.ManagedTppSecretsServiceClient;
import se.tink.backend.integration.tpp_secrets_service.client.TppSecretsServiceClientImpl;
import se.tink.backend.integration.tpp_secrets_service.client.configuration.TppSecretsServiceConfiguration;
import se.tink.libraries.cache.CacheClient;
import se.tink.libraries.cache.FakeCacheClient;
import se.tink.libraries.concurrency.LockFactory;
import se.tink.libraries.discovery.CoordinationConfiguration;
import se.tink.libraries.discovery.FakeCuratorFramework;
import se.tink.libraries.endpoints.model.EndpointConfiguration;
import se.tink.libraries.event_producer_service_client.grpc.EventProducerServiceClient;
import se.tink.libraries.event_producer_service_client.grpc.EventProducerServiceClientProvider;
import se.tink.libraries.event_producer_service_client.grpc.EventProducerServiceEndpointConfiguration;
import se.tink.libraries.http.client.RequestTracingFilter;
import se.tink.libraries.jersey.guice.JerseyResourceRegistrar;
import se.tink.libraries.jersey.logging.AccessLoggingFilter;
import se.tink.libraries.jersey.logging.ResourceCounterFilterFactory;
import se.tink.libraries.jersey.logging.ResourceTimerFilterFactory;
import se.tink.libraries.metrics.collection.MetricCollector;
import se.tink.libraries.metrics.prometheus.PrometheusConfiguration;
import se.tink.libraries.metrics.prometheus.PrometheusExportServer;
import se.tink.libraries.metrics.registry.MeterFactory;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.metrics.types.other.HeapDumpGauge;
import se.tink.libraries.queue.QueueProducer;
import se.tink.libraries.queue.sqs.FakeHandler;
import se.tink.libraries.queue.sqs.FakeProducer;
import se.tink.libraries.queue.sqs.QueueMessageAction;
import se.tink.libraries.queue.sqs.configuration.SqsQueueConfiguration;
import se.tink.libraries.service.version.VersionInformation;

/**
 * A singular place for all the Guice bindings necessary to start up and make calls to the
 * Aggregation service without failures. By default, the aggregation service has a dependency on
 * many extra services and resources that would require setting up a complete Kubernetes cluster.
 * These have here been replaced with fake implementations.
 */
public class AggregationDecoupledModule extends AbstractModule {
    private static final Logger log = LoggerFactory.getLogger(AggregationDecoupledModule.class);

    private final AggregationServiceConfiguration configuration;
    private final Environment environment;

    public AggregationDecoupledModule(
            final AggregationServiceConfiguration configuration, final Environment environment) {
        this.configuration = configuration;
        this.environment = environment;
    }

    @Override
    protected void configure() {
        // AggregationCommonModule
        bind(CacheClient.class).toInstance(new FakeCacheClient());

        bind(LockFactory.class).in(Scopes.SINGLETON);
        bind(MeterFactory.class).in(Scopes.SINGLETON);
        bind(VersionInformation.class).in(Scopes.SINGLETON);

        bind(MetricCollector.class).in(Scopes.SINGLETON);
        bind(PrometheusExportServer.class).in(Scopes.SINGLETON);
        bind(MetricRegistry.class).in(Scopes.SINGLETON);
        bind(HeapDumpGauge.class).in(Scopes.SINGLETON);

        // CoordinationModule
        bind(CuratorFramework.class).to(FakeCuratorFramework.class);

        // AgentWorkerCommandModule
        bind(AgentWorkerOperation.AgentWorkerOperationState.class).in(Scopes.SINGLETON);
        bind(DebugAgentWorkerCommandState.class).in(Scopes.SINGLETON);
        bind(CircuitBreakerAgentWorkerCommandState.class).in(Scopes.SINGLETON);
        bind(InstantiateAgentWorkerCommandState.class).in(Scopes.SINGLETON);
        bind(LoginAgentWorkerCommandState.class).in(Scopes.SINGLETON);
        bind(ReportProviderMetricsAgentWorkerCommandState.class).in(Scopes.SINGLETON);

        // AggregationConfigurationModule
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

        bind(CacheConfiguration.class)
                .toProvider(Providers.of(configuration.getCacheConfiguration()));
        bind(SqsQueueConfiguration.class)
                .toProvider(Providers.of(configuration.getSqsQueueConfiguration()));
        bind(AggregationServiceConfiguration.class).toInstance(configuration);
        bind(AgentsServiceConfiguration.class)
                .toInstance(configuration.getAgentsServiceConfiguration());
        bind(TinkHttpClientProviderFactory.class).to(NextGenTinkHttpClientProviderFactory.class);
        bind(SupplementalInformationProviderFactory.class)
                .to(SupplementalInformationProviderFactoryImpl.class);
        bind(AgentContextProviderFactory.class).to(AgentContextProviderFactoryImpl.class);
        bind(RandomValueGenerator.class).to(RandomValueGeneratorImpl.class);
        bind(LocalDateTimeSource.class).to(ActualLocalDateTimeSource.class);
        bind(GeneratedValueProvider.class).to(GeneratedValueProviderImpl.class);
        bind(TppSecretsServiceConfiguration.class)
                .toInstance(
                        configuration
                                .getAgentsServiceConfiguration()
                                .getTppSecretsServiceConfiguration());
        bind(ProviderConfigurationServiceConfiguration.class)
                .toInstance(configuration.getProviderConfigurationServiceConfiguration());

        // Tink public library configurations
        bind(CoordinationConfiguration.class)
                .toProvider(Providers.of(configuration.getCoordination()));
        bind(PrometheusConfiguration.class).toInstance(configuration.getPrometheus());

        // AggregationModule
        bind(AggregationControllerAggregationClient.class)
                .to(AggregationControllerAggregationClientStub.class);
        bind(AgentWorker.class).in(Scopes.SINGLETON);
        bind(ManagedTppSecretsServiceClient.class)
                .to(TppSecretsServiceClientImpl.class)
                .in(Scopes.SINGLETON);
        bind(ClientConfig.class).toInstance(new DefaultApacheHttpClient4Config());
        bind(InterProcessSemaphoreMutexFactory.class)
                .to(InterProcessSemaphoreMutexFactoryStub.class);
        bind(AgentDebugStorageHandler.class).to(AgentDebugLocalStorage.class).in(Scopes.SINGLETON);

        bind(CryptoConfigurationsRepository.class)
                .toInstance(new FakeCryptoConfigurationsRepository(configuration));
        bind(CryptoConfigurationDao.class).in(Scopes.SINGLETON);
        bind(ControllerWrapperProvider.class).in(Scopes.SINGLETON);
        bind(AggregatorInfoProvider.class).in(Scopes.SINGLETON);
        bind(ClientConfigurationProvider.class).in(Scopes.SINGLETON);

        bind(AggregationService.class).to(AggregationServiceResource.class).in(Scopes.SINGLETON);
        bind(CreditSafeService.class).to(FakeCreditSafeService.class).in(Scopes.SINGLETON);
        bind(MonitoringService.class).to(MonitoringServiceResource.class).in(Scopes.SINGLETON);
        bind(ProviderConfigurationService.class)
                .to(FakeProviderConfigurationService.class)
                .in(Scopes.SINGLETON);

        JerseyResourceRegistrar.build()
                .binder(binder())
                .jersey(environment.jersey())
                .addFilterFactories(ResourceTimerFilterFactory.class)
                .addFilterFactories(
                        ResourceTimerFilterFactory.class, ResourceCounterFilterFactory.class)
                .addRequestFilters(
                        AccessLoggingFilter.class,
                        AggregationLoggerRequestFilter.class,
                        RequestTracingFilter.class)
                .addResponseFilters(AccessLoggingFilter.class, RequestTracingFilter.class)
                // This is not a resource, but a provider
                .addResources(
                        AggregationService.class,
                        CreditSafeService.class,
                        MonitoringService.class,
                        JerseyClientProvider.class)
                .bind();

        // AgentDataAvailabilityTrackerModule
        bind(AgentDataAvailabilityTrackerConfiguration.class)
                .toInstance(
                        configuration
                                .getAgentsServiceConfiguration()
                                .getAgentDataAvailabilityTrackerConfiguration());

        bind(AgentDataAvailabilityTrackerClient.class)
                .to(AgentDataAvailabilityTrackerClientMockImpl.class)
                .in(Scopes.SINGLETON);

        // QueueModule
        bind(QueueProducer.class).to(FakeProducer.class).in(Scopes.SINGLETON);
        bind(QueueMessageAction.class).to(FakeHandler.class).in(Scopes.SINGLETON);

        // EventProducerServiceClientModule
        bind(EndpointConfiguration.class)
                .annotatedWith(EventProducerServiceEndpointConfiguration.class)
                .toProvider(
                        Providers.of(
                                configuration
                                        .getEndpoints()
                                        .getEventProducerServiceConfiguration()));

        bind(EventProducerServiceClient.class).toProvider(EventProducerServiceClientProvider.class);
    }

    @Provides
    @Singleton
    @Named("clusterConfigurations")
    public Map<String, ClusterConfiguration> provideClusterConfigurations() {
        return Collections.singletonMap(
                configuration
                        .getDevelopmentConfiguration()
                        .getClusterConfiguration()
                        .getClusterId(),
                configuration.getDevelopmentConfiguration().getClusterConfiguration());
    }

    @Provides
    @Singleton
    @Named("aggregatorConfiguration")
    public Map<String, AggregatorConfiguration> providerAggregatorConfiguration() {
        return Collections.singletonMap(
                configuration
                        .getDevelopmentConfiguration()
                        .getAggregatorConfiguration()
                        .getAggregatorId(),
                configuration.getDevelopmentConfiguration().getAggregatorConfiguration());
    }

    @Provides
    @Singleton
    @Named("clientConfigurationByClientKey")
    public Map<String, ClientConfiguration> providerClientConfiguration() {
        return Collections.singletonMap(
                configuration
                        .getDevelopmentConfiguration()
                        .getClientConfiguration()
                        .getApiClientKey(),
                configuration.getDevelopmentConfiguration().getClientConfiguration());
    }

    @Provides
    @Singleton
    @Named("clientConfigurationByName")
    public Map<String, ClientConfiguration> providerClientConfigurationByName() {
        return Collections.emptyMap();
    }
}
