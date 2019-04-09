package se.tink.libraries.metrics;

import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.util.StopWatch;

/**
 * Convenience class to time a sequence according to the MECE principle (mutually exclusive,
 * collectively exhaustive); no overlaps and no gaps.
 *
 * <p>To not have to stack the parts of the sequence to monitor the whole sequence, the sequence is
 * timed as a whole as well (in addition to its parts).
 *
 * <p>The sequence timer can be warmed up by supplying a set of timer names in the constructor. This
 * registers the timers before the sequence has been started. This is optional.
 *
 * <p>Example:
 *
 * <pre>{@code
 * SequenceTimer sequence = new SequenceTimer(registry, class, SequenceTimers.MY_SEQUENCE);
 * SequenceTimer.Context sequenceContext = sequence.time();
 * sequenceContext.mark("step-1"); // Start the timer of "step-1" and the sequence timer as a whole.
 * // Execute step 1.
 * sequenceContext.mark("step-2"); // Stop the "step-1" timer and start the "step-2" timer.
 * // Execute step 2.
 * sequenceContext.stop(); // Stop the "step-2" timer and the sequence timer.
 * }</pre>
 */
public class SequenceTimer {
    private final Timer sequenceTimer;
    private final String sequenceName;
    private final Class<?> metricClass;
    private final LoadingCache<MetricId, Timer> timerCache;

    public class Context {
        private Timer.Context currentContext;
        private Timer.Context sequenceContext;
        private final StopWatch stopWatch;

        public Context(String name) {
            stopWatch = new StopWatch(name);
        }

        public synchronized void mark(String name) {
            // First step in sequence.
            if (currentContext == null) {
                // Start the sequence timer.
                sequenceContext = sequenceTimer.time();
            } else {
                // Stop previous timer.
                currentContext.stop();
            }

            currentContext = timerCache.getUnchecked(constructName(name)).time();

            if (stopWatch.isRunning()) {
                stopWatch.stop();
            }
            stopWatch.start(name);
        }

        public synchronized void stop() {
            if (currentContext != null) {
                currentContext.stop();
                currentContext = null;
            }

            if (sequenceContext != null) {
                sequenceContext.stop();
                sequenceContext = null;
            }

            if (stopWatch.isRunning()) {
                stopWatch.stop();
            }
        }

        public synchronized String prettyPrint() {
            return stopWatch.prettyPrint();
        }
    }

    public SequenceTimer(
            final Class<?> metricClass, final MetricRegistry metricRegistry, String sequenceName) {
        this.metricClass = metricClass;
        this.sequenceName = sequenceName;

        timerCache =
                CacheBuilder.newBuilder()
                        .concurrencyLevel(
                                40) // Arbitrary choice, but we need to manage sequences with high
                        // concurrency.
                        .maximumSize(100)
                        .build(
                                new CacheLoader<MetricId, Timer>() {
                                    public Timer load(MetricId key) {
                                        return metricRegistry.timer(key);
                                    }
                                });

        sequenceTimer = timerCache.getUnchecked(constructName(null));
    }

    public Context time() {
        return new Context(sequenceName);
    }

    private MetricId constructName(String name) {
        if (Strings.isNullOrEmpty(name)) {
            return MetricId.newId("sequence_timer")
                    .label("class", metricClass.getSimpleName())
                    .label("sequence", sequenceName);
        } else {
            return MetricId.newId("sequence_timer")
                    .label("class", metricClass.getSimpleName())
                    .label("sequence", sequenceName)
                    .label("name", name);
        }
    }
}
