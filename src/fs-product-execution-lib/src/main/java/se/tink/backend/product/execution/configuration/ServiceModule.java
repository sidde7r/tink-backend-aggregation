package se.tink.backend.product.execution.configuration;

import com.google.common.collect.Queues;
import com.google.common.util.concurrent.RateLimiter;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import org.joda.time.LocalDate;
import se.tink.backend.common.client.SystemServiceFactoryProvider;
import se.tink.backend.common.concurrency.ListenableThreadPoolExecutor;
import se.tink.backend.common.concurrency.TypedThreadPoolBuilder;
import se.tink.backend.common.concurrency.WrappedRunnableListenableFutureTask;
import se.tink.backend.product.execution.unit.agents.CreateProductExecutor;
import se.tink.backend.product.execution.unit.agents.seb.mortgage.HttpClient;
import se.tink.backend.product.execution.unit.agents.seb.mortgage.JerseyClientWrapper;
import se.tink.backend.product.execution.unit.agents.seb.mortgage.ProductInformationGetRatesMapper;
import se.tink.backend.product.execution.unit.agents.seb.mortgage.ProductInformationGetRatesMapperImpl;
import se.tink.backend.product.execution.unit.agents.seb.mortgage.SEBCreateProductExecutor;
import se.tink.backend.product.execution.unit.agents.seb.mortgage.SEBMortgageApiClient;
import se.tink.backend.product.execution.unit.agents.seb.mortgage.SEBMortgageApiClientImpl;
import se.tink.backend.product.execution.unit.agents.seb.mortgage.SEBMortgageBankIdCollector;
import se.tink.backend.product.execution.unit.agents.seb.mortgage.SEBMortgageBankIdCollectorImpl;
import se.tink.backend.product.execution.unit.agents.seb.mortgage.mapping.ApplicationToLoanPostRequestMapper;
import se.tink.backend.product.execution.unit.agents.seb.mortgage.mapping.ApplicationToLoanPostRequestMapperImpl;
import se.tink.backend.product.execution.annotations.CurrentDate;
import se.tink.backend.product.execution.api.MonitoringService;
import se.tink.backend.product.execution.api.SBABProductExecutorService;
import se.tink.backend.product.execution.api.SEBProductExecutorService;
import se.tink.backend.product.execution.resources.MonitoringResource;
import se.tink.backend.product.execution.resources.SBABProductExecutorResource;
import se.tink.backend.product.execution.resources.SEBProductExecutorResource;
import se.tink.backend.product.execution.utils.RateLimitedCountdown;
import se.tink.backend.product.execution.utils.RateLimitedCountdownImpl;
import se.tink.backend.system.client.SystemServiceFactory;
import se.tink.libraries.jersey.guice.JerseyResourceRegistrar;
import se.tink.libraries.metrics.MetricCollector;
import se.tink.libraries.metrics.MetricRegistry;

public class ServiceModule extends AbstractModule {

    private final JerseyEnvironment environment;

    ServiceModule(JerseyEnvironment environment) {
        this.environment = environment;
    }

    @Override
    protected void configure() {
        bind(SEBProductExecutorService.class).to(SEBProductExecutorResource.class);
        bind(SBABProductExecutorService.class).to(SBABProductExecutorResource.class);
        bind(MonitoringService.class).to(MonitoringResource.class);

        bind(SystemServiceFactory.class).toProvider(SystemServiceFactoryProvider.class).in(Scopes.SINGLETON);

        // Countdown instances when injected for bank id looping
        bind(RateLimitedCountdown.class)
                .annotatedWith(Names.named("SEBMortgage.bankIdCollectCountdown"))
                .to(RateLimitedCountdownImpl.class);

        // Regular interface -> impl bindings
        bind(HttpClient.class).to(JerseyClientWrapper.class);
        bind(CreateProductExecutor.class).to(SEBCreateProductExecutor.class);
        bind(SEBMortgageApiClient.class).to(SEBMortgageApiClientImpl.class);
        bind(SEBMortgageBankIdCollector.class).to(SEBMortgageBankIdCollectorImpl.class);
        bind(ApplicationToLoanPostRequestMapper.class).to(ApplicationToLoanPostRequestMapperImpl.class);
        bind(ProductInformationGetRatesMapper.class).to(ProductInformationGetRatesMapperImpl.class);

        JerseyResourceRegistrar.build()
                .binder(binder())
                .jersey(environment)
                .addResources(SEBProductExecutorService.class)
                .addResources(SBABProductExecutorService.class)
                .addResources(MonitoringService.class)
                .bind();

        bind(MetricCollector.class).in(Scopes.SINGLETON);
        bind(MetricRegistry.class).in(Scopes.SINGLETON);
    }

    @Provides
    @Singleton
    @Named("product-executor")
    private ListenableThreadPoolExecutor<Runnable> createThreadPoolExecutor() {
        BlockingQueue<WrappedRunnableListenableFutureTask<Runnable, ?>> executorServiceQueue = Queues
                .newLinkedBlockingQueue();
        ThreadFactory executorServiceThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("product-executor-thread-%d")
                .build();

        return  ListenableThreadPoolExecutor.builder(
                executorServiceQueue,
                new TypedThreadPoolBuilder(10, executorServiceThreadFactory))
                .build();
    }

    @Provides
    @CurrentDate
    private LocalDate provideCurrentLocalDate() {
        return new LocalDate();
    }

    /**
     * Countdown configuration (60 * 0.5 reqs/sec -> min 120 seconds until countdown expiry)
     */
    @Provides
    @Named("RateLimitedCountdownImpl.rateLimiter")
    private RateLimiter provideCountdownRateLimiter() {
        return RateLimiter.create(0.5);
    }

    @Provides
    @Named("RateLimitedCountdownImpl.counts")
    private Integer provideCoundownCounts() {
        return 60;
    }

}