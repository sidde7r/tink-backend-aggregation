package se.tink.libraries.retrypolicy;

class RetryContext {
    private int attempt;
    private Throwable t;

    RetryContext() {}

    int getAttempt() {
        return attempt;
    }

    Throwable getLastThrowable() {
        return t;
    }

    void setNewThrowable(final Throwable t) {
        this.t = t;
        attempt++;
    }
}
