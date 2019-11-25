package se.tink.backend.aggregation.selenium;

import org.junit.Test;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.firefox.FirefoxDriver;

public class SeleniumTest {

    @Test
    public void whenAttemptingToLoadFirefoxDriver_raisePermissibleException() {
        try {
            System.clearProperty("webdriver.gecko.driver");
            new FirefoxDriver();
        } catch (IllegalStateException | WebDriverException e) {
            // Permissible exceptions
        }
    }
}
