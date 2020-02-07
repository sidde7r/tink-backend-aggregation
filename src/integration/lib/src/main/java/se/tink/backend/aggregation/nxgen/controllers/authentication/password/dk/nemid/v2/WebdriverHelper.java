package se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2;

import com.google.common.collect.ImmutableList;
import java.io.File;
import java.net.URI;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class WebdriverHelper {

    // NemId Javascript Client Integration for mobile:
    // https://www.nets.eu/dk-da/kundeservice/nemid-tjenesteudbyder/NemID-tjenesteudbyderpakken/Documents/NemID%20Integration%20-%20Mobile.pdf
    private static final long WAIT_FOR_INIT_MILLIS = 1000;
    private static final long WAIT_FOR_RENDER_MILLIS = 5000;
    private static final long PHANTOMJS_TIMEOUT_SECONDS = 30;
    private static final Logger LOGGER = LoggerFactory.getLogger(WebdriverHelper.class);

    private static final ImmutableList<Pattern> INCORRECT_CREDENTIALS_ERROR_PATTERNS =
            ImmutableList.<Pattern>builder()
                    .add(
                            Pattern.compile("^incorrect (user|password).*"),
                            Pattern.compile("^fejl (bruger|adgangskode).*"))
                    .build();

    private static final By ERROR_MESSAGE = By.cssSelector("p.error");

    private WebDriver driver;

    private File readDriverFile() {
        boolean mac = System.getProperty("os.name").toLowerCase().contains("mac");

        if (mac) {
            return new File("tools/phantomjs-tink-mac64-2.1.1");
        } else {
            return new File("tools/phantomjs-tink-linux-x86_64-2.1.1");
        }
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
                    "--debug=true",
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

    public void lookForErrorAndThrowIfFound() throws LoginException {
        Optional<String> errorText = waitForElement(ERROR_MESSAGE).map(WebElement::getText);

        if (errorText.isPresent()) {
            throwError(errorText.get());
        }
    }

    public void throwError(String errorText) throws LoginException {
        // Seen errors:
        // - "Incorrect user ID or password. Enter user ID and password. Changed your password
        // recently, perhaps?"
        // - "Incorrect password."
        String err = errorText.toLowerCase();

        if (INCORRECT_CREDENTIALS_ERROR_PATTERNS.stream()
                .map(p -> p.matcher(err))
                .anyMatch(Matcher::matches)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception(err);
        }

        throw new IllegalStateException(
                String.format("[nemid] Unknown login error '%s'.", errorText));
    }

    // TODO: make this void or return webdriverHelper instead
    public WebDriver constructWebDriver() {

        try {
            this.driver = getPhantomJsDriver();

            Thread.sleep(1000);
            driver.manage().timeouts().implicitlyWait(PHANTOMJS_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            driver.manage().timeouts().pageLoadTimeout(PHANTOMJS_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            driver.manage()
                    .timeouts()
                    .setScriptTimeout(PHANTOMJS_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        } catch (RuntimeException e) {
            LOGGER.error("Could not create PhantomJS WebDriver", e);
            driver.quit();
            throw e;
        } catch (InterruptedException e) {
            LOGGER.error("Could not create PhantomJS WebDriver", e);
            driver.quit();
            Thread.currentThread().interrupt();
        }
        return driver;
    }

    // Functions
    public void initBrowser(URL url) {
        driver.get(url.get());

        waitSomeMillis(WAIT_FOR_INIT_MILLIS);

        String currentUrl = driver.getCurrentUrl();
        for (int i = 0; i < 10; i++) {
            Optional<String> filteredDomain =
                    Optional.ofNullable(currentUrl)
                            .map(URI::create)
                            .map(URI::getHost)
                            .filter(host -> url.get().contains(host));
            if (filteredDomain.isPresent()) {
                return;
            }
            driver.get(url.get());
            waitSomeMillis(WAIT_FOR_INIT_MILLIS);
            currentUrl = driver.getCurrentUrl();
        }
    }

    public void executeJavascript(String script) {
        ((JavascriptExecutor) driver).executeScript(script);
    }

    public void setValueToElement(String value, By xpath) {
        WebElement element =
                waitForElement(xpath)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "[nemid] Could not find element for " + xpath));
        element.sendKeys(value);
    }

    // switch to iframe AND check if rendered correctly, this is done by retrieving an element we
    // know should be present

    // we use element "user name"
    // if the element "user name" is found the iframe is rendered correctly (it renders in several
    // steps)
    public void switchToIframe(By iframe, By input) {
        for (int i = 0; i < 5; i++) {

            Optional<WebElement> element = waitForElement(iframe);

            if (element.isPresent()) {
                driver.switchTo().frame(element.get());
                if (waitForElement(input).isPresent()) {
                    return;
                }
            }
            driver.switchTo().defaultContent();
        }
        throw new IllegalStateException("Can't find iframe element");
    }

    public void switchToIframeAndExecuteJavascript(
            By iframe, By input, NemIdParametersV2 nemIdParameters) {

        for (int i = 0; i < 5; i++) {

            // 1. Inject javascript
            String html =
                    String.format(NemIdConstantsV2.BASE_HTML, nemIdParameters.getNemIdElements());
            String b64Html = Base64.getEncoder().encodeToString(html.getBytes());

            executeJavascript("document.write(atob(\"" + b64Html + "\"));");

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            Optional<WebElement> element = waitForElement(iframe);

            if (element.isPresent()) {
                driver.switchTo().frame(element.get());
                if (waitForElement(input).isPresent()) {
                    return;
                }
            }
            driver.switchTo().defaultContent();
        }
        throw new IllegalStateException("Can't find iframe element");
    }

    public void click(By button) {
        waitForElement(button)
                .orElseThrow(
                        () -> {
                            logErrorUsingPhantomJS(
                                    "[nemid] Could not find button in: " + driver.getPageSource());
                            return new IllegalStateException(
                                    "[nemid] Could not find button element " + button);
                        })
                .click();
    }

    public boolean isVisible(By by) {
        return waitForElement(by).isPresent();
    }

    Optional<WebElement> waitForElement(By by) {
        return waitForElements(by).findFirst();
    }

    public String getPageSource() {
        return driver.getPageSource();
    }

    // UTIL
    private Stream<WebElement> waitForElements(By by) {
        // Wait for new content to load in the frame.
        List<WebElement> elements = driver.findElements(by);
        if (elements.isEmpty()) {
            waitSomeMillis(WAIT_FOR_RENDER_MILLIS);
            elements = driver.findElements(by);
        }
        return elements.stream().filter(WebElement::isDisplayed);
    }

    private void waitSomeMillis(long waitTime) {
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    void clickButton(By button) {
        waitForElement(button)
                .orElseThrow(
                        () -> {
                            logErrorUsingPhantomJS(
                                    "[nemid] Could not find button in: " + driver.getPageSource());
                            return new IllegalStateException(
                                    "[nemid] Could not find button element " + button);
                        })
                .click();
    }

    private void logErrorUsingPhantomJS(String errorMessage) {
        // if we get here we failed to get the initial page
        LOGGER.debug("PhantomJS current(reported) URL: " + driver.getCurrentUrl());
        LOGGER.debug("PHANTOMJS LOGS: ");
        driver.manage()
                .logs()
                .getAvailableLogTypes()
                .forEach(
                        logName -> {
                            LOGGER.debug(logName);
                            driver.manage()
                                    .logs()
                                    .get(logName)
                                    .forEach(l -> LOGGER.debug(l.toString()));
                        });

        LOGGER.warn(errorMessage);
    }
}
