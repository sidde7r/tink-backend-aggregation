package se.tink.backend.system.cli.helper.traversal;

import com.google.common.base.Preconditions;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.Observable.Transformer;
import se.tink.backend.common.concurrency.ListenableThreadPoolExecutor;
import se.tink.backend.common.concurrency.TypeSafeBlockingExecutionHandler;
import se.tink.backend.common.concurrency.TypedThreadPoolBuilder;

/**
 * A {@link Transformer} that starts a {@link ThreadPoolExecutor} lazily and makes sure that to gracefully stop the
 * thread pool executor when it's done. The {@link Builder} allows setting the graceful shutdown timeout. The object is
 * created by calling {@link #buildWithConcurrency(int)} or {@link #buildFromSystemPropertiesWithConcurrency(int)}. The latter supports overriding
 * concurrency using a system property.
 */
public class ThreadPoolObserverTransformer<T> implements Transformer<T, T> {

    private ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("thread-pool-observer-%d").build();

    public static class Builder {

        private static final long DEFAULT_GRACEFUL_SHUTDOWN_TIMEOUT_MS = TimeUnit.HOURS.toMillis(3);

        private long gracefulShutDownTimeout = DEFAULT_GRACEFUL_SHUTDOWN_TIMEOUT_MS;
        private TimeUnit gracefulShutDownTimeoutUnit = TimeUnit.MILLISECONDS;
        private int concurrency;

        private Builder(int concurrency) {
            this.concurrency = concurrency;
        }

        public void setGracefulShutDownTimeout(long timeout, TimeUnit unit) {
            this.gracefulShutDownTimeout = timeout;
            this.gracefulShutDownTimeoutUnit = unit;
        }

        public <T> ThreadPoolObserverTransformer<T> build() {
            return new ThreadPoolObserverTransformer<T>(concurrency, gracefulShutDownTimeout,
                    gracefulShutDownTimeoutUnit);
        }
    }

    private final int concurrency;
    private long gracefulShutDownTimeout;
    private TimeUnit gracefulShutDownTimeoutUnit;

    private final int queueLimit;

    /**
     * Helper method to create {@link ThreadPoolObserverTransformer} where a standard system property can be used
     * overridden.
     *
     * @param fallbackConcurrency concurrency used if no system property is set.
     * @return a builder.
     */
    // TODO: Rename buildFromSystemProperties
    public static Builder buildFromSystemPropertiesWithConcurrency(int fallbackConcurrency) {
        return buildWithConcurrency(Integer.getInteger("concurrency", fallbackConcurrency));
    }

    /* package protected for testability */
    static Builder buildWithConcurrency(int concurrency) {
        return new Builder(concurrency);
    }

    /**
     * Constructor.
     *
     * @param concurrency the number of threads that can process
     */
    private ThreadPoolObserverTransformer(int concurrency, long duration, TimeUnit unit) {
        Preconditions.checkArgument(concurrency > 0, "Concurrency must be positive.");
        Preconditions.checkArgument(duration > 0, "Duration must be positive.");

        this.concurrency = concurrency;

        this.gracefulShutDownTimeout = duration;
        this.gracefulShutDownTimeoutUnit = Preconditions.checkNotNull(unit);

        // Just a guess. Must set this, otherwise we will load all users into memory.
        this.queueLimit = 10 * concurrency;

    }

    @Override
    public Observable<T> call(Observable<T> userSource) {
        Preconditions.checkNotNull(userSource, "userRepository must not be null.");

        // This "starts" the thread pool. It's stopped by ExecutorOperator.

        final ListenableThreadPoolExecutor<Runnable> executor = ListenableThreadPoolExecutor.builder(
                Queues.newLinkedBlockingQueue(queueLimit),
                new TypedThreadPoolBuilder(concurrency, threadFactory))
                .withRejectedHandler(new TypeSafeBlockingExecutionHandler<>())
                .build();

        return userSource
                // Using lift to have my own operator here. I can't simply listen to onTerminated/onCompleted/onError
                // because they would be called on the observable thread running in the thread pool being shut down!
                .lift(new ExecutorOperator<T>(executor, gracefulShutDownTimeout, gracefulShutDownTimeoutUnit));
    }

}
