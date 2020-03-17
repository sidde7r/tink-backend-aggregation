package se.tink.libraries.retrypolicy;

public class RetryExecutor {

    private final RetryPolicy retryPolicy;

    RetryExecutor(final RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    public <T, E extends Throwable> T execute(RetryCallback<T, E> retryCallback) throws E {
        RetryContext retryContext = new RetryContext();

        Throwable lastThrowable = null;
        while (retryPolicy.canRetry(retryContext)) {
            try {
                return retryCallback.retry();
            } catch (Throwable t) {
                lastThrowable = t;
                retryContext.setNewThrowable(t);
            }
        }
        throw RetryExecutor.<E>castWhenNeeded(lastThrowable);
    }

    private static <E extends Throwable> E castWhenNeeded(Throwable throwable)
            throws RetryException {
        if (throwable instanceof Error) {
            throw (Error) throwable;
        } else if (throwable instanceof Exception) {
            @SuppressWarnings("unchecked")
            E rethrow = (E) throwable;
            return rethrow;
        } else {
            throw new RetryException(throwable);
        }
    }
}
