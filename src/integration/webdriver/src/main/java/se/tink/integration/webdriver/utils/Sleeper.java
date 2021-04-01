package se.tink.integration.webdriver.utils;

public class Sleeper {
    public void sleepFor(final long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
