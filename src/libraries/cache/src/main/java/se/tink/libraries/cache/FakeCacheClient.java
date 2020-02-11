package se.tink.libraries.cache;

public class FakeCacheClient implements CacheClient {

    public FakeCacheClient() {}

    @Override
    public void set(CacheScope scope, String key, int expiredTime, Object object) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Object get(CacheScope scope, String key) {
        throw new UnsupportedOperationException("Not implemented");
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
