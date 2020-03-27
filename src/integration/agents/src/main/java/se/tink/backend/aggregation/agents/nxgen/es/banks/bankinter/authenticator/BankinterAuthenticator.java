package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.authenticator;

import com.google.common.base.Function;
import java.io.File;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.WebDriverWait;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.LoginForm;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.Paths;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.Urls;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class BankinterAuthenticator implements PasswordAuthenticator {
    private static final AggregationLogger LOG =
            new AggregationLogger(BankinterAuthenticator.class);
    private static final File phantomJsFile;
    private final BankinterApiClient apiClient;
    private final SessionStorage sessionStorage;

    public BankinterAuthenticator(BankinterApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    static {
        boolean mac = System.getProperty("os.name").toLowerCase().contains("mac");

        if (mac) {
            phantomJsFile = new File("tools/phantomjs-tink-mac64-2.1.1");
        } else {
            phantomJsFile = new File("tools/phantomjs-tink-linux-x86_64-2.1.1");
        }
    }

    private WebDriver createWebDriver() {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(
                PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
                phantomJsFile.getAbsolutePath());

        capabilities.setCapability(CapabilityType.TAKES_SCREENSHOT, false);
        capabilities.setCapability(CapabilityType.SUPPORTS_ALERTS, false);
        capabilities.setCapability(
                PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "userAgent",
                HeaderValues.USER_AGENT);

        final String[] phantomArgs =
                new String[] {
                    // No need to load images
                    "--load-images=false",
                    // For debugging, activate these:
                    // "--webdriver-loglevel=DEBUG",
                    // "--debug=true",
                    // "--proxy=http://127.0.0.1:8888",
                    // "--ignore-ssl-errors=yes"
                };
        capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, phantomArgs);
        final WebDriver driver = new PhantomJSDriver(capabilities);
        driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        return driver;
    }

    private boolean isShowingError(WebDriver driver) {
        try {
            return driver.findElement(By.id(LoginForm.ERROR_PANEL)).isDisplayed();
        } catch (org.openqa.selenium.NoSuchElementException e) {
            return false;
        }
    }

    private String getErrorMessage(WebDriver driver) {
        try {
            return driver.findElement(By.id(LoginForm.ERROR_PANEL)).getText();
        } catch (org.openqa.selenium.NoSuchElementException e) {
            return null;
        }
    }

    private Function<WebDriver, Boolean> didRedirectOrShowError(String initialUrl) {
        return driver -> {
            if (!driver.getCurrentUrl().equals(initialUrl)) {
                return true;
            }
            return isShowingError(driver);
        };
    }

    private void submitLoginForm(WebDriver driver, String username, String password)
            throws AuthenticationException, AuthorizationException {
        // go to login page
        driver.navigate().to(Urls.LOGIN_PAGE);

        // fill in form
        final WebElement loginForm = driver.findElement(By.id(LoginForm.FORM_ID));
        final WebElement nameField = loginForm.findElement(By.id(LoginForm.USERNAME_FIELD));
        final WebElement passwordField = loginForm.findElement(By.id(LoginForm.PASSWORD_FIELD));
        final String initialUrl = driver.getCurrentUrl();
        nameField.sendKeys(username);
        passwordField.sendKeys(password);

        // submit and wait for error or redirect
        loginForm.submit();
        final WebDriverWait wait = new WebDriverWait(driver, LoginForm.SUBMIT_TIMEOUT_SECONDS);
        wait.ignoring(StaleElementReferenceException.class)
                .until(didRedirectOrShowError(initialUrl));

        // SCA
        if (getCurrentUrl(driver).toUri().getPath().equalsIgnoreCase(Paths.VERIFY_SCA)) {
            // TODO: handle SCA properly
            // SCA triggers randomly for the same user, I haven't been able to see it in testing
            // When we get to this page, the SMS has already been sent
            // Throw a bank side failure, since it will most likely not trigger SCA next time
            LOG.error("SCA not implemented");
            throw BankServiceError.BANK_SIDE_FAILURE.exception("SCA not implemented");
        }

        if (isShowingError(driver)) {
            // error message
            final String errorMessage = getErrorMessage(driver);
            LOG.info("Login error: " + errorMessage);
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        } else if (getCurrentUrl(driver)
                .toUri()
                .getPath()
                .equalsIgnoreCase(Paths.GLOBAL_POSITION)) {
            // login successful
            return;
        }

        // Unhandled error
        LOG.error("Did not reach logged in state or error message: " + driver.getCurrentUrl());
        throw AuthorizationError.UNAUTHORIZED.exception();
    }

    private URL getCurrentUrl(WebDriver driver) {
        return new URL(driver.getCurrentUrl());
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {
        final WebDriver driver = createWebDriver();
        submitLoginForm(driver, username, password);
        apiClient.storeLoginCookies(driver.manage().getCookies());
        driver.quit();
    }
}
