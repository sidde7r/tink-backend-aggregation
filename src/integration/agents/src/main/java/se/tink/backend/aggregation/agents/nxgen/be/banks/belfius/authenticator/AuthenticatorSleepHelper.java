package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator;

public class AuthenticatorSleepHelper {

    public void sleepForMilliseconds(final int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
