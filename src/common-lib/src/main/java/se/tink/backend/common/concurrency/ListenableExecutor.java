package se.tink.backend.common.concurrency;

import com.google.common.util.concurrent.ListenableFuture;

public interface ListenableExecutor<T extends Runnable> {
    ListenableFuture<?> execute(T r);

    <V> ListenableFuture<V> execute(T c, V t);
}
