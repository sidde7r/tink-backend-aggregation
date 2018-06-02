package se.tink.backend.insights.configuration;

import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import se.tink.backend.common.concurrency.ListenableThreadPoolExecutor;
import se.tink.backend.common.concurrency.TypedThreadPoolBuilder;
import se.tink.backend.common.concurrency.WrappedRunnableListenableFutureTask;
import se.tink.backend.insights.accounts.AccountQueryService;
import se.tink.backend.insights.accounts.AccountQueryServiceImpl;
import se.tink.backend.insights.app.CommandGateway;
import se.tink.backend.insights.app.CommandHandler;
import se.tink.backend.insights.app.GeneratorsProvider;
import se.tink.backend.insights.app.GeneratorsProviderImpl;
import se.tink.backend.insights.app.queryservices.CategoryQueryService;
import se.tink.backend.insights.app.queryservices.CategoryQueryServiceImpl;
import se.tink.backend.insights.app.queryservices.InsightQueryService;
import se.tink.backend.insights.app.queryservices.StatisticsQueryService;
import se.tink.backend.insights.identity.IdentityQueryService;
import se.tink.backend.insights.identity.IdentityQueryServiceImpl;
import se.tink.backend.insights.transactions.TransactionQueryService;
import se.tink.backend.insights.http.InsightService;
import se.tink.backend.insights.http.InsightServiceResource;
import se.tink.backend.insights.http.InsightsMonitoringService;
import se.tink.backend.insights.http.InsightsMonitoringServiceResource;
import se.tink.backend.insights.queryservice.InsightQueryServiceImpl;
import se.tink.backend.insights.queryservice.relevance.RelevanceEngine;
import se.tink.backend.insights.queryservice.relevance.RelevanceEngineImpl;
import se.tink.backend.insights.transactions.TransactionQueryServiceImpl;
import se.tink.backend.insights.transfer.TransferQueryService;
import se.tink.backend.insights.transfer.TransferQueryServiceImpl;
import se.tink.backend.insights.user.UserQueryService;
import se.tink.backend.insights.user.UserQueryServiceImpl;
import se.tink.libraries.jersey.guice.JerseyResourceRegistrar;
import se.tink.libraries.metrics.MetricRegistry;

public class InsightsServiceModule extends AbstractModule {

    private final JerseyEnvironment environment;

    InsightsServiceModule(JerseyEnvironment environment) {
        this.environment = environment;
    }

    @Override
    protected void configure() {

        bind(InsightQueryService.class).to(InsightQueryServiceImpl.class);
        bind(TransactionQueryService.class).to(TransactionQueryServiceImpl.class);
        bind(IdentityQueryService.class).to(IdentityQueryServiceImpl.class);
        bind(UserQueryService.class).to(UserQueryServiceImpl.class);
        bind(AccountQueryService.class).to(AccountQueryServiceImpl.class);
        bind(TransferQueryService.class).to(TransferQueryServiceImpl.class);
        bind(CategoryQueryService.class).to(CategoryQueryServiceImpl.class);
        bind(StatisticsQueryService.class).in(Scopes.SINGLETON);
        bind(CommandGateway.class).to(CommandHandler.class);
        bind(RelevanceEngine.class).to(RelevanceEngineImpl.class);


        bind(GeneratorsProvider.class).to(GeneratorsProviderImpl.class);
        bind(InsightService.class).to(InsightServiceResource.class);
        bind(InsightsMonitoringService.class).to(InsightsMonitoringServiceResource.class);

        JerseyResourceRegistrar.build()
                .binder(binder())
                .jersey(environment)
                .addResources(
                        InsightsMonitoringService.class,
                        InsightService.class)
                .bind();
    }


    @Provides
    @Singleton
    @Named("insightsExecutor")
    public ListenableThreadPoolExecutor<Runnable> provideInsightsExecutor(MetricRegistry metricRegistry) {
        BlockingQueue<WrappedRunnableListenableFutureTask<Runnable, ?>> executorServiceQueue = Queues
                .newLinkedBlockingQueue();
        ThreadFactory executorServiceThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("insights-executor-thread-%d")
                .build();

        return ListenableThreadPoolExecutor.builder(
                executorServiceQueue,
                new TypedThreadPoolBuilder(10, executorServiceThreadFactory))
                .withMetric(metricRegistry, "insights_executor_service")
                .build();
    }
}
