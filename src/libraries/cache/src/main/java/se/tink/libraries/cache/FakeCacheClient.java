package se.tink.libraries.cache;

import com.google.common.util.concurrent.Futures;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

public class FakeCacheClient implements CacheClient {

    private Map<String, Object> cache;

    public FakeCacheClient() {
        this.cache = new HashMap<>();
    }

    private String getInternalKey(CacheScope scope, String key) {
        return scope.toString() + "-" + key;
    }

    @Override
    public Future<?> set(CacheScope scope, String key, int expiredTime, Object object) {
        this.cache.put(getInternalKey(scope, key), object);
        return Futures.immediateFuture(object);
    }

    @Override
    public Object get(CacheScope scope, String key) {
        return cache.containsKey(getInternalKey(scope, key))
                ? this.cache.get(getInternalKey(scope, key))
                : null;
    }

    @Override
    public Future<Boolean> delete(CacheScope scope, String key) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void shutdown() {
        throw new UnsupportedOperationException("Not implemented");
    }
}
