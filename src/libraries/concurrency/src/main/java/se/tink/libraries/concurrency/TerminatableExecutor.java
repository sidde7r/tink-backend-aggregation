package se.tink.libraries.concurrency;

import java.util.List;
import java.util.concurrent.TimeUnit;

/** Implements a subset of {@link java.util.concurrent.ExecutorService}. */
public interface TerminatableExecutor {
    void shutdown();

    boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException;

    List<Runnable> shutdownNow();
}
