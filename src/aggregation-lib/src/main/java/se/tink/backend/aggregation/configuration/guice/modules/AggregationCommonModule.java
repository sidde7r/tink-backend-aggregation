package se.tink.backend.aggregation.configuration.guice.modules;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import net.spy.memcached.BinaryConnectionFactory;
import se.tink.backend.common.VersionInformation;
import se.tink.backend.common.cache.CacheClient;
import se.tink.backend.common.cache.CacheInstrumentationDecorator;
import se.tink.backend.common.cache.CacheReplicator;
import se.tink.backend.common.cache.MemcacheClient;
import se.tink.backend.common.cache.NonCachingCacheClient;
import se.tink.backend.common.concurrency.ListenableThreadPoolExecutor;
import se.tink.backend.common.concurrency.LockFactory;
import se.tink.backend.common.concurrency.TypedThreadPoolBuilder;
import se.tink.backend.common.concurrency.WrappedRunnableListenableFutureTask;
import se.tink.backend.aggregation.configuration.CacheConfiguration;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.metrics.HeapDumpGauge;
import se.tink.libraries.metrics.MeterFactory;
import se.tink.libraries.metrics.MetricCollector;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.PrometheusExportServer;

public class AggregationCommonModule extends AbstractModule {
    private static final LogUtils log = new LogUtils(AggregationCommonModule.class);

    @Override
    protected void configure() {
        bind(CacheClient.class).toProvider(CacheProvider.class).in(Scopes.SINGLETON);

        bind(LockFactory.class).in(Scopes.SINGLETON);
        bind(MeterFactory.class).in(Scopes.SINGLETON);
        bind(VersionInformation.class).in(Scopes.SINGLETON);

        bind(MetricCollector.class).in(Scopes.SINGLETON);
        bind(PrometheusExportServer.class).in(Scopes.SINGLETON);
        bind(MetricRegistry.class).in(Scopes.SINGLETON);
        bind(HeapDumpGauge.class).in(Scopes.SINGLETON);
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

    private static class CacheProvider implements Provider<CacheClient> {
        private static final MetricId PRIMARY_METRIC_NAME = MetricId.newId("caches")
                .label("hierarchy", "primary");
        private static final MetricId REPLICA_METRIC_NAME = MetricId.newId("caches")
                .label("hierarchy", "replica");

        private final CacheConfiguration configuration;
        private final MetricRegistry registry;

        @Inject
        public CacheProvider(@Nullable CacheConfiguration configuration, MetricRegistry registry) {
            this.configuration = configuration;
            this.registry = registry;
        }

        @Override
        public CacheClient get() {
            if (configuration == null || !configuration.isEnabled()) {
                return new NonCachingCacheClient();
            }

            CacheClient client;
            try {
                client = new MemcacheClient(new TinkConnectionFactory(), configuration.getHosts());
            } catch (IOException e) {
                log.error("Could not create memcache client", e);
                client = new NonCachingCacheClient();
            }

            client = new CacheInstrumentationDecorator(client, registry, PRIMARY_METRIC_NAME);

            if (configuration.getReplicaHosts() != null && !configuration.getReplicaHosts().isEmpty()) {

                final ArrayList<CacheClient> replicas = Lists.newArrayListWithCapacity(configuration
                        .getReplicaHosts().size());
                for (int i = 0; i < configuration.getReplicaHosts().size(); i++) {
                    try {
                        replicas.add(new CacheInstrumentationDecorator(
                                new MemcacheClient(new TinkConnectionFactory(), configuration
                                        .getReplicaHosts().get(i)),
                                registry, REPLICA_METRIC_NAME.label("replicaid", Integer.toString(i))));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                client = new CacheReplicator(client, replicas);
            }

            return client;
        }

        private class TinkConnectionFactory extends BinaryConnectionFactory {

            @Override
            public long getOperationTimeout() {
                return TimeUnit.SECONDS.toMillis(20);
            }

        }

    }
}
