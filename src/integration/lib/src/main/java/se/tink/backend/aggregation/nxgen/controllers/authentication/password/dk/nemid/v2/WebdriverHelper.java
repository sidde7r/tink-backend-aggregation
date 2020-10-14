package se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

@Slf4j
class WebdriverHelper {

    private static final long WAIT_FOR_RENDER_MILLIS = 5_000;

    private final Sleeper sleeper;

    WebdriverHelper() {
        this(new Sleeper());
    }

    WebdriverHelper(Sleeper sleeper) {
        this.sleeper = sleeper;
    }

    WebDriver constructWebDriver(long waitTimeout) {

        WebDriver driver = getPhantomJsDriver();
        driver.manage().timeouts().implicitlyWait(waitTimeout, TimeUnit.SECONDS);
        driver.manage().timeouts().pageLoadTimeout(waitTimeout, TimeUnit.SECONDS);
        driver.manage().timeouts().setScriptTimeout(waitTimeout, TimeUnit.SECONDS);

        return driver;
    }

    private WebDriver getPhantomJsDriver() {
        File file = readDriverFile();

        DesiredCapabilities capabilities = new DesiredCapabilities();

        capabilities.setCapability(
                PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, file.getAbsolutePath());

        capabilities.setCapability(CapabilityType.TAKES_SCREENSHOT, false);
        capabilities.setCapability(CapabilityType.SUPPORTS_ALERTS, false);

        String[] phantomArgs =
                new String[] {
                    // To allow iframe-hacking
                    "--web-security=false",
                    // No need to load images
                    "--load-images=false",
                    // For debugging, activate these:
                    // "--webdriver-loglevel=DEBUG",
                    "--debug=false",
                    // "--proxy=127.0.0.1:8888",
                    // "--ignore-ssl-errors=true",
                    // "--webdriver-logfile=/tmp/phantomjs.log"
                };

        capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, phantomArgs);
        capabilities.setCapability(
                PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "userAgent",
                NemIdConstantsV2.USER_AGENT);

        capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, phantomArgs);
        capabilities.setCapability(
                PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "userAgent",
                NemIdConstantsV2.USER_AGENT);

        return new PhantomJSDriver(capabilities);
    }

    private File readDriverFile() {
        boolean mac = System.getProperty("os.name").toLowerCase().contains("mac");

        if (mac) {
            return new File("tools/phantomjs-tink-mac64-2.1.1");
        } else {
            return new File("tools/phantomjs-tink-linux-x86_64-2.1.1");
        }
    }

    void setValueToElement(WebDriver driver, String value, By xpath) {
        WebElement element =
                waitForElement(driver, xpath)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Could not find element for " + xpath));
        element.sendKeys(value);
    }

    Optional<WebElement> waitForElement(WebDriver driver, By by) {
        return waitForElements(driver, by).findFirst();
    }

    private Stream<WebElement> waitForElements(WebDriver driver, By by) {
        List<WebElement> elements = driver.findElements(by);
        if (elements.isEmpty()) {
            sleeper.sleepFor(WAIT_FOR_RENDER_MILLIS);
            elements = driver.findElements(by);
        }
        return elements.stream().filter(WebElement::isDisplayed);
    }

    void clickButton(WebDriver driver, By button) {
        waitForElement(driver, button)
                .orElseThrow(
                        () -> {
                            logErrorUsingPhantomJS(
                                    driver, "Could not find button in: " + driver.getPageSource());
                            return new IllegalStateException(
                                    "Could not find button element " + button);
                        })
                .click();
    }

    private void logErrorUsingPhantomJS(WebDriver driver, String errorMessage) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("PhantomJS current(reported) URL: %s", driver.getCurrentUrl()));
            log.debug("PHANTOMJS LOGS: ");
        }
        driver.manage()
                .logs()
                .getAvailableLogTypes()
                .forEach(
                        logName -> {
                            log.debug(logName);
                            driver.manage()
                                    .logs()
                                    .get(logName)
                                    .forEach(l -> log.debug(l.toString()));
                        });

        log.warn(errorMessage);
    }
}
