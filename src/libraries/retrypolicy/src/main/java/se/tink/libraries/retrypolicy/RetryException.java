package se.tink.libraries.retrypolicy;

class RetryException extends RuntimeException {
    RetryException(Throwable cause) {
        super(cause);
    }
}
