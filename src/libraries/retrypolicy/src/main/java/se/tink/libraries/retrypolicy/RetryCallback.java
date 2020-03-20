package se.tink.libraries.retrypolicy;

public interface RetryCallback<T, E extends Throwable> {
    T retry() throws E;
}
