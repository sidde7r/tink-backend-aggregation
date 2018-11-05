package se.tink.backend.aggregation.guice.configuration;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import java.util.Objects;
import se.tink.backend.aggregation.aggregationcontroller.AggregationControllerAggregationClient;
import se.tink.backend.aggregation.api.AggregationService;
import se.tink.backend.aggregation.api.CreditSafeService;
import se.tink.backend.aggregation.clients.ProviderServiceFactoryProvider;
import se.tink.backend.aggregation.cluster.JerseyClusterInfoProvider;
import se.tink.backend.aggregation.cluster.provider.ClusterInfoProvider;
import se.tink.backend.aggregation.configurations.AggregationConfigurations;
import se.tink.backend.aggregation.configurations.ConfigurationsDao;
import se.tink.backend.aggregation.log.AggregationLoggerRequestFilter;
import se.tink.backend.aggregation.provider.configuration.client.InterContainerProviderServiceFactory;
import se.tink.backend.aggregation.resources.AggregationServiceResource;
import se.tink.backend.aggregation.resources.CreditSafeServiceResource;
import se.tink.backend.aggregation.storage.AgentDebugLocalStorage;
import se.tink.backend.aggregation.storage.AgentDebugS3Storage;
import se.tink.backend.aggregation.storage.AgentDebugStorageHandler;
import se.tink.backend.aggregation.workers.AgentWorker;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.libraries.jersey.guice.JerseyResourceRegistrar;
import se.tink.libraries.jersey.logging.AccessLoggingFilter;
import se.tink.libraries.jersey.logging.ResourceTimerFilterFactory;

public class AggregationModule extends AbstractModule {
    private final JerseyEnvironment jersey;
    private final ServiceConfiguration configuration;

    AggregationModule(ServiceConfiguration configuration, JerseyEnvironment jersey) {
        this.configuration = configuration;
        this.jersey = jersey;
    }

    @Override
    protected void configure() {
        bind(AggregationControllerAggregationClient.class).in(Scopes.SINGLETON);
        bind(InterContainerProviderServiceFactory.class).toProvider(ProviderServiceFactoryProvider.class).in(Scopes.SINGLETON);
        bind(AgentWorker.class).in(Scopes.SINGLETON);
        bind(ClusterInfoProvider.class).in(Scopes.SINGLETON);

        if (Objects.nonNull(configuration.getS3StorageConfiguration()) &&
                configuration.getS3StorageConfiguration().isEnabled()) {
            bind(AgentDebugStorageHandler.class).to(AgentDebugS3Storage.class).in(Scopes.SINGLETON);
        } else {
            bind(AgentDebugStorageHandler.class).to(AgentDebugLocalStorage.class).in(Scopes.SINGLETON);
        }

        if ( configuration.isMultiClientDevelopment()) {
            bind(ConfigurationsDao.class).to(AggregationConfigurations.class).in(Scopes.SINGLETON);
        }

        // TODO Remove these lines after getting rid of dependencies on ServiceContext
        bind(ServiceContext.class).in(Scopes.SINGLETON);

        bind(AggregationService.class).to(AggregationServiceResource.class);
        bind(CreditSafeService.class).to(CreditSafeServiceResource.class);

        JerseyResourceRegistrar.build()
                .binder(binder())
                .jersey(jersey)
                .addFilterFactories(ResourceTimerFilterFactory.class)
                .addRequestFilters(AccessLoggingFilter.class, AggregationLoggerRequestFilter.class)
                .addResponseFilters(AccessLoggingFilter.class)
                //This is not a resource, but a provider
                .addResources(
                        JerseyClusterInfoProvider.class,
                        AggregationService.class,
                        CreditSafeService.class
                )
                .bind();

    }
}
