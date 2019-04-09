package se.tink.libraries.concurrency;

import com.google.common.base.Function;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.Nullable;

public class WrappedCallableListenableFutureTask<T extends Callable<V>, V>
        implements Runnable, ListenableFuture<V> {

    public static class DelegateExtractor<T extends Callable<?>>
            implements Function<WrappedCallableListenableFutureTask<T, ?>, T> {

        @Nullable
        @Override
        public T apply(
                @Nullable
                        WrappedCallableListenableFutureTask<T, ?> comparableListenableFutureTask) {
            return comparableListenableFutureTask.getDelegate();
        }
    }

    private final ListenableFutureTask<V> delegateListenableFuture;
    private T delegate;

    public T getDelegate() {
        return delegate;
    }

    WrappedCallableListenableFutureTask(T delegate) {
        this.delegate = delegate;
        this.delegateListenableFuture = ListenableFutureTask.create(delegate);
    }

    @Override
    public void addListener(Runnable runnable, Executor executor) {
        delegateListenableFuture.addListener(runnable, executor);
    }

    @Override
    public void run() {
        delegateListenableFuture.run();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return delegateListenableFuture.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return delegateListenableFuture.isCancelled();
    }

    @Override
    public boolean isDone() {
        return delegateListenableFuture.isDone();
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        return delegateListenableFuture.get();
    }

    @Override
    public V get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return delegateListenableFuture.get(timeout, unit);
    }
}
