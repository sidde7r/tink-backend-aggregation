package se.tink.libraries.executor;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.libraries.concurrency.TerminatableExecutor;

public class ExecutorServiceUtils {
    private static final Logger log = LoggerFactory.getLogger(ExecutorServiceUtils.class);

    /**
     * Shut down a non-monitored executor.
     *
     * @param executorServiceIdentifier identifier used for logging.
     * @param executor the {@link ExecutorService} to shut down.
     * @param waitingTime the amount of patience we have to wait for it to shut down gracefully
     *     before we interrupt the running threads and empty the {@link ExecutorService}'s queue of
     *     stuff to do.
     * @param waitingTimeUnit the {@link TimeUnit} for waitingTime.
     * @return true if shutdown was graceful, false if non-graceful.
     */
    public static boolean shutdownExecutor(
            String executorServiceIdentifier,
            TerminatableExecutor executor,
            long waitingTime,
            TimeUnit waitingTimeUnit) {

        Preconditions.checkNotNull(executor, "executor must not be null.").shutdown();

        Stopwatch timer = Stopwatch.createStarted();

        log.info(
                "Initiated shutdown of thread pool '"
                        + executorServiceIdentifier
                        + "'. Waiting for it to drain...");

        boolean wasGracefulShutdown;
        try {
            wasGracefulShutdown = executor.awaitTermination(waitingTime, waitingTimeUnit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            wasGracefulShutdown = false;
        }

        if (!wasGracefulShutdown) {
            log.warn(
                    "ExecutorService '"
                            + executorServiceIdentifier
                            + "' did not drain in the specified time. Forcing shutdown...");
            List<Runnable> droppedTasks = executor.shutdownNow();
            log.warn(
                    "ExecutorService '"
                            + executorServiceIdentifier
                            + "' was abruptly shut down. "
                            + droppedTasks.size()
                            + " tasks will not be executed.");
        }

        timer.stop();
        log.info(
                "Shutting down '"
                        + executorServiceIdentifier
                        + "' took "
                        + timer.elapsed(TimeUnit.MILLISECONDS)
                        + " ms.");

        return wasGracefulShutdown;
    }
}
