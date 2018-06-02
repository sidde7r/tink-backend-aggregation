package se.tink.backend.utils;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

/**
 * Wraps a {@link Callable} and logs exceptions (and rethrows them). Useful utility class when submitting {@link Runnable}s to
 * {@link ExecutorService}s.
 */
public class LoggingCallable<T> implements Callable<T> {

    private LogUtils log;
    private Callable<T> delegate;

    public LoggingCallable(Callable<T> delegate) {
        this(delegate, new LogUtils(LoggingCallable.class));
    }
    
    public LoggingCallable(Callable<T> delegate, LogUtils log) {
        this.delegate = delegate;
        this.log = log;
    }
    
    @Override
    public T call() throws Exception {
        try {
            return delegate.call();
        } catch (Exception e) {
            log.error(String.format("Could not execute delegated callable '%s'.", delegate.toString()), e);
            
            // Rethrowing this exception to be able to wrap it in something else if we want to.
            throw e;
        }
    }
    
}
