package se.tink.backend.common.retry;

import com.github.rholder.retry.Attempt;
import com.github.rholder.retry.RetryListener;
import se.tink.backend.utils.LogUtils;

public class RetryerBuilder {

    public static <S> com.github.rholder.retry.RetryerBuilder<S> newBuilder(final LogUtils log, final String descriptor) {
        com.github.rholder.retry.RetryerBuilder<S> builder = com.github.rholder.retry.RetryerBuilder.newBuilder();
        builder.withRetryListener(new RetryListener() {

            @Override
            public <V> void onRetry(Attempt<V> attempt) {
                if (attempt.getAttemptNumber() > 1) {

                    if (attempt.hasResult()) {
                        log.warn(String.format("Retried '%s' without exception. Attempt: %s",
                                descriptor, attempt.getAttemptNumber()));
                    } else {
                        log.warn(String.format("Retried '%s' with exception. Attempt: %s, Exception: %s",
                                descriptor, attempt.getAttemptNumber(), attempt.getExceptionCause()));
                    }
                }
            }

        });
        return builder;
    }

}
