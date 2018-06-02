package se.tink.backend.guice.configuration;

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
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import net.spy.memcached.AddrUtil;
import net.spy.memcached.BinaryConnectionFactory;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.joda.time.DateTime;
import se.tink.backend.categorization.api.AbnAmroCategories;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.categorization.api.SECategories;
import se.tink.backend.categorization.api.SebCategories;
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
import se.tink.backend.common.config.CacheConfiguration;
import se.tink.backend.common.config.SearchConfiguration;
import se.tink.backend.common.search.SearchProxy;
import se.tink.backend.common.tracking.intercom.IntercomTracker;
import se.tink.libraries.cluster.Cluster;
import se.tink.backend.guice.annotations.Now;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.metrics.HeapDumpGauge;
import se.tink.libraries.metrics.MeterFactory;
import se.tink.libraries.metrics.MetricCollector;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.PrometheusExportServer;

public class CommonModule extends AbstractModule {
    private static final LogUtils log = new LogUtils(CommonModule.class);

    @Override
    protected void configure() {
        bind(CacheClient.class).toProvider(CacheProvider.class).in(Scopes.SINGLETON);

        bind(LockFactory.class).in(Scopes.SINGLETON);
        bind(MeterFactory.class).in(Scopes.SINGLETON);
        bind(VersionInformation.class).in(Scopes.SINGLETON);
        bind(IntercomTracker.class).in(Scopes.SINGLETON);

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
    public Client provideSearchClient(SearchConfiguration configuration) {
        if (configuration == null || !configuration.isEnabled()) {
            return null;
        }

        Settings settings = ImmutableSettings.settingsBuilder()
                .put("cluster.name", configuration.getClusterName())
                .put("client.transport.sniff", configuration.useHostSniffing())
                .put("client.transport.ping_timeout", "20s")
                .build();

        List<InetSocketAddress> addresses = AddrUtil.getAddresses(configuration.getHosts());

        TransportClient client = new TransportClient(settings);

        for (InetSocketAddress address : addresses) {
            client.addTransportAddress(new InetSocketTransportAddress(address.getHostName(), address.getPort()));
        }

        SearchProxy.getInstance().setClient(client);

        return client;
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
