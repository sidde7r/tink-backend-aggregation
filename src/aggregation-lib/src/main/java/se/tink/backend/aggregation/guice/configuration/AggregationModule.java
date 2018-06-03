package se.tink.backend.aggregation.guice.configuration;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.grpc.BindableService;
import se.tink.backend.aggregation.aggregationcontroller.AggregationControllerAggregationClient;
import se.tink.backend.aggregation.api.ProviderService;
import se.tink.backend.aggregation.client.AggregationServiceFactory;
import se.tink.backend.aggregation.client.InProcessAggregationServiceFactory;
import se.tink.backend.aggregation.controllers.ProviderServiceController;
import se.tink.backend.aggregation.grpc.CredentialGrpcTransport;
import se.tink.backend.aggregation.grpc.GrpcServer;
import se.tink.backend.aggregation.grpc.KeepAliveGrpcTransport;
import se.tink.backend.aggregation.grpc.ProductGrpcTransport;
import se.tink.backend.aggregation.grpc.RefreshGrpcTransport;
import se.tink.backend.aggregation.grpc.TransferGrpcTransport;
import se.tink.backend.aggregation.log.AggregationLoggerRequestFilter;
import se.tink.backend.aggregation.resources.ProviderServiceResource;
import se.tink.backend.client.ServiceFactory;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.client.EncryptionServiceFactoryProvider;
import se.tink.backend.common.client.ServiceFactoryProvider;
import se.tink.backend.common.client.SystemServiceFactoryProvider;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.encryption.client.EncryptionServiceFactory;
import se.tink.backend.system.client.SystemServiceFactory;
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
        bind(ServiceFactory.class).toProvider(ServiceFactoryProvider.class).in(Scopes.SINGLETON);
        bind(SystemServiceFactory.class).toProvider(SystemServiceFactoryProvider.class).in(Scopes.SINGLETON);
        bind(AggregationServiceFactory.class).to(InProcessAggregationServiceFactory.class).in(Scopes.SINGLETON);
        bind(EncryptionServiceFactory.class).toProvider(EncryptionServiceFactoryProvider.class).in(Scopes.SINGLETON);
        bind(AggregationControllerAggregationClient.class).in(Scopes.SINGLETON);

        bind(ProviderService.class).to(ProviderServiceResource.class).in(Scopes.SINGLETON);
        bind(ProviderServiceController.class).in(Scopes.SINGLETON);

        bindConstant().annotatedWith(Names.named("gRpcPort")).to(configuration.getGrpc().getPort());
        bind(GrpcServer.class).in(Scopes.SINGLETON);

        Multibinder<BindableService> grpcMultibinder = Multibinder.newSetBinder(binder(), BindableService.class,
                Names.named("grpcServices"));
        grpcMultibinder.addBinding().to(CredentialGrpcTransport.class).in(Scopes.SINGLETON);
        grpcMultibinder.addBinding().to(CredentialGrpcTransport.class).in(Scopes.SINGLETON);
        grpcMultibinder.addBinding().to(TransferGrpcTransport.class).in(Scopes.SINGLETON);
        grpcMultibinder.addBinding().to(ProductGrpcTransport.class).in(Scopes.SINGLETON);
        grpcMultibinder.addBinding().to(RefreshGrpcTransport.class).in(Scopes.SINGLETON);
        grpcMultibinder.addBinding().to(KeepAliveGrpcTransport.class).in(Scopes.SINGLETON);

        // TODO Remove these lines after getting rid of dependencies on ServiceContext
        bind(ServiceContext.class).in(Scopes.SINGLETON);

        JerseyResourceRegistrar.build()
                .binder(binder())
                .jersey(jersey)
                .addFilterFactories(ResourceTimerFilterFactory.class)
                .addRequestFilters(AccessLoggingFilter.class, AggregationLoggerRequestFilter.class)
                .addResponseFilters(AccessLoggingFilter.class)
                .addResources(ProviderService.class)
                .bind();
    }
}
