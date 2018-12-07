package se.tink.libraries.cache;

public class NonCachingCacheClient implements CacheClient {
    @Override
    public void set(CacheScope scope, String key, int expiredTime, Object object) {
        // Do nothing
    }

    @Override
    public Object get(CacheScope scope, String key) {
        return null;
    }

    @Override
    public void delete(CacheScope scope, String key) {
        // Do nothing
    }

    @Override
    public void shutdown() {
        // Do nothing
    }
}
