package se.tink.libraries.cache;

import java.util.HashMap;
import java.util.Map;

public class FakeCacheClient implements CacheClient {

    private Map<String, Object> cache;

    public FakeCacheClient() {
        this.cache = new HashMap<>();
    }

    private String getInternalKey(CacheScope scope, String key) {
        return scope.toString() + "-" + key;
    }

    @Override
    public void set(CacheScope scope, String key, int expiredTime, Object object) {
        this.cache.put(getInternalKey(scope, key), object);
    }

    @Override
    public Object get(CacheScope scope, String key) {
        return cache.containsKey(getInternalKey(scope, key))
                ? this.cache.get(getInternalKey(scope, key))
                : null;
    }

    @Override
    public void delete(CacheScope scope, String key) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void shutdown() {
        throw new UnsupportedOperationException("Not implemented");
    }
}
