package se.tink.backend.aggregation.configuration.guice.modules;

import com.google.common.base.Predicate;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import java.util.Objects;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.aggregationcontroller.AggregationControllerAggregationClientImpl;
import se.tink.backend.aggregation.aggregationcontroller.iface.AggregationControllerAggregationClient;
import se.tink.backend.aggregation.api.AggregationService;
import se.tink.backend.aggregation.api.CreditSafeService;
import se.tink.backend.aggregation.api.MonitoringService;
import se.tink.backend.aggregation.client.provider_configuration.ProviderConfigurationService;
import se.tink.backend.aggregation.cluster.jersey.JerseyClientProvider;
import se.tink.backend.aggregation.configuration.models.AggregationServiceConfiguration;
import se.tink.backend.aggregation.log.AggregationLoggerRequestFilter;
import se.tink.backend.aggregation.resources.AggregationServiceResource;
import se.tink.backend.aggregation.resources.CreditSafeServiceResource;
import se.tink.backend.aggregation.resources.MonitoringServiceResource;
import se.tink.backend.aggregation.resources.ProviderConfigurationServiceResource;
import se.tink.backend.aggregation.storage.database.daos.CryptoConfigurationDao;
import se.tink.backend.aggregation.storage.database.providers.AggregatorInfoProvider;
import se.tink.backend.aggregation.storage.database.providers.ClientConfigurationProvider;
import se.tink.backend.aggregation.storage.database.providers.ControllerWrapperProvider;
import se.tink.backend.aggregation.storage.debug.AgentDebugLocalStorage;
import se.tink.backend.aggregation.storage.debug.AgentDebugS3Storage;
import se.tink.backend.aggregation.storage.debug.AgentDebugStorageHandler;
import se.tink.backend.aggregation.workers.worker.AgentWorker;
import se.tink.backend.aggregation.workers.worker.conditions.IsPrevGenProvider;
import se.tink.backend.aggregation.workers.worker.conditions.annotation.ShouldAddExtraCommands;
import se.tink.backend.integration.tpp_secrets_service.client.ManagedTppSecretsServiceClient;
import se.tink.backend.integration.tpp_secrets_service.client.TppSecretsServiceClientImpl;
import se.tink.libraries.http.client.LoggingFilter;
import se.tink.libraries.http.client.RequestTracingFilter;
import se.tink.libraries.jersey.guice.JerseyResourceRegistrar;
import se.tink.libraries.jersey.logging.AccessLoggingFilter;
import se.tink.libraries.jersey.logging.ResourceCounterFilterFactory;
import se.tink.libraries.jersey.logging.ResourceTimerFilterFactory;
import se.tink.libraries.tracing.jersey.filter.ServerTracingFilter;

public class AggregationModule extends AbstractModule {
    private final JerseyEnvironment jersey;
    private final AggregationServiceConfiguration configuration;

    AggregationModule(AggregationServiceConfiguration configuration, JerseyEnvironment jersey) {
        this.configuration = configuration;
        this.jersey = jersey;
    }

    @Override
    protected void configure() {
        bind(AggregationControllerAggregationClient.class)
                .to(AggregationControllerAggregationClientImpl.class);
        bind(AgentWorker.class).in(Scopes.SINGLETON);
        bind(ManagedTppSecretsServiceClient.class)
                .to(TppSecretsServiceClientImpl.class)
                .in(Scopes.SINGLETON);

        bind(ClientConfig.class).toInstance(new DefaultApacheHttpClient4Config());

        if (Objects.nonNull(configuration.getS3StorageConfiguration())
                && configuration.getS3StorageConfiguration().isEnabled()) {
            bind(AgentDebugStorageHandler.class).to(AgentDebugS3Storage.class).in(Scopes.SINGLETON);
        } else {
            bind(AgentDebugStorageHandler.class)
                    .to(AgentDebugLocalStorage.class)
                    .in(Scopes.SINGLETON);
        }

        bind(new TypeLiteral<Predicate<Provider>>() {})
                .annotatedWith(ShouldAddExtraCommands.class)
                .to(IsPrevGenProvider.class);

        bind(CryptoConfigurationDao.class).in(Scopes.SINGLETON);
        bind(ControllerWrapperProvider.class).in(Scopes.SINGLETON);
        bind(AggregatorInfoProvider.class).in(Scopes.SINGLETON);
        bind(ClientConfigurationProvider.class).in(Scopes.SINGLETON);

        bind(AggregationService.class).to(AggregationServiceResource.class).in(Scopes.SINGLETON);
        bind(CreditSafeService.class).to(CreditSafeServiceResource.class).in(Scopes.SINGLETON);
        bind(MonitoringService.class).to(MonitoringServiceResource.class).in(Scopes.SINGLETON);
        bind(ProviderConfigurationService.class)
                .to(ProviderConfigurationServiceResource.class)
                .in(Scopes.SINGLETON);

        JerseyResourceRegistrar.build()
                .binder(binder())
                .jersey(jersey)
                .addFilterFactories(ResourceTimerFilterFactory.class)
                .addFilterFactories(
                        ResourceTimerFilterFactory.class, ResourceCounterFilterFactory.class)
                .addRequestFilters(
                        AccessLoggingFilter.class,
                        AggregationLoggerRequestFilter.class,
                        RequestTracingFilter.class,
                        ServerTracingFilter.class,
                        LoggingFilter.class)
                .addResponseFilters(
                        LoggingFilter.class,
                        AccessLoggingFilter.class,
                        RequestTracingFilter.class,
                        ServerTracingFilter.class)
                // This is not a resource, but a provider
                .addResources(
                        AggregationService.class,
                        CreditSafeService.class,
                        MonitoringService.class,
                        JerseyClientProvider.class)
                .bind();
    }
}
