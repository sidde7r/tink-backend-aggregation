package se.tink.backend.aggregation.configuration.guice.modules;

import com.google.common.base.Predicate;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import java.util.Objects;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.aggregationcontroller.AggregationControllerAggregationClientImpl;
import se.tink.backend.aggregation.aggregationcontroller.InterClusterClientFactory;
import se.tink.backend.aggregation.aggregationcontroller.InterClusterClientProvider;
import se.tink.backend.aggregation.aggregationcontroller.iface.AggregationControllerAggregationClient;
import se.tink.backend.aggregation.api.AggregationService;
import se.tink.backend.aggregation.api.CreditSafeService;
import se.tink.backend.aggregation.api.MonitoringService;
import se.tink.backend.aggregation.client.provider_configuration.ProviderConfigurationService;
import se.tink.backend.aggregation.cluster.jersey.JerseyClientProvider;
import se.tink.backend.aggregation.configuration.models.AggregationServiceConfiguration;
import se.tink.backend.aggregation.eidasidentity.CertificateIdProvider;
import se.tink.backend.aggregation.eidasidentity.UnleashCertificateIdProvider;
import se.tink.backend.aggregation.log.AggregationLoggerRequestFilter;
import se.tink.backend.aggregation.resources.AggregationServiceResource;
import se.tink.backend.aggregation.resources.CreditSafeServiceResource;
import se.tink.backend.aggregation.resources.MonitoringServiceResource;
import se.tink.backend.aggregation.resources.ProviderConfigurationServiceResource;
import se.tink.backend.aggregation.storage.database.daos.CryptoConfigurationDao;
import se.tink.backend.aggregation.storage.database.providers.AggregatorInfoProvider;
import se.tink.backend.aggregation.storage.database.providers.ClientConfigurationProvider;
import se.tink.backend.aggregation.storage.database.providers.ControllerWrapperProvider;
import se.tink.backend.aggregation.storage.logs.AgentHttpLogsStorageHandler;
import se.tink.backend.aggregation.storage.logs.handlers.AgentHttpLogsLocalStorageHandler;
import se.tink.backend.aggregation.storage.logs.handlers.AgentHttpLogsS3StorageHandler;
import se.tink.backend.aggregation.workers.abort.DefaultRequestAbortHandler;
import se.tink.backend.aggregation.workers.abort.RequestAbortHandler;
import se.tink.backend.aggregation.workers.commands.exceptions.ExceptionProcessor;
import se.tink.backend.aggregation.workers.commands.exceptions.handlers.BankIdExceptionHandler;
import se.tink.backend.aggregation.workers.commands.exceptions.handlers.BankServiceExceptionHandler;
import se.tink.backend.aggregation.workers.commands.exceptions.handlers.CreditorValidationExceptionHandler;
import se.tink.backend.aggregation.workers.commands.exceptions.handlers.DateValidationExceptionHandler;
import se.tink.backend.aggregation.workers.commands.exceptions.handlers.DebtorValidationExceptionHandler;
import se.tink.backend.aggregation.workers.commands.exceptions.handlers.DefaultExceptionHandler;
import se.tink.backend.aggregation.workers.commands.exceptions.handlers.DuplicatePaymentExceptionHandler;
import se.tink.backend.aggregation.workers.commands.exceptions.handlers.ExceptionHandler;
import se.tink.backend.aggregation.workers.commands.exceptions.handlers.InsufficientFundsExceptionHandler;
import se.tink.backend.aggregation.workers.commands.exceptions.handlers.InterruptedExceptionHandler;
import se.tink.backend.aggregation.workers.commands.exceptions.handlers.PaymentAuthenticationExceptionHandler;
import se.tink.backend.aggregation.workers.commands.exceptions.handlers.PaymentAuthorizationCancelledByUserExceptionHandler;
import se.tink.backend.aggregation.workers.commands.exceptions.handlers.PaymentAuthorizationExceptionHandler;
import se.tink.backend.aggregation.workers.commands.exceptions.handlers.PaymentAuthorizationFailedByUserExceptionHandler;
import se.tink.backend.aggregation.workers.commands.exceptions.handlers.PaymentAuthorizationTimeOutExceptionHandler;
import se.tink.backend.aggregation.workers.commands.exceptions.handlers.PaymentCancelledExceptionHandler;
import se.tink.backend.aggregation.workers.commands.exceptions.handlers.PaymentExceptionHandler;
import se.tink.backend.aggregation.workers.commands.exceptions.handlers.PaymentPendingExceptionHandler;
import se.tink.backend.aggregation.workers.commands.exceptions.handlers.PaymentRejectedExceptionHandler;
import se.tink.backend.aggregation.workers.commands.exceptions.handlers.PaymentValidationExceptionHandler;
import se.tink.backend.aggregation.workers.commands.exceptions.handlers.ReferenceValidationExceptionHandler;
import se.tink.backend.aggregation.workers.commands.exceptions.handlers.TransferExecutionExceptionHandler;
import se.tink.backend.aggregation.workers.commands.payment.PaymentExecutionService;
import se.tink.backend.aggregation.workers.commands.payment.PaymentExecutionServiceImpl;
import se.tink.backend.aggregation.workers.commands.payment.executor.PaymentExecutorFactory;
import se.tink.backend.aggregation.workers.commands.payment.executor.PaymentExecutorFactoryImpl;
import se.tink.backend.aggregation.workers.operation.DefaultLockSupplier;
import se.tink.backend.aggregation.workers.operation.LockSupplier;
import se.tink.backend.aggregation.workers.worker.AgentWorker;
import se.tink.backend.aggregation.workers.worker.conditions.IsPrevGenProvider;
import se.tink.backend.aggregation.workers.worker.conditions.annotation.ShouldAddExtraCommands;
import se.tink.backend.integration.tpp_secrets_service.client.ManagedTppSecretsServiceInternalClient;
import se.tink.backend.integration.tpp_secrets_service.client.TppSecretsServiceInternalClientImpl;
import se.tink.libraries.http.client.HstsFilter;
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
        bind(InterClusterClientFactory.class).in(Scopes.SINGLETON);
        bind(InterClusterClientProvider.class).in(Scopes.SINGLETON);
        bind(AggregationControllerAggregationClient.class)
                .to(AggregationControllerAggregationClientImpl.class);
        bind(AgentWorker.class).in(Scopes.SINGLETON);
        bind(ManagedTppSecretsServiceInternalClient.class)
                .to(TppSecretsServiceInternalClientImpl.class)
                .in(Scopes.SINGLETON);
        bind(CertificateIdProvider.class)
                .to(UnleashCertificateIdProvider.class)
                .in(Scopes.SINGLETON);
        bind(ClientConfig.class).toInstance(new DefaultApacheHttpClient4Config());

        if (Objects.nonNull(configuration.getS3StorageConfiguration())
                && configuration.getS3StorageConfiguration().isEnabled()) {
            bind(AgentHttpLogsStorageHandler.class)
                    .to(AgentHttpLogsS3StorageHandler.class)
                    .in(Scopes.SINGLETON);
        } else {
            bind(AgentHttpLogsStorageHandler.class)
                    .to(AgentHttpLogsLocalStorageHandler.class)
                    .in(Scopes.SINGLETON);
        }

        bind(new TypeLiteral<Predicate<Provider>>() {})
                .annotatedWith(ShouldAddExtraCommands.class)
                .to(IsPrevGenProvider.class);

        bind(CryptoConfigurationDao.class).in(Scopes.SINGLETON);
        bind(ControllerWrapperProvider.class).in(Scopes.SINGLETON);
        bind(AggregatorInfoProvider.class).in(Scopes.SINGLETON);
        bind(ClientConfigurationProvider.class).in(Scopes.SINGLETON);

        bind(PaymentExecutorFactory.class)
                .to(PaymentExecutorFactoryImpl.class)
                .in(Scopes.SINGLETON);

        bind(PaymentExecutionService.class)
                .to(PaymentExecutionServiceImpl.class)
                .in(Scopes.SINGLETON);
        bind(AggregationService.class).to(AggregationServiceResource.class).in(Scopes.SINGLETON);
        bind(CreditSafeService.class).to(CreditSafeServiceResource.class).in(Scopes.SINGLETON);
        bind(MonitoringService.class).to(MonitoringServiceResource.class).in(Scopes.SINGLETON);
        bind(ProviderConfigurationService.class)
                .to(ProviderConfigurationServiceResource.class)
                .in(Scopes.SINGLETON);
        bind(RequestAbortHandler.class).to(DefaultRequestAbortHandler.class).in(Scopes.SINGLETON);
        bind(LockSupplier.class).to(DefaultLockSupplier.class).in(Scopes.SINGLETON);

        bind(ExceptionProcessor.class).in(Scopes.SINGLETON);
        Multibinder<ExceptionHandler> actionBinder =
                Multibinder.newSetBinder(binder(), ExceptionHandler.class);
        actionBinder.addBinding().to(BankIdExceptionHandler.class);
        actionBinder.addBinding().to(BankServiceExceptionHandler.class);
        actionBinder.addBinding().to(DefaultExceptionHandler.class);
        actionBinder.addBinding().to(InterruptedExceptionHandler.class);
        actionBinder.addBinding().to(TransferExecutionExceptionHandler.class);

        bindPaymentExceptionsHandlers(actionBinder);

        JerseyResourceRegistrar.build()
                .binder(binder())
                .jersey(jersey)
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
                        ServerTracingFilter.class,
                        HstsFilter.class)
                // This is not a resource, but a provider
                .addResources(
                        AggregationService.class,
                        CreditSafeService.class,
                        MonitoringService.class,
                        JerseyClientProvider.class)
                .bind();
    }

    private static void bindPaymentExceptionsHandlers(Multibinder<ExceptionHandler> actionBinder) {
        actionBinder.addBinding().to(CreditorValidationExceptionHandler.class);
        actionBinder.addBinding().to(DateValidationExceptionHandler.class);
        actionBinder.addBinding().to(DebtorValidationExceptionHandler.class);
        actionBinder.addBinding().to(DuplicatePaymentExceptionHandler.class);
        actionBinder.addBinding().to(InsufficientFundsExceptionHandler.class);
        actionBinder.addBinding().to(PaymentAuthenticationExceptionHandler.class);
        actionBinder.addBinding().to(PaymentAuthorizationCancelledByUserExceptionHandler.class);
        actionBinder.addBinding().to(PaymentAuthorizationExceptionHandler.class);
        actionBinder.addBinding().to(PaymentAuthorizationFailedByUserExceptionHandler.class);
        actionBinder.addBinding().to(PaymentAuthorizationTimeOutExceptionHandler.class);
        actionBinder.addBinding().to(PaymentCancelledExceptionHandler.class);
        actionBinder.addBinding().to(PaymentExceptionHandler.class);
        actionBinder.addBinding().to(PaymentPendingExceptionHandler.class);
        actionBinder.addBinding().to(PaymentRejectedExceptionHandler.class);
        actionBinder.addBinding().to(PaymentValidationExceptionHandler.class);
        actionBinder.addBinding().to(ReferenceValidationExceptionHandler.class);
    }
}
