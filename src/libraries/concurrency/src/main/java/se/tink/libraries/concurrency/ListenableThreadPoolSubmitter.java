package se.tink.libraries.concurrency;

import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Service;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.libraries.concurrency.logger.exception.FutureUncaughtExceptionLogger;
import se.tink.libraries.metrics.Counter;

/**
 * Something similar to a {@link ThreadPoolExecutor}, but
 *
 * <ul>
 *   <li>logs errors.
 *   <li>returns {@link ListenableFuture}s.
 *   <li>makes sure that submission to {@link ThreadPoolExecutor} is type-safe.
 *   <li>only handles {@link Callable}s to make implementation of type safety _much_ easier. See
 *       {@link ListenableThreadPoolExecutor} for something that accepts {@link Runnable}s.
 * </ul>
 *
 * @param <T> the type of Callable that can be submitted.
 */
public class ListenableThreadPoolSubmitter<T extends Callable<?>>
        implements TerminatableExecutor, ListenableSubmitter {

    public static final FutureUncaughtExceptionLogger errorLoggingCallback =
            new FutureUncaughtExceptionLogger();

    private final RejectedExecutionHandler<WrappedCallableListenableFutureTask<T, ?>>
            rejectedHandler;
    private final BlockingQueue<WrappedCallableListenableFutureTask<T, ?>> queue;
    private final QueuePopper queuePopper;
    private final Counter queuedItems;
    private final Counter startedItems;
    private final IncrementCounterRunnable finishedRunningIncrementor;
    private ThreadPoolExecutor threadPool;
    private final AtomicBoolean shutdown = new AtomicBoolean(false);
    private static final Logger log = LoggerFactory.getLogger(ListenableThreadPoolSubmitter.class);

    private class QueuePopper extends AbstractExecutionThreadService {
        private Thread thread;
        private CountDownLatch started = new CountDownLatch(1);

        @Override
        protected void run() throws Exception {
            thread = Thread.currentThread();

            // Must be called after setting `thread` above to avoid getting an NPE in
            // `triggerShutdown` as a race
            // condition.
            started.countDown();

            WrappedCallableListenableFutureTask<T, ?> item = null;

            // Normal loop:

            while (isRunning()) {
                item = pollAndDelegateQueueItem(item);
            }

            // Drain:

            while (queue.size() > 0 || item != null) {
                item = pollAndDelegateQueueItem(item);
            }
        }

        private WrappedCallableListenableFutureTask<T, ?> pollAndDelegateQueueItem(
                WrappedCallableListenableFutureTask<T, ?> item) {
            if (item == null) {
                try {
                    item = queue.take();
                } catch (InterruptedException e) {
                    // Deliberately left empty.
                }
            }
            if (item != null) {
                try {
                    threadPool.execute(item);

                    // Since there is no queue capacity for the threadPool, we know the item is
                    // running.
                    startedItems.inc();

                    item = null;
                } catch (RejectedExecutionException e) {
                    // RejectedException is thrown when we interrupt when threadPool has all slots
                    // taken and we are
                    // waiting for an opening.
                    if (!(e.getCause() instanceof InterruptedException)) {
                        log.error(
                                "Unexpected error in when trying to delegate a Runnable to thread pool.",
                                e);
                    }
                }
            }
            return item;
        }

        @Override
        protected void triggerShutdown() {
            // Interrupt run() thread to signal it should shutdown.
            thread.interrupt();
        }

        public void awaitStarted() {
            try {
                started.await();
            } catch (InterruptedException e) {
                thread.currentThread().interrupt();
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        // Make sure that the background QueuePopper service can be garbage collected. Notice that,
        // a ThreadPoolExecutor
        // only will be garbage collected if `corePoolSize` is zero. See
        // http://stackoverflow.com/a/7728645.
        shutdown();
    }

    /**
     * Package private Constructor. You probably want to use {@code
     * ListenableThreadPoolSubmitterBuilder} instead.
     */
    ListenableThreadPoolSubmitter(
            BlockingQueue<WrappedCallableListenableFutureTask<T, ?>> queue,
            TypedThreadPoolBuilder tpb,
            RejectedExecutionHandler<WrappedCallableListenableFutureTask<T, ?>> rejectedHandler,
            Counter queuedItems,
            Counter startedItems,
            Counter finishedItems) {
        this.queue = queue;
        this.queuePopper = new QueuePopper();
        this.rejectedHandler = rejectedHandler;

        this.queuedItems = queuedItems;
        this.startedItems = startedItems;

        threadPool = tpb.build();
        queuePopper.startAsync();

        // Need to call this here to really make sure that _our_ `QueuePopper#run` has started.
        // Otherwise there's a
        // race condition where it never starts if we:
        //
        // 1. Instantiate a `TypedRunnableThreadPoolExecutor`; and shortly thereafter
        // 2. Call `TypedRunnableThreadPoolExecutor#shutdown`.
        //
        // The reason why this happen lies in the method name name "startAsync", which spawns a new
        // thread and then,
        // before calling `run()` double checks that the service still should be started.
        queuePopper.awaitStarted();
        finishedRunningIncrementor = new IncrementCounterRunnable(finishedItems);
    }

    @Override
    public void shutdown() {
        if (!shutdown.compareAndSet(false, true)) {
            // Already shutdown.
            return;
        }

        final ThreadPoolExecutor tp = threadPool;
        queuePopper.addListener(
                new Service.Listener() {
                    @Override
                    public void terminated(Service.State from) {
                        tp.shutdown();
                    }

                    @Override
                    public void failed(Service.State from, Throwable failure) {
                        tp.shutdown();
                    }
                },
                MoreExecutors.directExecutor());
        queuePopper.stopAsync();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        try {
            queuePopper.awaitTerminated(timeout, unit);
        } catch (TimeoutException e) {
            return false;
        }
        if (!threadPool.awaitTermination(timeout, unit)) {
            return false;
        }
        return true;
    }

    @Override
    public List<Runnable> shutdownNow() {
        queuePopper.stopAsync();
        return threadPool.shutdownNow();
    }

    @Override
    public <Q extends Callable<V>, V> ListenableFuture<V> submit(Q callable) {
        WrappedCallableListenableFutureTask<Q, V> future =
                new WrappedCallableListenableFutureTask<Q, V>(callable);

        // HACK: Workaround to map a type T's generic to method type V here. Since generics are
        // runtime this seem to
        // work as unit test pass.
        WrappedCallableListenableFutureTask<T, ?> castWorkaround =
                (WrappedCallableListenableFutureTask<T, ?>) future;
        if (!queue.offer(castWorkaround)) {
            rejectedHandler.handle(castWorkaround, queue);
        }

        queuedItems.inc();

        // Make sure we log errors.
        Futures.addCallback(future, errorLoggingCallback);

        future.addListener(finishedRunningIncrementor, MoreExecutors.directExecutor());

        return future;
    }

    public static <T extends Callable<?>> ListenableThreadPoolSubmitterBuilder<T> builder(
            BlockingQueue<WrappedCallableListenableFutureTask<T, ?>> queue,
            TypedThreadPoolBuilder threadPoolBuilder) {
        return new ListenableThreadPoolSubmitterBuilder<>(queue, threadPoolBuilder);
    }
}
