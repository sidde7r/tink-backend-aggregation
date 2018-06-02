package se.tink.backend.common.concurrency;

import com.google.common.base.Preconditions;

public class PrioritizedRunnable implements Runnable {

    public static final int HIGH_PRIORITY = 0;
    public static final int LOW_PRIORITY = 1;

    private final Runnable delegate;
    public final int priority;

    public PrioritizedRunnable(int priority, Runnable delegate) {
        this.priority = priority;
        this.delegate = Preconditions.checkNotNull(delegate);
    }

    @Override
    public void run() {
        delegate.run();
    }
}
