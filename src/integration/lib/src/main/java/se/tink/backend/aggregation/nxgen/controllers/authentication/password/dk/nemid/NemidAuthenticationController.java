package se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid;

import com.google.common.base.Strings;
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
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.log.AggregationLogger;

public abstract class NemidAuthenticationController {

    private static final AggregationLogger LOGGER = new AggregationLogger(NemidAuthenticationController.class);
    private static final Pattern PATTERN_INCORRECT_CREDENTIALS = Pattern.compile("^incorrect (user|password).*");

    // NemId Javascript Client Integration for mobile:
    // https://www.nets.eu/dk-da/kundeservice/nemid-tjenesteudbyder/NemID-tjenesteudbyderpakken/Documents/NemID%20Integration%20-%20Mobile.pdf
    private static final long WAIT_FOR_INIT_MILLIS = 1000;
    private static final long WAIT_FOR_RENDER_MILLIS = 5000;
    private static final long PHANTOMJS_TIMEOUT_SECONDS = 30;

    private static final By USERNAME_INPUT = By.cssSelector("input[type=text]");
    private static final By ERROR_MESSAGE = By.cssSelector("p.error");
    private static final By PASSWORD_INPUT = By.cssSelector("input[type=password]");
    static final By SUBMIT_BUTTON = By.cssSelector("button.button--submit");
    private static final By NEMID_TOKEN = By.cssSelector("div#tink_nemIdToken");
    private static final By IFRAME = By.tagName("iframe");

    private static File phantomJsFile;
    private static File phantomJsFile2;

    static {
        boolean mac = System.getProperty("os.name").toLowerCase().contains("mac");

        if (mac) {
            phantomJsFile = new File("tools/phantomjs-tink-mac64-2.1.1");
            phantomJsFile2 = new File("tools/phantomjs-tink-mac64-2.1.1");
        } else {
            phantomJsFile = new File("tools/phantomjs-tink-linux-x86_64-1.9.8");
            phantomJsFile2 = new File("tools/phantomjs-tink-linux-x86_64-2.1.1");
        }
    }

    private final NemIdAuthenticator authenticator;
    private final int version;
    private WebDriver driver;

    public NemidAuthenticationController(NemIdAuthenticator authenticator) {
        this(authenticator, 1);
    }
    public NemidAuthenticationController(NemIdAuthenticator authenticator, int version) {
        this.authenticator = authenticator;
        this.version = version;
    }

    abstract void clickLogin();

    void doLoginWith(String username, String password) throws AuthenticationException, AuthorizationException {
        try {
            driver = constructWebDriver();

            // The base URL MUST be `https://applet.danid.dk`. This is a hack to make it work, phantomjs/selenium does
            // not have features for this. I expect we'd have to recompile phantomjs to accommodate this feature.
            // How it works:
            //  1. Make a normal request to the base url
            //  2. Inject JavaScript code to replace the html contents (base64 encoded in order to avoid string escaping)
            //  3. The new contents will execute and we can go forward

            // 1 - normal request to base URL
            NemIdParameters nemIdParameters = authenticator.getNemIdParameters();
            initBrowser(nemIdParameters);

            // 2 - Inject javascript to do magic

            String html = String.format(NemIdConstants.BASE_HTML, nemIdParameters.getNemIdElements());
            String b64Html = Base64.getEncoder().encodeToString(html.getBytes());
            ((JavascriptExecutor) driver).executeScript("document.write(atob(\"" + b64Html + "\"));");

            // 3 - check all javascript executions are done
            switchToIframe();

            setUserName(username);
            setPassword(password);
            clickLogin();

            lookForErrorAndThrowIfFound();
        } catch (Exception e) {
            logGeneralError();
            throw e;
        }
    }

    void logGeneralError() {
        logErrorUsingPhantomJS("Could not perform operation in: " + driver.getPageSource() );
    }

    void clickButton(By button) {
        waitForElement(button)
                .orElseThrow(() -> {
                    logErrorUsingPhantomJS("[nemid] Could not find button in: " + driver.getPageSource());
                    return new IllegalStateException("[nemid] Could not find button element " + button);
                })
                .click();
    }

    Optional<WebElement> waitForElement(By by) {
        return waitForElements(by).findFirst();
    }

    void passTokenToAuthenticator() throws AuthenticationException, AuthorizationException {
        authenticator.exchangeNemIdToken(collectToken());
    }

    void close() {
        if (driver != null) {
            try {
                driver.quit();
                driver = null;
            } catch (Exception e) {
                LOGGER.warn("Cannot quit PhantomJS WebDriver " + e.getMessage());
            }
        }
    }

    private String collectToken() throws AuthenticationException, AuthorizationException {
        // Try to get the token/errors multiple times. It (both token or error) might not have loaded yet.
        driver.switchTo().defaultContent();
        for (int i = 0; i < 7; i++) {
            Optional<String> nemIdToken = getNemIdToken();

            if (nemIdToken.isPresent()) {
                return nemIdToken.get();
            }
        }
        // We will only reach this state if we could not find the nemId token -> something went wrong in the
        // authentication.
        throw new IllegalStateException("[nemid] Could not find nemId token.");
    }

    private void lookForErrorAndThrowIfFound() throws LoginException {
        Optional<String> errorText = waitForElement(ERROR_MESSAGE).map(WebElement::getText);

        if (errorText.isPresent()) {
            throwError(errorText.get());
        }
    }

    public void throwError(String errorText) throws LoginException {
        // Seen errors:
        // - "Incorrect user ID or password. Enter user ID and password. Changed your password recently, perhaps?"
        // - "Incorrect password."
        String err = errorText.toLowerCase();

        Matcher matcher = PATTERN_INCORRECT_CREDENTIALS.matcher(err);
        if (matcher.matches()) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        throw new IllegalStateException(String.format("[nemid] Unknown login error '%s'.", errorText));
    }

    private WebDriver constructWebDriver() {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        if (version == 2) {
            capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
                    phantomJsFile2.getAbsolutePath());
        } else {
            capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
                    phantomJsFile.getAbsolutePath());
        }

        capabilities.setCapability(CapabilityType.TAKES_SCREENSHOT, false);
        capabilities.setCapability(CapabilityType.SUPPORTS_ALERTS, false);

        String[] phantomArgs = new String[] {
                // To allow iframe-hacking
                "--web-security=false",
                // No need to load images
                "--load-images=false",
                // For debugging, activate these:
//                "--webdriver-loglevel=DEBUG",
                //"--debug=true",
//                "--proxy=127.0.0.1:8888",
//                "--ignore-ssl-errors=true",
//                "--webdriver-logfile=/tmp/phantomjs.log"
        };
        capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, phantomArgs);
        capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "userAgent",
        NemIdConstants.USER_AGENT);
        WebDriver thePhantom = null;

        try {
            thePhantom = new PhantomJSDriver(capabilities);
            thePhantom.manage().timeouts().implicitlyWait(PHANTOMJS_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            thePhantom.manage().timeouts().pageLoadTimeout(PHANTOMJS_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            thePhantom.manage().timeouts().setScriptTimeout(PHANTOMJS_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        } catch (Exception e) {
            LOGGER.error("Could not create PhantomJS WebDriver", e);
            close();
            throw e;
        }
        return thePhantom;
    }
    // switch to iframe AND check if rendered correctly, this is done by retrieving an element we know should be present

    // we use element "user name"
    // if the element "user name" is found the iframe is rendered correctly (it renders in several steps)
    private void switchToIframe() {
        for (int i = 0; i < 5; i++) {
            Optional<WebElement> iframeElement = waitForElement(IFRAME);

            if (iframeElement.isPresent()) {
                driver.switchTo().frame(iframeElement.get());
                if (waitForElement(USERNAME_INPUT).isPresent()) {
                    return;
                }
            }
            driver.switchTo().defaultContent();
        }
        throw new IllegalStateException("Can't find iframe element");
    }

    private Stream<WebElement> waitForElements(By by) {
        // Wait for new content to load in the frame.
        List<WebElement> elements = driver.findElements(by);
        if (elements.isEmpty()) {
            waitSomeMillis(WAIT_FOR_RENDER_MILLIS);
            elements = driver.findElements(by);
        }
        return elements.stream().filter(WebElement::isDisplayed);
    }

    private static void waitSomeMillis(long waitTime) {
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {}
    }

    private void setUserName(String username) {
        setValueToElement(username, USERNAME_INPUT);
    }

    private void setPassword(String password) {
        setValueToElement(password, PASSWORD_INPUT);
    }

    private void setValueToElement(String value, By xpath) {
        WebElement element = waitForElement(xpath)
                .orElseThrow(() -> new IllegalStateException("[nemid] Could not find element for " + xpath));
        element.sendKeys(value);
    }

    private Optional<String> getNemIdToken() {
        Optional<WebElement> tokenElement = waitForElement(
                NEMID_TOKEN);

        if (!tokenElement.isPresent()) {
            return Optional.empty();
        }
        return Optional.ofNullable(Strings.emptyToNull(tokenElement.get().getText()));
    }
    // fetch a page from danid to init session, check that we are not redirected

    private void initBrowser(NemIdParameters nemIdParameters) {
        String baseUrl = nemIdParameters.getInitialUrl().get();
        driver.get(baseUrl);

        waitSomeMillis(WAIT_FOR_INIT_MILLIS);

        String currentUrl = driver.getCurrentUrl();
        for (int i = 0; i < 10; i++) {
            Optional<String> filteredDomain = Optional.ofNullable(currentUrl)
                    .map(URI::create)
                    .map(URI::getHost)
                    .filter(host -> baseUrl.contains(host));
            if (filteredDomain.isPresent()) {
                return;
            }
            driver.get(baseUrl);
            waitSomeMillis(WAIT_FOR_INIT_MILLIS);
            currentUrl = driver.getCurrentUrl();
        }

        // if we get here we failed to get the initial page
        LOGGER.debug("PhantomJS current(reported) URL: " + currentUrl);
        LOGGER.debug("PHANTOMJS LOGS: ");
        driver.manage().logs().getAvailableLogTypes().forEach(logName -> {
            LOGGER.debug(logName);
            driver.manage().logs().get(logName).forEach(l -> LOGGER.debug(l.toString()));
        });

        logErrorUsingPhantomJS("Current URL is not matching requested (did we get redirected?): " + currentUrl);
        throw new IllegalStateException("Bad URL, check protocol: " + currentUrl);
    }

    private void logErrorUsingPhantomJS(String errorMessage) {
        // if we get here we failed to get the initial page
        LOGGER.debug("PhantomJS current(reported) URL: " + driver.getCurrentUrl());
        LOGGER.debug("PHANTOMJS LOGS: ");
        driver.manage().logs().getAvailableLogTypes().forEach(logName -> {
            LOGGER.debug(logName);
            driver.manage().logs().get(logName).forEach(l -> LOGGER.debug(l.toString()));
        });

        LOGGER.warn(errorMessage);
    }
}
