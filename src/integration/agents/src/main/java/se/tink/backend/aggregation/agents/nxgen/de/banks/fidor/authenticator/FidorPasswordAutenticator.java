package se.tink.backend.aggregation.agents.nxgen.de.banks.fidor.authenticator;

import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fidor.FidorApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fidor.FidorConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.integration.webdriver.WebDriverInitializer;

public class FidorPasswordAutenticator implements PasswordAuthenticator {

    private FidorApiClient client;
    private Logger logger = LoggerFactory.getLogger(FidorPasswordAutenticator.class);

    public FidorPasswordAutenticator(FidorApiClient client) {
        this.client = client;
    }

    private String getCode(
            WebDriver driver,
            String clientId,
            String state,
            String redirectUrl,
            String username,
            String password)
            throws LoginException {
        String url =
                new URL(
                                FidorConstants.URL.OPENAPI.SANDBOX_BASE
                                        + FidorConstants.URL.OPENAPI.OAUTH_AUTHORIZE)
                        .queryParam(FidorConstants.QUERYPARAMS.CLIENT_ID, clientId)
                        .queryParam(FidorConstants.QUERYPARAMS.REDRIECT_URI, redirectUrl)
                        .queryParam(FidorConstants.QUERYPARAMS.STATE, state)
                        .queryParam(
                                FidorConstants.QUERYPARAMS.RESPONSE_TYPE,
                                FidorConstants.QUERYPARAMS.RESPONSE_TYPE_CODE)
                        .get();

        driver.navigate().to(url);

        WebElement emailField = null;
        WebElement passwordField = null;
        WebElement submitButton = null;
        try {
            emailField = driver.findElement(By.id(FidorConstants.FORM.EMAIL_ID));
            passwordField = driver.findElement(By.id(FidorConstants.FORM.PASSWORD_ID));
            submitButton = driver.findElement(By.name(FidorConstants.FORM.SUBMIT_NAME));
        } catch (org.openqa.selenium.NoSuchElementException e) {
            logger.error(
                    "{} Selenium could not find element",
                    FidorConstants.LOGGING.AUTHENTICATION_ERROR,
                    e);
            throw LoginError.DEFAULT_MESSAGE.exception();
        }

        emailField.sendKeys(username);
        passwordField.sendKeys(password);
        submitButton.click();

        if (StringUtils.containsIgnoreCase(
                driver.getPageSource(), FidorConstants.ERROR.INVALID_CREDENTIALS)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);

        return driver.getCurrentUrl().split("([=&])")[1];
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {
        WebDriver driver = WebDriverInitializer.constructWebDriver();
        String code =
                getCode(
                        driver,
                        FidorConstants.SANBOX_CLIENT_ID,
                        FidorConstants.STATE,
                        FidorConstants.SANDBOX_REDIRECT_URL,
                        username,
                        password);
        client.getOpenApiToken(
                FidorConstants.SANDBOX_BASE64_BASIC_AUTH,
                code,
                FidorConstants.SANDBOX_REDIRECT_URL,
                FidorConstants.SANBOX_CLIENT_ID);
    }
}
