package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator;

public class HumanInteractionDelaySimulator {

    public void delayExecution(final int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
