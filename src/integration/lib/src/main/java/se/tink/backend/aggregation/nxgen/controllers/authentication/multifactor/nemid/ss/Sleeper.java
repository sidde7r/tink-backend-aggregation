package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss;

class Sleeper {
    void sleepFor(final long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
