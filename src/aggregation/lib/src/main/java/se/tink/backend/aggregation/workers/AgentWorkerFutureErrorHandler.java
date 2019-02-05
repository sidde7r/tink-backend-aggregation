package se.tink.backend.aggregation.workers;

import com.google.common.util.concurrent.FutureCallback;
import java.util.concurrent.CancellationException;
import javax.annotation.Nullable;
import se.tink.backend.aggregation.log.AggregationLogger;

/**
 * Thread-safe.
 */
public class AgentWorkerFutureErrorHandler implements FutureCallback<Object> {
    private static final AggregationLogger LOG = new AggregationLogger(AgentWorkerFutureErrorHandler.class);

    @Override
    public void onSuccess(@Nullable Object t) {
        // Deliberately left empty.
    }

    @Override
    public void onFailure(Throwable throwable) {
        if (throwable instanceof CancellationException) {
            // Ignore.
            // A CancellationException is thrown whenever the FIFO queue (in AgentWorkerContext) of size 1 cancels the
            // item in the queue to make space for a new one.
            return;
        }

        LOG.error("Uncaught exception in ListenableFuture.", throwable);
    }
}
