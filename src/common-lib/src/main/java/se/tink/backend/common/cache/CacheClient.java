package se.tink.backend.common.cache;

public interface CacheClient {
    void set(CacheScope scope, String key, int expiredTime, Object object);

    Object get(CacheScope scope, String key);

    void delete(CacheScope scope, String key);

    void shutdown();
}
