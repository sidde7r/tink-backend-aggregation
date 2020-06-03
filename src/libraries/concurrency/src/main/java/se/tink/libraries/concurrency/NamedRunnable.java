package se.tink.libraries.concurrency;

import se.tink.libraries.tracing.lib.api.Tracing;

public class NamedRunnable implements Runnable {
    private final Runnable delegate;
    private final String name;

    public NamedRunnable(Runnable delegate, String name) {
        this.delegate = Tracing.wrapRunnable(delegate);
        this.name = name;
    }

    @Override
    public void run() {
        String originalThreadName = Thread.currentThread().getName();
        Thread.currentThread().setName(name);
        try {
            delegate.run();
        } finally {
            Thread.currentThread().setName(originalThreadName);
        }
    }
}
