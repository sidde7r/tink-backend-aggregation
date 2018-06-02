package se.tink.backend.common.concurrency;

import java.util.concurrent.Callable;

public class NamedCallable<T> implements Callable<T> {
    protected final Callable<T> delegate;
    protected final String name;

    public NamedCallable(Callable<T> delegate, String name) {
        this.delegate = delegate;
        this.name = name;
    }

    @Override
    public T call() throws Exception {
        String originalThreadName = Thread.currentThread().getName();
        Thread.currentThread().setName(name);
        try {
            return delegate.call();
        } finally {
            Thread.currentThread().setName(originalThreadName);
        }
    }
}
