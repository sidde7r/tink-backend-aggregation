package se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.authenticator;

import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.LoginForm.ID_FIELD;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.LoginForm.PASSWORD_FIELD;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.LoginForm.USER_FIELD;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.USER_FIELD_INPUT;

import com.google.common.base.Strings;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.LoginForm;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.authenticator.exception.GlobalPositionNotFoundException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.http.log.executor.raw.RawHttpTrafficLogger;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorage;
import se.tink.integration.webdriver.WebDriverWrapper;
import se.tink.integration.webdriver.logger.HtmlLogger;
import se.tink.libraries.har_logger.src.model.HarEntry;
import se.tink.libraries.retrypolicy.RetryCallback;
import se.tink.libraries.retrypolicy.RetryExecutor;
import se.tink.libraries.retrypolicy.RetryPolicy;

@Slf4j
public class RuralviaAuthenticator implements Authenticator {

    private final RuralviaApiClient apiClient;
    private final WebDriverWrapper driver;
    private final AgentTemporaryStorage agentTemporaryStorage;
    private final HtmlLogger htmlLogger;
    private final RetryExecutor retryExecutor = new RetryExecutor();

    public RuralviaAuthenticator(
            RuralviaApiClient apiClient,
            AgentTemporaryStorage agentTemporaryStorage,
            RawHttpTrafficLogger rawHttpTrafficLogger,
            Consumer<HarEntry> harEntryConsumer,
            WebDriverWrapper driver) {
        this.apiClient = apiClient;
        this.agentTemporaryStorage = agentTemporaryStorage;
        this.driver = driver;
        this.htmlLogger = new HtmlLogger(driver, rawHttpTrafficLogger, harEntryConsumer);
        this.retryExecutor.setRetryPolicy(
                new RetryPolicy(3, GlobalPositionNotFoundException.class));
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {

        checkCredentials(credentials);

        // There are a lot of errors during authentication caused by not finding Global Posicion
        // element. It looks that result is non deterministic, that's why retry used to increase
        // chances of getting through.
        retryExecutor.execute(
                (RetryCallback<Void, GlobalPositionNotFoundException>)
                        () -> {
                            doLogin(credentials);
                            return null;
                        });
        apiClient.storeLoginCookies(driver.manage().getCookies());
        apiClient.setGlobalPositionHtml(driver.getPageSource());
        apiClient.setLogged(true);
        agentTemporaryStorage.remove(driver.getDriverId());
    }

    private void checkCredentials(Credentials credentials) {
        if (Strings.isNullOrEmpty(credentials.getField(Field.Key.USERNAME))
                || Strings.isNullOrEmpty(credentials.getField(Field.Key.PASSWORD))
                || Strings.isNullOrEmpty(credentials.getField(Field.Key.NATIONAL_ID_NUMBER))) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
    }

    private void doLogin(Credentials credentials) {

        // login page
        driver.get(Urls.RURALVIA_MOBILE_LOGIN);
        htmlLogger.info("Logging in");
        waitForLoad(5);

        // Search for the fields
        WebElement userField = driver.findElement(By.id(USER_FIELD)).findElement(USER_FIELD_INPUT);
        WebElement nationalIdNumber =
                driver.findElement(By.id(ID_FIELD)).findElement(USER_FIELD_INPUT);
        WebElement passwordField =
                driver.findElement(By.id(PASSWORD_FIELD)).findElement(USER_FIELD_INPUT);

        final String username = credentials.getField(Field.Key.USERNAME);
        final String IdNumber = credentials.getField(Field.Key.NATIONAL_ID_NUMBER);
        final String password = credentials.getField(Field.Key.PASSWORD);

        userField.sendKeys(username);
        nationalIdNumber.sendKeys(IdNumber);
        passwordField.sendKeys(password);

        driver.findElement(By.cssSelector(LoginForm.WEB_VIEW)).click();
        driver.findElement(By.id(LoginForm.ACCEPT_BUTTON)).click();

        waitForLoad(5);
        checkCorrectLogin();
    }

    /** Checks if appears a Login error in the DOM due a failed login */
    private void checkCorrectLogin() {

        try {
            if (driver.findElement(By.id("divErrorMessage")).isDisplayed()) {
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }
        } catch (NoSuchElementException e) {
            if (!driver.getPageSource().contains("<div id=\"HEADER\">Posición Global</div>")) {
                throw new GlobalPositionNotFoundException("Global Position not Found");
            }
        }
    }

    private void waitForLoad(int secondsToWait) {
        ExpectedCondition<Boolean> pageLoadCondition =
                drive ->
                        ((JavascriptExecutor) drive)
                                .executeScript("return document.readyState")
                                .equals("complete");

        WebDriverWait wait = new WebDriverWait(driver, secondsToWait);
        wait.until(pageLoadCondition);
        log.info("Page has been load successfully");
    }
}
