package se.tink.backend.aggregation.configuration.guice.modules;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import io.prometheus.client.CollectorRegistry;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import net.spy.memcached.BinaryConnectionFactory;
import se.tink.backend.aggregation.configuration.models.CacheConfiguration;
import se.tink.libraries.cache.CacheClient;
import se.tink.libraries.cache.CacheInstrumentationDecorator;
import se.tink.libraries.cache.CacheReplicator;
import se.tink.libraries.cache.MemcacheClient;
import se.tink.libraries.cache.NonCachingCacheClient;
import se.tink.libraries.concurrency.LockFactory;
import se.tink.libraries.metrics.collection.MetricCollector;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.prometheus.PrometheusExportServer;
import se.tink.libraries.metrics.registry.MeterFactory;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.metrics.types.other.HeapDumpGauge;
import se.tink.libraries.service.version.VersionInformation;

@Slf4j
public class AggregationCommonModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(CacheClient.class).toProvider(CacheProvider.class).in(Scopes.SINGLETON);

        bind(LockFactory.class).in(Scopes.SINGLETON);
        bind(MeterFactory.class).in(Scopes.SINGLETON);
        bind(VersionInformation.class).in(Scopes.SINGLETON);

        bind(CollectorRegistry.class).toInstance(CollectorRegistry.defaultRegistry);
        bind(MetricCollector.class).in(Scopes.SINGLETON);
        bind(PrometheusExportServer.class).in(Scopes.SINGLETON);
        bind(MetricRegistry.class).in(Scopes.SINGLETON);
        bind(HeapDumpGauge.class).in(Scopes.SINGLETON);
    }

    private static class CacheProvider implements Provider<CacheClient> {
        private static final MetricId PRIMARY_METRIC_NAME =
                MetricId.newId("caches").label("hierarchy", "primary");
        private static final MetricId REPLICA_METRIC_NAME =
                MetricId.newId("caches").label("hierarchy", "replica");

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
                log.info("Non caching cache client created");
                return new NonCachingCacheClient();
            }

            CacheClient client;
            try {
                client = new MemcacheClient(new TinkConnectionFactory(), configuration.getHosts());
            } catch (IOException e) {
                throw new IllegalStateException("Could not create memcache client: " + e);
            }

            client = new CacheInstrumentationDecorator(client, registry, PRIMARY_METRIC_NAME);

            if (configuration.getReplicaHosts() != null
                    && !configuration.getReplicaHosts().isEmpty()) {

                final ArrayList<CacheClient> replicas =
                        Lists.newArrayListWithCapacity(configuration.getReplicaHosts().size());
                for (int i = 0; i < configuration.getReplicaHosts().size(); i++) {
                    try {
                        replicas.add(
                                new CacheInstrumentationDecorator(
                                        new MemcacheClient(
                                                new TinkConnectionFactory(),
                                                configuration.getReplicaHosts().get(i)),
                                        registry,
                                        REPLICA_METRIC_NAME.label(
                                                "replicaid", Integer.toString(i))));
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
