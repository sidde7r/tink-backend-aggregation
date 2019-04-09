package se.tink.libraries.concurrency;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.Callable;

/** Similar to a {@link ListenableExecutor}, but takes {@link Callable}s as argument. */
public interface ListenableSubmitter {

    <T extends Callable<V>, V> ListenableFuture<V> submit(T callable);
}
