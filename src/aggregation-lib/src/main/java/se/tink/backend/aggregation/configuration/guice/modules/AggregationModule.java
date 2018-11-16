package se.tink.backend.aggregation.configuration.guice.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import java.util.Objects;
import se.tink.backend.aggregation.aggregationcontroller.AggregationControllerAggregationClient;
import se.tink.backend.aggregation.api.AggregationService;
import se.tink.backend.aggregation.api.CreditSafeService;
import se.tink.backend.aggregation.cluster.jersey.JerseyClientApiKeyProvider;
import se.tink.backend.aggregation.cluster.jersey.JerseyClusterInfoProvider;
import se.tink.backend.aggregation.configuration.models.AggregationServiceConfiguration;
import se.tink.backend.aggregation.storage.database.providers.AggregatorInfoProvider;
import se.tink.backend.aggregation.storage.database.providers.ClientConfigurationProvider;
import se.tink.backend.aggregation.storage.database.providers.ControllerWrapperProvider;
import se.tink.backend.aggregation.storage.database.daos.CryptoConfigurationDao;
import se.tink.backend.aggregation.storage.database.providers.ClusterInfoProvider;
import se.tink.backend.aggregation.log.AggregationLoggerRequestFilter;
import se.tink.backend.aggregation.resources.AggregationServiceResource;
import se.tink.backend.aggregation.resources.CreditSafeServiceResource;
import se.tink.backend.aggregation.storage.debug.AgentDebugLocalStorage;
import se.tink.backend.aggregation.storage.debug.AgentDebugS3Storage;
import se.tink.backend.aggregation.storage.debug.AgentDebugStorageHandler;
import se.tink.backend.aggregation.workers.AgentWorker;
import se.tink.backend.aggregation.legacy.ServiceContext;
import se.tink.libraries.http.client.RequestTracingFilter;
import se.tink.libraries.jersey.guice.JerseyResourceRegistrar;
import se.tink.libraries.jersey.logging.AccessLoggingFilter;
import se.tink.libraries.jersey.logging.ResourceTimerFilterFactory;

public class AggregationModule extends AbstractModule {
    private final JerseyEnvironment jersey;
    private final AggregationServiceConfiguration configuration;

    AggregationModule(AggregationServiceConfiguration configuration, JerseyEnvironment jersey) {
        this.configuration = configuration;
        this.jersey = jersey;
    }

    @Override
    protected void configure() {
        bind(AggregationControllerAggregationClient.class).in(Scopes.SINGLETON);
        bind(AgentWorker.class).in(Scopes.SINGLETON);
        bind(ClusterInfoProvider.class).in(Scopes.SINGLETON);

        if (Objects.nonNull(configuration.getS3StorageConfiguration()) &&
                configuration.getS3StorageConfiguration().isEnabled()) {
            bind(AgentDebugStorageHandler.class).to(AgentDebugS3Storage.class).in(Scopes.SINGLETON);
        } else {
            bind(AgentDebugStorageHandler.class).to(AgentDebugLocalStorage.class).in(Scopes.SINGLETON);
        }

        bind(CryptoConfigurationDao.class).in(Scopes.SINGLETON);
        bind(ControllerWrapperProvider.class).in(Scopes.SINGLETON);
        bind(AggregatorInfoProvider.class).in(Scopes.SINGLETON);
        bind(ClientConfigurationProvider.class).in(Scopes.SINGLETON);

        // TODO Remove these lines after getting rid of dependencies on ServiceContext
        bind(ServiceContext.class).in(Scopes.SINGLETON);

        bind(AggregationService.class).to(AggregationServiceResource.class);
        bind(CreditSafeService.class).to(CreditSafeServiceResource.class);

        JerseyResourceRegistrar.build()
                .binder(binder())
                .jersey(jersey)
                .addFilterFactories(ResourceTimerFilterFactory.class)
                .addRequestFilters(AccessLoggingFilter.class, AggregationLoggerRequestFilter.class,
                        RequestTracingFilter.class)
                .addResponseFilters(AccessLoggingFilter.class, RequestTracingFilter.class)
                //This is not a resource, but a provider
                .addResources(
                        JerseyClusterInfoProvider.class,
                        JerseyClientApiKeyProvider.class,
                        AggregationService.class,
                        CreditSafeService.class,
                        JerseyClientApiKeyProvider.class
                )
                .bind();

    }
}
