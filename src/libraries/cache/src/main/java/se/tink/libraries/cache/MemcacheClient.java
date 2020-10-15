package se.tink.libraries.cache;

import com.google.common.util.concurrent.Futures;
import java.io.IOException;
import java.util.concurrent.Future;
import javax.annotation.PreDestroy;
import net.spy.memcached.AddrUtil;
import net.spy.memcached.ConnectionFactory;
import net.spy.memcached.MemcachedClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemcacheClient implements CacheClient {
    private static final Logger log = LoggerFactory.getLogger(MemcacheClient.class);
    private final MemcachedClient memcachedClient;

    public MemcacheClient(ConnectionFactory connectionFactory, String host) throws IOException {
        this.memcachedClient = new MemcachedClient(connectionFactory, AddrUtil.getAddresses(host));
    }

    @Override
    public Future<?> set(CacheScope scope, String key, int expiredTime, Object object) {
        try {
            return memcachedClient.set(scope.withKey(key), expiredTime, object);
        } catch (Exception e) {
            log.error("Could not store a key to memcache.", e);
            return Futures.immediateFailedFuture(e);
        }
    }

    @Override
    public Object get(CacheScope scope, String key) {
        try {
            return memcachedClient.get(scope.withKey(key));
        } catch (Exception e) {
            log.warn("Could not query a key from memcache.", e);
            return null;
        }
    }

    @Override
    public Future<Boolean> delete(CacheScope scope, String key) {
        try {
            return memcachedClient.delete(scope.withKey(key));
        } catch (Exception e) {
            log.warn("Could not delete a key in memcache.", e);
            return Futures.immediateFailedFuture(e);
        }
    }

    @Override
    @PreDestroy
    public void shutdown() {
        memcachedClient.shutdown();
        log.debug("Stopped memcached client");
    }
}
