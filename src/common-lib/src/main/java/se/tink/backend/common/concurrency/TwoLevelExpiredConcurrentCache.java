package se.tink.backend.common.concurrency;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheStats;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

public class TwoLevelExpiredConcurrentCache<V> {
    private static final long DEFAULT_EXPIRED_TIME_INNER_MAP_MS = TimeUnit.MINUTES.toMillis(2);
    private static final long DEFAULT_EXPIRED_TIME_OUTER_MAP_MS = TimeUnit.MINUTES
            .toMillis(3); // Check map every 3 minutes
    private static final long CLEAN_UP_TIME_MS = TimeUnit.MINUTES.toMillis(5);
    private final LoadingCache<String, Cache<String, V>> expiringCache;

    private final BiConsumer<String, V> evictionListener;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public TwoLevelExpiredConcurrentCache(BiConsumer<String, V> evictionListener) {
        this.evictionListener = evictionListener;

        this.expiringCache = CacheBuilder.newBuilder()
                .expireAfterAccess(DEFAULT_EXPIRED_TIME_OUTER_MAP_MS, TimeUnit.MILLISECONDS)
                .build(new CacheLoader<String, Cache<String, V>>() {
                    @Override
                    public Cache<String, V> load(String key) throws Exception {
                        return initiateInnerMap();
                    }
                });

        scheduler.schedule(expiringCache::cleanUp, CLEAN_UP_TIME_MS, TimeUnit.MILLISECONDS);
    }

    private Cache<String, V> initiateInnerMap() {
        return CacheBuilder.newBuilder()
                .expireAfterAccess(DEFAULT_EXPIRED_TIME_INNER_MAP_MS, TimeUnit.MILLISECONDS)
                .removalListener((RemovalListener<String, V>) removalNotification -> {
                    if (evictionListener != null && removalNotification.wasEvicted()) {
                        evictionListener.accept(removalNotification.getKey(), removalNotification.getValue());
                    }
                })
                .build();
    }

    public void put(String key1, String key2, V value) {
        expiringCache.getUnchecked(key1).put(key2, value);
    }

    public Map<String, V> get(String key1) {
        Cache<String, V> innerCache = expiringCache.getIfPresent(key1);

        return innerCache != null ? innerCache.asMap() : Collections.emptyMap();
    }

    public Optional<V> remove(String key1, String key2) {
        Cache<String, V> innerCache = expiringCache.getIfPresent(key1);
        if (innerCache != null) {
            V item = innerCache.getIfPresent(key2);

            if (item != null) {
                innerCache.invalidate(key2);
                return Optional.of(item);
            }
        }

        return Optional.empty();
    }

    public void shutdown() {
        scheduler.shutdown();
    }

    public Optional<V> updateExpiration(String key1, String key2) {
        Cache<String, V> innerCache = expiringCache.getIfPresent(key1);
        if (innerCache == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(innerCache.getIfPresent(key2));
    }

    public long size() {
        return expiringCache.size();
    }

    public CacheStats stats() {
        return expiringCache.stats();
    }
}
