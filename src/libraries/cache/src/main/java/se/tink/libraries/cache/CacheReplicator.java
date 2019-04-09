package se.tink.libraries.cache;

import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import se.tink.libraries.concurrency.ListenableThreadPoolExecutor;
import se.tink.libraries.concurrency.TypedThreadPoolBuilder;
import se.tink.libraries.executor.ExecutorServiceUtils;

public class CacheReplicator implements CacheClient {
    private static final ThreadFactory THREAD_FACTORY =
            new ThreadFactoryBuilder().setNameFormat("cache-replicator-%d").build();
    private static final TypedThreadPoolBuilder THREAD_POOL_BUILDER =
            new TypedThreadPoolBuilder(1, THREAD_FACTORY)
                    .withMaximumPoolSize(50, 1L, TimeUnit.MINUTES);

    private final ListenableThreadPoolExecutor<Runnable> asyncExecutor =
            ListenableThreadPoolExecutor.builder(
                            Queues.newArrayBlockingQueue(1000), THREAD_POOL_BUILDER)
                    .build();

    private final CacheClient primaryClient;
    private final List<CacheClient> asyncMirrorClients;

    public CacheReplicator(CacheClient primaryClient, List<CacheClient> asyncMirrorClients) {
        this.primaryClient = primaryClient;
        this.asyncMirrorClients = asyncMirrorClients;
    }

    @Override
    public void set(
            final CacheScope scope, final String key, final int expiredTime, final Object object) {
        primaryClient.set(scope, key, expiredTime, object);
        for (final CacheClient asyncMirrorClient : asyncMirrorClients) {
            try {
                asyncExecutor.execute(() -> asyncMirrorClient.set(scope, key, expiredTime, object));
            } catch (RejectedExecutionException e) {
                // Deliberately ignored.
            }
        }
    }

    @Override
    public Object get(CacheScope scope, String key) {
        return primaryClient.get(scope, key);
    }

    @Override
    public void delete(final CacheScope scope, final String key) {
        primaryClient.delete(scope, key);
        for (final CacheClient asyncMirrorClient : asyncMirrorClients) {
            try {
                asyncExecutor.execute(() -> asyncMirrorClient.delete(scope, key));
            } catch (RejectedExecutionException e) {
                // Deliberately ignored.
            }
        }
    }

    @Override
    public void shutdown() {
        ExecutorServiceUtils.shutdownExecutor(
                "cacheReplicator", asyncExecutor, 10, TimeUnit.SECONDS);
    }
}
