package se.tink.libraries.cache;

import com.google.common.util.concurrent.Futures;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

public class FakeCacheClient implements CacheClient {

    private final Map<String, Object> cache = new ConcurrentHashMap<>();

    private static String getInternalKey(CacheScope scope, String key) {
        return scope.toString() + "-" + key;
    }

    @Override
    public Future<?> set(CacheScope scope, String key, int expiredTime, Object object) {
        cache.put(getInternalKey(scope, key), object);
        return Futures.immediateFuture(object);
    }

    @Override
    public Object get(CacheScope scope, String key) {
        return cache.get(getInternalKey(scope, key));
    }

    @Override
    public Future<Boolean> delete(CacheScope scope, String key) {
        return Futures.immediateFuture(cache.remove(getInternalKey(scope, key)) != null);
    }

    @Override
    public void shutdown() {}
}
