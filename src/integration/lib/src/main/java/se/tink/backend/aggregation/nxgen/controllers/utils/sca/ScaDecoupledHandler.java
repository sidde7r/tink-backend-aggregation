package se.tink.backend.aggregation.nxgen.controllers.utils.sca;

import com.google.common.util.concurrent.Uninterruptibles;
import java.util.concurrent.TimeUnit;

public abstract class ScaDecoupledHandler<E extends Throwable> {
    final int attempts;
    final long waitBetweenAttempts;
    final TimeUnit unit;

    /**
     * @param attempts number of times to poll
     * @param waitBetweenAttempts time to wait between polling
     * @param unit time to wait between polling
     */
    public ScaDecoupledHandler(int attempts, long waitBetweenAttempts, TimeUnit unit) {
        this.attempts = attempts;
        this.waitBetweenAttempts = waitBetweenAttempts;
        this.unit = unit;
    }

    /**
     * Poll for a response. On error, this method should throw an appropriate exception.
     *
     * @return true on success, false to continue polling
     * @throws E fail with an exception
     */
    protected abstract boolean poll() throws E;

    /**
     * Handle a SCA decoupled flow by polling for a response.
     *
     * @return true on success, false on timeout
     */
    public boolean handleSca() throws E {
        for (int i = 0; i < attempts; i++) {
            if (poll()) {
                return true;
            } else {
                Uninterruptibles.sleepUninterruptibly(waitBetweenAttempts, unit);
            }
        }

        return false;
    }
}
