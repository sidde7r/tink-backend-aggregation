package se.tink.libraries.cache;

import com.google.common.base.Objects;
import com.google.common.base.Stopwatch;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.concurrent.TimeUnit;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.Timer;

public class CacheInstrumentationDecorator implements CacheClient {
    private static final Logger logger =
            LoggerFactory.getLogger(CacheInstrumentationDecorator.class);

    private final CacheClient delegate;

    private final LoadingCache<TimerKey, Timer> cache;
    private final MetricId metricName;

    private static class TimerKey {
        public final String command;
        public final String status;
        public final String scope;

        public TimerKey(String command, String status, CacheScope scope) {
            this.command = command;
            this.status = status;
            this.scope = scope.name();
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(command, status, scope);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            final TimerKey other = (TimerKey) obj;
            return Objects.equal(this.command, other.command)
                    && Objects.equal(this.status, other.status)
                    && Objects.equal(this.scope, other.scope);
        }
    }

    public CacheInstrumentationDecorator(
            CacheClient delegate, final MetricRegistry registry, final MetricId metricName) {
        this.metricName = metricName;
        this.delegate = delegate;
        this.cache =
                CacheBuilder.newBuilder()
                        .build(
                                new CacheLoader<TimerKey, Timer>() {

                                    @Override
                                    public Timer load(TimerKey key) throws Exception {
                                        // Using the same labels are the memcached Prometheus
                                        // exporter.
                                        return registry.timer(
                                                metricName
                                                        .label("command", key.command)
                                                        .label("status", key.status)
                                                        .label("scope", key.scope));
                                    }
                                });
    }

    @Override
    public void set(CacheScope scope, String key, int expiredTime, Object object) {
        final Timer timer = cache.getUnchecked(new TimerKey("set", "hit", scope));
        final Timer.Context context = timer.time();

        delegate.set(scope, key, expiredTime, object);

        context.stop();
    }

    @Override
    public Object get(CacheScope scope, String key) {
        final Stopwatch watch = Stopwatch.createStarted();
        final Object result = delegate.get(scope, key);

        final Timer timer =
                cache.getUnchecked(new TimerKey("get", result != null ? "hit" : "miss", scope));
        timer.update(watch.elapsed(TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);

        return result;
    }

    @Override
    public void delete(CacheScope scope, String key) {
        // This is called a "hit", but that doesn't mean that the command was actually deleting
        // anything.
        final Timer.Context context =
                cache.getUnchecked(new TimerKey("delete", "hit", scope)).time();
        delegate.delete(scope, key);
        context.stop();
    }

    @Override
    @PreDestroy
    public void shutdown() {
        delegate.shutdown();
        logger.debug("Stopped CacheInstrumentationDecorator");
    }
}
