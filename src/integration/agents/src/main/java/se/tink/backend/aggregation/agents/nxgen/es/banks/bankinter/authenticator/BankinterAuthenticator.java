package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.authenticator;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.LoginForm;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.Paths;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.ScaForm;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.Urls;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.integration.webdriver.WebDriverInitializer;

public class BankinterAuthenticator implements PasswordAuthenticator {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final BankinterApiClient apiClient;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final PrintStream logStream;

    public BankinterAuthenticator(
            BankinterApiClient apiClient,
            SupplementalInformationHelper supplementalInformationHelper,
            ByteArrayOutputStream logOutputStream) {
        this.apiClient = apiClient;
        this.supplementalInformationHelper = supplementalInformationHelper;
        try {
            this.logStream = new PrintStream(logOutputStream, true, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    private WebDriver createWebDriver() {
        WebDriver driver =
                WebDriverInitializer.constructWebDriver(
                        HeaderValues.USER_AGENT, HeaderValues.ACCEPT_LANGUAGE);
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

    private void waitForErrorOrRedirect(WebDriver driver, String fromUrl, long timeoutSeconds) {
        new WebDriverWait(driver, timeoutSeconds)
                .ignoring(StaleElementReferenceException.class)
                .until(didRedirectOrShowError(fromUrl));
    }

    private void submitScaForm(WebDriver driver) throws LoginException {
        final WebElement codeField =
                driver.findElement(By.cssSelector(ScaForm.CODE_FIELD_SELECTOR));
        final WebElement submitButton =
                driver.findElement(By.cssSelector(ScaForm.SUBMIT_BUTTON_SELECTOR));

        final String code;
        try {
            code = supplementalInformationHelper.waitForOtpInput();
        } catch (SupplementalInfoException e) {
            throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception(e);
        }
        codeField.sendKeys(code);
        logger.info("Submitting SCA");
        submitButton.click();

        try {
            waitForErrorOrRedirect(driver, driver.getCurrentUrl(), ScaForm.SUBMIT_TIMEOUT_SECONDS);
        } catch (TimeoutException ex) {
            logger.error("Timed out after submitting SCA");
            logRequest("Timed out after submitting SCA", driver.getPageSource());
            throw ex;
        }

        logRequest("Submitted SCA: " + driver.getCurrentUrl(), driver.getPageSource());
    }

    private void submitLoginForm(WebDriver driver, String username, String password)
            throws LoginException {
        // go to login page
        driver.navigate().to(Urls.LOGIN_PAGE);
        logRequest("Logging in to " + Urls.LOGIN_PAGE, driver.getPageSource());

        // fill in form
        final WebElement loginForm = driver.findElement(By.id(LoginForm.FORM_ID));
        final WebElement nameField = loginForm.findElement(By.id(LoginForm.USERNAME_FIELD));
        final WebElement passwordField = loginForm.findElement(By.id(LoginForm.PASSWORD_FIELD));
        final String initialUrl = driver.getCurrentUrl();
        nameField.sendKeys(username);
        passwordField.sendKeys(password);

        // submit and wait for error or redirect
        logger.info("Submitting login form");
        loginForm.submit();
        waitForErrorOrRedirect(driver, initialUrl, LoginForm.SUBMIT_TIMEOUT_SECONDS);
        final URL afterLoginUrl = getCurrentUrl(driver);
        logRequest("Login ended up in " + afterLoginUrl.toString(), driver.getPageSource());

        // SCA
        if (afterLoginUrl.toUri().getPath().equalsIgnoreCase(Paths.VERIFY_SCA)) {
            logger.info("Reached SCA form");
            submitScaForm(driver);
        }

        if (isShowingError(driver)) {
            // error message
            final String errorMessage = getErrorMessage(driver);
            logger.info("Login error: " + errorMessage);
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        } else if (getCurrentUrl(driver)
                .toUri()
                .getPath()
                .equalsIgnoreCase(Paths.GLOBAL_POSITION)) {
            // login successful
            logger.info("Login successful");
            return;
        }

        // Unhandled error
        logger.error("Did not reach logged in state or error message: " + driver.getCurrentUrl());
        throw LoginError.NOT_SUPPORTED.exception();
    }

    private URL getCurrentUrl(WebDriver driver) {
        return new URL(driver.getCurrentUrl());
    }

    @Override
    public void authenticate(String username, String password) throws LoginException {
        final WebDriver driver = createWebDriver();
        submitLoginForm(driver, username, password);
        apiClient.storeLoginCookies(driver.manage().getCookies());
        driver.quit();
    }

    private void logRequest(String heading, String body) {
        logStream.println("*** " + heading);
        if (!Strings.isNullOrEmpty(body)) {
            logStream.println(body);
        }
    }
}
