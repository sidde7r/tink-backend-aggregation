package se.tink.backend.utils;

import com.google.common.util.concurrent.FutureCallback;
import java.util.concurrent.CancellationException;
import javax.annotation.Nullable;

/**
 * Thread-safe.
 */
public class FutureUncaughtExceptionLogger implements FutureCallback<Object> {
    private static final LogUtils LOG = new LogUtils(FutureUncaughtExceptionLogger.class);

    @Override
    public void onSuccess(@Nullable Object t) {
        // Deliberately left empty.
    }

    @Override
    public void onFailure(Throwable throwable) {
        if (throwable instanceof CancellationException) {
            LOG.debug("Throwing away cancelled future", throwable);
            return;
        }

        LOG.error("Uncaught exception in ListenableFuture.", throwable);
    }
}
