package se.tink.backend.common.concurrency;

import com.google.common.base.Preconditions;
import java.util.concurrent.Callable;

public class PrioritizedCallable<V> implements Callable<V> {

    public static final int HIGH_PRIORITY = 0;
    public static final int LOW_PRIORITY = 1;

    private final Callable<V> delegate;
    public final int priority;

    public PrioritizedCallable(int priority, Callable<V> delegate) {
        this.priority = priority;
        this.delegate = Preconditions.checkNotNull(delegate);
    }

    @Override
    public V call() throws Exception {
        return delegate.call();
    }
}
