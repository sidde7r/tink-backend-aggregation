package se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.authenticator;

import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.LoginForm.ID_FIELD;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.LoginForm.PASSWORD_FIELD;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.LoginForm.USER_FIELD;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.USER_FIELD_INPUT;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.LoginForm;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.Urls;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.integration.webdriver.ChromeDriverInitializer;

@Slf4j
public class RuralviaAuthenticator implements Authenticator {

    private RuralviaApiClient apiClient;
    private WebDriver driver;

    public RuralviaAuthenticator(RuralviaApiClient apiClient) {
        super();
        this.apiClient = apiClient;
        this.driver = createDriver();
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {

        if (Strings.isNullOrEmpty(credentials.getField(Field.Key.USERNAME))
                || Strings.isNullOrEmpty(credentials.getField(Field.Key.PASSWORD))
                || Strings.isNullOrEmpty(credentials.getField(Field.Key.NATIONAL_ID_NUMBER))) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        doLogin(credentials);
        apiClient.storeLoginCookies(driver.manage().getCookies());
        apiClient.setGlobalPositionHtml(driver.getPageSource());
        driver.close();
        driver.quit();
    }

    private void doLogin(Credentials credentials) {

        // login page
        driver.get(Urls.RURALVIA_MOBILE_LOGIN);
        log.debug("open login url: " + Urls.RURALVIA_MOBILE_LOGIN);
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

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        waitForLoad(5);
        checkCorrectLogin();
    }

    /** Checks if appears a Login error in the DOM due a failed login */
    private void checkCorrectLogin() {
        boolean showsError;
        try {
            showsError = driver.findElement(By.id("divErrorMessage")).isDisplayed();

        } catch (NoSuchElementException e) {
            showsError = false;
        }

        if (showsError
                || !driver.getPageSource().contains("<div id=\"HEADER\">Posición Global</div>")) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
    }

    public WebDriver createDriver() {
        return ChromeDriverInitializer.constructChromeDriver(
                HeaderValues.USER_AGENT, HeaderValues.ACCEPT_LANGUAGE, null);
    }

    public void waitForLoad(int secondsToWait) {
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
