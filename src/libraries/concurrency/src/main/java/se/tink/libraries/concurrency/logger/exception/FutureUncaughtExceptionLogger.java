package se.tink.libraries.concurrency.logger.exception;

import com.google.common.util.concurrent.FutureCallback;
import java.util.concurrent.CancellationException;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Thread-safe. */
public class FutureUncaughtExceptionLogger implements FutureCallback<Object> {
    private static final Logger log = LoggerFactory.getLogger(FutureUncaughtExceptionLogger.class);

    @Override
    public void onSuccess(@Nullable Object t) {
        // Deliberately left empty.
    }

    @Override
    public void onFailure(Throwable throwable) {
        if (throwable instanceof CancellationException) {
            log.debug("Throwing away cancelled future", throwable);
            return;
        }

        log.error("Uncaught exception in ListenableFuture.", throwable);
    }
}
