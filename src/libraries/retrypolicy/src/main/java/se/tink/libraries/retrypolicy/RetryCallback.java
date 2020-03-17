package se.tink.libraries.retrypolicy;

interface RetryCallback<T, E extends Throwable> {
    T retry() throws E;
}
