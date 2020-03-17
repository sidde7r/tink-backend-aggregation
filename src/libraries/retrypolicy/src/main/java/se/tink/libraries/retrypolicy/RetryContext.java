package se.tink.libraries.retrypolicy;

class RetryContext {
    private int attempt;
    private Throwable throwable;

    RetryContext() {}

    int getAttempt() {
        return attempt;
    }

    Throwable getLastThrowable() {
        return throwable;
    }

    void setNewThrowable(final Throwable t) {
        this.throwable = t;
        attempt++;
    }
}
