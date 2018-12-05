package se.tink.backend.aggregation.agents.abnamro.ics.retry;

import com.google.common.util.concurrent.Uninterruptibles;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import static org.junit.Assert.fail;

public class RetryHelper {

    private final int MAX_ATTEMPTS;
    private final int SLEEP_MILLISECONDS;

    public RetryHelper(int maxAttempts, int sleepMilliseconds) {
        MAX_ATTEMPTS = maxAttempts;
        SLEEP_MILLISECONDS = sleepMilliseconds;
    }

    public void retryUntil(Supplier<Boolean> supplier) {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            if (supplier.get()) {
                return;
            }
            Uninterruptibles.sleepUninterruptibly(SLEEP_MILLISECONDS, TimeUnit.MILLISECONDS);
        }
        fail("Timeout reached");
    }
}
