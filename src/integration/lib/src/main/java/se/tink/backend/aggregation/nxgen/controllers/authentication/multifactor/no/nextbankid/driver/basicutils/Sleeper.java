package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.basicutils;

public class Sleeper {
    public void sleepFor(final long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
