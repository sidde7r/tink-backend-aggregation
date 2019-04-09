package se.tink.libraries.concurrency;

import com.google.common.base.Function;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.Nullable;

public class WrappedRunnableListenableFutureTask<T extends Runnable, V>
        implements Runnable, ListenableFuture<V> {

    public static class DelegateExtractor<T extends Runnable>
            implements Function<WrappedRunnableListenableFutureTask<T, ?>, T> {

        @Nullable
        @Override
        public T apply(WrappedRunnableListenableFutureTask<T, ?> comparableListenableFutureTask) {
            return comparableListenableFutureTask.getDelegate();
        }
    }

    private final ListenableFutureTask<V> delegateListenableFuture;
    private T delegate;

    public T getDelegate() {
        return delegate;
    }

    WrappedRunnableListenableFutureTask(T delegate, V value) {
        this.delegate = delegate;
        this.delegateListenableFuture = ListenableFutureTask.create(delegate, value);
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
