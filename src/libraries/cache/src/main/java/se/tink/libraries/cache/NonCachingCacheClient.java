package se.tink.libraries.cache;

import com.google.common.util.concurrent.Futures;
import java.util.concurrent.Future;

public class NonCachingCacheClient implements CacheClient {
    @Override
    public Future<?> set(CacheScope scope, String key, int expiredTime, Object object) {
        // Do nothing
        return Futures.immediateFuture(object);
    }

    @Override
    public Object get(CacheScope scope, String key) {
        return null;
    }

    @Override
    public Future<Boolean> delete(CacheScope scope, String key) {
        // Do nothing
        return Futures.immediateFuture(true);
    }

    @Override
    public void shutdown() {
        // Do nothing
    }
}
