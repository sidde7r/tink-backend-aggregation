package se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2;

class Sleeper {
    void sleepFor(final long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
