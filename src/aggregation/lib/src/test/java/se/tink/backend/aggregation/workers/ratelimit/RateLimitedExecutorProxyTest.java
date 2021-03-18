package se.tink.backend.aggregation.workers.ratelimit;

import com.google.common.base.MoreObjects;
import com.google.common.base.Stopwatch;
import com.google.common.base.Suppliers;
import com.google.common.base.Ticker;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.workers.ratelimit.RateLimitedExecutorProxy.RateLimiter;
import se.tink.libraries.concurrency.ListenableThreadPoolExecutor;
import se.tink.libraries.concurrency.TypedThreadPoolBuilder;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.registry.MetricRegistry;

public class RateLimitedExecutorProxyTest {

    public static final MetricId.MetricLabels NO_METRIC_LABELS = new MetricId.MetricLabels();
    public static final MetricRegistry dummyMetricRegistry = new MetricRegistry();
    private static final int MAX_QUEUED_UP = 180000;

    private static class TestRunnable implements Runnable {
        private String identifier;

        public TestRunnable(String identifier) {
            this.identifier = identifier;
        }

        @Override
        public void run() {
            System.out.println(this);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this).add("identifier", identifier).toString();
        }
    }

    private static class FakeRateLimiter extends Ticker
            implements RateLimitedExecutorProxy.RateLimiter {

        private static final long DURATION_SINCE_LAST_ACQUIRE = TimeUnit.MILLISECONDS.toSeconds(50);
        private final long minimumNanosPerAcquire;

        private Long lastAquire = null;
        private long fakeNow;

        /**
         * Constructor.
         *
         * @param now the start time
         * @param minimumNanosPerAcquire the rate per second of aquire. Using long to make
         *     arithmetics without rounding errors.
         */
        public FakeRateLimiter(long now, long minimumNanosPerAcquire) {
            this.fakeNow = now;
            this.minimumNanosPerAcquire = minimumNanosPerAcquire;
        }

        @Override
        public double acquire() {
            if (lastAquire == null) {
                lastAquire = System.nanoTime();
            } else {
                this.fakeNow += Math.max(0, minimumNanosPerAcquire - DURATION_SINCE_LAST_ACQUIRE);
            }
            return 0.0;
        }

        @Override
        public long read() {
            return fakeNow;
        }
    }

    /**
     * Making sure 1) execution order is correctly understood and 2) that ClassCastExceptions are
     * not thrown.
     *
     * @throws InterruptedException should never be thrown.
     */
    @Test
    public void testBasicRateLimiting() throws InterruptedException {

        ListenableThreadPoolExecutor<Runnable> delegateExecutor =
                ListenableThreadPoolExecutor.builder(
                                Queues.newLinkedBlockingQueue(),
                                new TypedThreadPoolBuilder(1, new ThreadFactoryBuilder().build()))
                        .build();

        int RATE_PER_SECOND = 1;
        FakeRateLimiter fakeRateLimiter =
                new FakeRateLimiter(System.nanoTime(), TimeUnit.SECONDS.toNanos(1));

        RateLimitedExecutorProxy executor =
                new RateLimitedExecutorProxy(
                        false,
                        Suppliers.<RateLimiter>ofInstance(fakeRateLimiter),
                        delegateExecutor,
                        new ThreadFactoryBuilder()
                                .setNameFormat("test-rate-limit-proxy-%d")
                                .build(),
                        dummyMetricRegistry,
                        NO_METRIC_LABELS,
                        MAX_QUEUED_UP);

        Stopwatch timer = Stopwatch.createStarted(fakeRateLimiter);

        final int ITEMS_SUBMITTED = 4;
        for (int i = 0; i < ITEMS_SUBMITTED; i++) {
            executor.execute(new TestRunnable(Integer.toString(i)));
        }

        executor.shutdown();
        executor.awaitTermination(60, TimeUnit.SECONDS);

        delegateExecutor.shutdown();
        delegateExecutor.awaitTermination(60, TimeUnit.SECONDS);

        long elapsed = timer.elapsed(TimeUnit.MILLISECONDS);

        // The first RateLimitter#acquire call is always free.
        final int RUNNABLES_THAT_WILL_WAIT_FOR_RATE_LIMITER = ITEMS_SUBMITTED - 1;
        final int TOTAL_LATENCY_RATE_LIMITTER_WILL_INCUR_MS =
                RUNNABLES_THAT_WILL_WAIT_FOR_RATE_LIMITER * 1000 / RATE_PER_SECOND;

        Assert.assertEquals(elapsed, TOTAL_LATENCY_RATE_LIMITTER_WILL_INCUR_MS);
    }
}
