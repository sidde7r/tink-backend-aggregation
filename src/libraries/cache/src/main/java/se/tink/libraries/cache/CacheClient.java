package se.tink.libraries.cache;

import java.util.concurrent.Future;

public interface CacheClient {
    Future<?> set(CacheScope scope, String key, int expiredTime, Object object);

    Object get(CacheScope scope, String key);

    Future<Boolean> delete(CacheScope scope, String key);

    void shutdown();
}
