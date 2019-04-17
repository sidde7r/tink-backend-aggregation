package se.tink.libraries.concurrency;

import com.google.common.base.Objects;
import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Service;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.libraries.metrics.Counter;

/**
 * Something similar to a {@link ThreadPoolExecutor}, but
 *
 * <ul>
 *   <li>logs errors.
 *   <li>returns {@link ListenableFuture}s.
 *   <li>makes sure that submission to {@link ThreadPoolExecutor} is type-safe.
 *   <li>only handles {@link Runnable}s to make implementation of type safety _much_ easier. See
 *       {@link ListenableThreadPoolSubmitter} for something that accepts {@link
 *       java.util.concurrent.Callable}s.
 * </ul>
 *
 * @param <T> the type of the runnable that can be submitted.
 */
public class ListenableThreadPoolExecutor<T extends Runnable>
        implements TerminatableExecutor, ListenableExecutor<T> {
    private static final Logger log = LoggerFactory.getLogger(ListenableThreadPoolExecutor.class);

    private final FutureCallback<Object> errorLoggingCallback;

    private final RejectedExecutionHandler<WrappedRunnableListenableFutureTask<T, ?>>
            rejectedHandler;
    private final BlockingQueue<WrappedRunnableListenableFutureTask<T, ?>> queue;
    private final QueuePopper queuePopper;
    private final AtomicBoolean shutdown = new AtomicBoolean(false);
    private final ThreadPoolExecutor threadPool;
    private final Counter queuedItems;
    private final Counter startedItems;
    private final IncrementCounterRunnable finishedRunningIncrementor;

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

            WrappedRunnableListenableFutureTask<T, ?> item = null;

            // Normal loop:

            while (isRunning()) {
                item = pollAndDelegateQueueItem(item);
            }

            // Drain:

            while (queue.size() > 0 || item != null) {
                item = pollAndDelegateQueueItem(item);
            }
        }

        private WrappedRunnableListenableFutureTask<T, ?> pollAndDelegateQueueItem(
                WrappedRunnableListenableFutureTask<T, ?> item) {
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

    /**
     * Package private Constructor. You probably want to use {@code
     * ListenableThreadPoolExecutorBuilder} instead.
     */
    ListenableThreadPoolExecutor(
            RejectedExecutionHandler<WrappedRunnableListenableFutureTask<T, ?>> rejectedHandler,
            BlockingQueue<WrappedRunnableListenableFutureTask<T, ?>> queue,
            TypedThreadPoolBuilder threadPoolBuilder,
            FutureCallback<Object> errorLoggingCallback,
            Counter queuedItems,
            Counter startedItems,
            Counter finishedItems) {

        this.queue = queue;
        this.queuePopper = new QueuePopper();
        this.rejectedHandler = rejectedHandler;
        this.errorLoggingCallback = errorLoggingCallback;

        this.queuedItems = queuedItems;
        this.startedItems = startedItems;

        this.finishedRunningIncrementor = new IncrementCounterRunnable(finishedItems);

        threadPool = threadPoolBuilder.build();
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
    }

    @Override
    protected void finalize() throws Throwable {
        // Make sure that the background QueuePopper service can be garbage collected. Notice that,
        // a ThreadPoolExecutor
        // only will be garbage collected if `corePoolSize` is zero. See
        // http://stackoverflow.com/a/7728645.
        shutdown();
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
        final Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            queuePopper.awaitTerminated(timeout, unit);
            if (Objects.equal(queuePopper.state(), Service.State.FAILED)) {
                log.error(
                        "QueuePopper failed. This should never happen.",
                        queuePopper.failureCause());
            }
        } catch (TimeoutException e) {
            return false;
        }

        timeout -= stopwatch.elapsed(unit);
        if (timeout < 0) {
            // Just in case. ThreadPoolExecutor#awaitTermination probably handles negative timeout,
            // but better safe than
            // sorry.
            timeout = 0;
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
    public ListenableFuture<?> execute(T r) {
        WrappedRunnableListenableFutureTask<T, ?> future =
                new WrappedRunnableListenableFutureTask<>(r, null);

        if (!queue.offer(future)) {
            rejectedHandler.handle(future, queue);

            // If we come here, we expect the rejectedHandler to have put `future` on `queue`.
            // Otherwise it violates
            // the contract the defined for `RejectedExecutionHandler`.
        }

        queuedItems.inc();

        // Make sure we log errors.
        Futures.addCallback(future, errorLoggingCallback, MoreExecutors.directExecutor());

        future.addListener(finishedRunningIncrementor, MoreExecutors.directExecutor());

        return future;
    }

    @Override
    public <V> ListenableFuture<V> execute(T r, V v) {
        WrappedRunnableListenableFutureTask<T, V> future =
                new WrappedRunnableListenableFutureTask<>(r, v);

        if (!queue.offer(future)) {
            rejectedHandler.handle(future, queue);

            // If we come here, we expect the rejectedHandler to have put `future` on `queue`.
            // Otherwise it violates
            // the contract the defined for `RejectedExecutionHandler`.
        }

        queuedItems.inc();

        // Make sure we log errors.
        Futures.addCallback(future, errorLoggingCallback, MoreExecutors.directExecutor());

        future.addListener(finishedRunningIncrementor, MoreExecutors.directExecutor());

        return future;
    }

    public static <T extends Runnable> ListenableThreadPoolExecutorBuilder<T> builder(
            BlockingQueue<WrappedRunnableListenableFutureTask<T, ?>> queue,
            TypedThreadPoolBuilder tpb) {
        return new ListenableThreadPoolExecutorBuilder<>(queue, tpb);
    }
}
