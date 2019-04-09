package se.tink.libraries.concurrency;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;

public class ImmediateListenableExecutor<T extends Runnable> implements ListenableExecutor<T> {
    @Override
    public ListenableFuture<?> execute(T r) {
        ListenableFutureTask<Object> task = ListenableFutureTask.create(r, null);
        task.run();
        return task;
    }

    @Override
    public <V> ListenableFuture<V> execute(T r, V t) {
        ListenableFutureTask<V> task = ListenableFutureTask.create(r, t);
        task.run();
        return task;
    }
}
