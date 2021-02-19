package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.utils;

public class Sleeper {
    public void sleepFor(final long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
