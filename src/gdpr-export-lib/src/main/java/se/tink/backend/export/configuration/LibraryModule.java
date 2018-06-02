package se.tink.backend.export.configuration;

import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import org.joda.time.DateTime;
import se.tink.backend.categorization.api.AbnAmroCategories;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.categorization.api.SECategories;
import se.tink.backend.categorization.api.SebCategories;
import se.tink.backend.common.concurrency.ListenableThreadPoolExecutor;
import se.tink.backend.common.concurrency.TypedThreadPoolBuilder;
import se.tink.backend.common.concurrency.WrappedRunnableListenableFutureTask;
import se.tink.backend.guice.annotations.Now;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.metrics.HeapDumpGauge;
import se.tink.libraries.metrics.MeterFactory;
import se.tink.libraries.metrics.MetricCollector;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.PrometheusExportServer;

public class LibraryModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(MeterFactory.class).in(Scopes.SINGLETON);
        bind(MetricCollector.class).in(Scopes.SINGLETON);
        bind(PrometheusExportServer.class).in(Scopes.SINGLETON);
        bind(MetricRegistry.class).in(Scopes.SINGLETON);
        bind(HeapDumpGauge.class).in(Scopes.SINGLETON);
    }

    @Provides
    @Now
    public DateTime provideNowDateTime() {
        return DateTime.now();
    }

    @Provides
    @Now
    public Date provideNowDate() {
        return provideNowDateTime().toDate();
    }

    @Provides
    @Singleton
    @Named("executor")
    public ListenableThreadPoolExecutor<Runnable> provideApplicationExecutor(MetricRegistry metricRegistry) {
        BlockingQueue<WrappedRunnableListenableFutureTask<Runnable, ?>> executorServiceQueue = Queues
                .newLinkedBlockingQueue();
        ThreadFactory executorServiceThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("executor-service-thread-%d")
                .build();

        return ListenableThreadPoolExecutor.builder(
                executorServiceQueue,
                new TypedThreadPoolBuilder(20, executorServiceThreadFactory))
                .withMetric(metricRegistry, "executor_service")
                .build();
    }

    @Provides
    @Singleton
    public CategoryConfiguration provideCategoryConfiguration(Cluster cluster) {
        // TODO: Use different cluster-specific modules for this.
        switch (cluster) {
        case ABNAMRO:
            return new AbnAmroCategories();
        case CORNWALL:
            return new SebCategories();
        default:
            return new SECategories();
        }
    }

    @Provides
    @Singleton
    @Named("trackingExecutor")
    public ListenableThreadPoolExecutor<Runnable> provideTrackingExecutor(MetricRegistry metricRegistry) {
        BlockingQueue<WrappedRunnableListenableFutureTask<Runnable, ?>> executorServiceQueue = Queues
                .newLinkedBlockingQueue();
        ThreadFactory executorServiceThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("service-context-tracking-thread-%d")
                .build();

        return ListenableThreadPoolExecutor.builder(
                executorServiceQueue,
                new TypedThreadPoolBuilder(10, executorServiceThreadFactory))
                .withMetric(metricRegistry, "tracking_executor_service")
                .build();
    }

}
