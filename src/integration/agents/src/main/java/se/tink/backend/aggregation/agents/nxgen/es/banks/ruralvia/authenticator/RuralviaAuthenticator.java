package se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.authenticator;

import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.LoginForm.ID_FIELD;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.LoginForm.PASSWORD_FIELD;

import com.google.common.base.Strings;
import java.lang.invoke.MethodHandles;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.LoginForm;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.Tags;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.SeleniumCommands;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;

public class RuralviaAuthenticator extends SeleniumCommands implements Authenticator {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private RuralviaApiClient apiClient;

    public RuralviaAuthenticator(RuralviaApiClient apiClient) {
        super();
        this.apiClient = apiClient;
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
        log.debug(driver.getPageSource());
        apiClient.storeLoginCookies(driver.manage().getCookies());
        apiClient.setGlobalPositionHtml(driver.getPageSource());
        quitWebDriver();
    }

    private void doLogin(Credentials credentials) {

        // login page
        driver.get(Urls.RURALVIA_MOBILE_LOGIN);
        log.debug("open login url: " + Urls.RURALVIA_MOBILE_LOGIN);

        // Search for the fields
        WebElement userField =
                driver.findElement(By.id(LoginForm.USER_FIELD))
                        .findElement(By.tagName(Tags.TAG_INPUT));
        WebElement nationalIdNumber =
                driver.findElement(By.id(ID_FIELD)).findElement(By.tagName(Tags.TAG_INPUT));
        WebElement passwordField =
                driver.findElement(By.id(PASSWORD_FIELD)).findElement(By.tagName(Tags.TAG_INPUT));

        final String username = credentials.getField(Field.Key.USERNAME);
        final String IdNumber = credentials.getField(Field.Key.NATIONAL_ID_NUMBER);
        final String password = credentials.getField(Field.Key.PASSWORD);

        userField.sendKeys(username);
        nationalIdNumber.sendKeys(IdNumber);
        passwordField.sendKeys(password);

        driver.findElement(By.cssSelector(LoginForm.WEB_VIEW)).click();
        driver.findElement(By.id(LoginForm.ACCEPT_BUTTON)).click();

        // driver.findElement(By.xpath(".//input[@name='botoncico']")).sendKeys(Keys.ENTER);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            log.error("Error in Authenticator Thread sleep", e);
            Thread.currentThread().interrupt();
        }
        waitForLoad();
        // checkCorrectLogin();
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
}
