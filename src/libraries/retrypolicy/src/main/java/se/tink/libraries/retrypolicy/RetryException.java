package se.tink.libraries.retrypolicy;

class RetryException extends RuntimeException {
    RetryException(final Throwable cause) {
        super(cause);
    }
}
