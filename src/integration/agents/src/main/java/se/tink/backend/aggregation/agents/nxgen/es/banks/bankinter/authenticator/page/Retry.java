package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.authenticator.page;

import java.util.function.Supplier;

public interface Retry<T> {

    static <T> Retry<T> of(Supplier<T> function, Runnable onError) {
        return new AttemptableCall<>(function, onError, 0);
    }

    static <T> Retry<T> of(Supplier<T> function, Runnable onError, int attempt) {
        return new AttemptableCall<>(function, onError, attempt);
    }

    Supplier<T> getFunction();

    Runnable onError();

    int getAttempt();

    void setLastException(Exception exception);

    Exception getLastException();

    default int getMaxAttempts() {
        return 3;
    }

    default T call() throws AttemptsLimitExceededException {
        if (getAttempt() >= getMaxAttempts()) {
            throw new AttemptsLimitExceededException(
                    "Attempts number has been exceeded", getLastException());
        }
        try {
            return getFunction().get();
        } catch (Exception e) {
            setLastException(e);
            onError().run();
            Retry<T> retry = Retry.of(getFunction(), onError(), getAttempt() + 1);
            return retry.call();
        }
    }

    class AttemptableCall<T> implements Retry<T> {
        private final Supplier<T> function;
        private final Runnable onError;
        private final int attempt;
        private Exception lastException;

        public AttemptableCall(Supplier<T> function, Runnable onError, int attempt) {
            this.function = function;
            this.onError = onError;
            this.attempt = attempt;
        }

        @Override
        public Supplier<T> getFunction() {
            return function;
        }

        @Override
        public Runnable onError() {
            return onError;
        }

        @Override
        public int getAttempt() {
            return attempt;
        }

        @Override
        public void setLastException(Exception exception) {
            this.lastException = exception;
        }

        @Override
        public Exception getLastException() {
            return lastException;
        }
    }
}
