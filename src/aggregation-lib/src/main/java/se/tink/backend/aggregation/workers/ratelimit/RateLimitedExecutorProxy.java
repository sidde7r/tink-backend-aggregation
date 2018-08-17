package se.tink.backend.aggregation.workers.ratelimit;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import se.tink.backend.common.concurrency.InstrumentedRunnable;
import se.tink.backend.common.concurrency.ListenableThreadPoolExecutor;
import se.tink.backend.common.concurrency.RunnableMdcWrapper;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;

public class RateLimitedExecutorProxy extends AbstractExecutorService {

    // On Leeds (running 3g heap size), we started GC:ing aggressively when above 180k elements in the queue here. At
    // 300k elements we ran out of memory entirely and all aggregation deadlocked (note that they did not restart). The
    // reason we queued up was because our rate limitters were limitting us to process at the incoming rate. That
    // said, we should not be piling up this many requests on an aggregation instance.
    //
    // If we hit this limit, #submit and #execute will throw RejectedExecutionException.

    private final MetricRegistry metricRegistry;
    private final MetricId.MetricLabels metricLabels;

    public interface RateLimiter {
        void acquire();
    }

    public static class RateLimiters {
        public static RateLimiter from(final com.google.common.util.concurrent.RateLimiter source) {
            return source::acquire;
        }
    }

    // Needs to be public to introspect <code>rateLimittedQueue</code> items from outside of this class.
    public class RateLimitedRunnable implements Runnable {

        private final Runnable actualRunnable;

        private RateLimitedRunnable(Runnable actualRunnable) {
            this.actualRunnable = RunnableMdcWrapper.wrap(actualRunnable);
        }

        @Override
        public void run() {
            rateLimiter.get().acquire();
            delegate.execute(actualRunnable);
        }
    }

    private final ListenableThreadPoolExecutor<Runnable> delegate;
    private final ExecutorService rateLimitedExecutorService;
    private final Supplier<RateLimiter> rateLimiter;
    private final BlockingQueue<Runnable> rateLimittedQueue;

    public RateLimitedExecutorProxy(Supplier<RateLimiter> rateLimiter,
            ListenableThreadPoolExecutor<Runnable> delegate,
            ThreadFactory threadFactory, MetricRegistry metricRegistry,
            MetricId.MetricLabels metricLabels,
            int maxQueueableItems) {
        this.delegate = Preconditions.checkNotNull(delegate);
        this.rateLimiter = Preconditions.checkNotNull(rateLimiter);
        this.rateLimittedQueue = new LinkedBlockingQueue<>(maxQueueableItems);
        this.rateLimitedExecutorService = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, rateLimittedQueue,
                threadFactory);
        this.metricRegistry = metricRegistry;
        this.metricLabels = metricLabels;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return rateLimitedExecutorService.awaitTermination(timeout, unit);
    }

    @Override
    public void execute(Runnable command) {
        // Important we call #execute here. #submit method will wrap the Runnable in a FutureRunnable, which isn't
        // comparable. Comparability is required for the PriorityBlockingQueue to properly function.
        InstrumentedRunnable instrumentedRunnable = new InstrumentedRunnable(metricRegistry, "rate-limitter",
                metricLabels,
                new RateLimitedRunnable(Preconditions.checkNotNull(command)));
        rateLimitedExecutorService.execute(instrumentedRunnable);
        instrumentedRunnable.submitted();
    }

    public BlockingQueue<Runnable> getQueue() {
        return rateLimittedQueue;
    }

    @Override
    public boolean isShutdown() {
        return rateLimitedExecutorService.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return rateLimitedExecutorService.isTerminated();
    }

    /**
     * Note that the delegate executor service is _not_ shutdown.
     */
    @Override
    public void shutdown() {
        rateLimitedExecutorService.shutdown();
    }

    /**
     * Note that the delegate executor service is _not_ shutdown.
     */
    @Override
    public List<Runnable> shutdownNow() {
        return rateLimitedExecutorService.shutdownNow();
    }

}
