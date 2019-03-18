package se.tink.backend.aggregation.agents.nxgen.de.banks.fidor.authenticator;

import java.io.File;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fidor.FidorApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fidor.FidorConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.http.URL;

public class FidorPasswordAutenticator implements PasswordAuthenticator {

    private FidorApiClient client;
    private static final File phantomJsFile;
    private Logger logger = LoggerFactory.getLogger(FidorPasswordAutenticator.class);

    public FidorPasswordAutenticator(FidorApiClient client){
        this.client = client;
    }

    static {
        boolean mac = System.getProperty("os.name").toLowerCase().contains("mac");

        if (mac) {
            phantomJsFile = new File("tools/phantomjs-tink-mac64-2.1.1");
        } else {
            phantomJsFile = new File("tools/phantomjs-tink-linux-x86_64-2.1.1");
        }
    }

    private WebDriver createWebdriver(){
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
                phantomJsFile.getAbsolutePath());

        capabilities.setCapability(CapabilityType.TAKES_SCREENSHOT, false);
        capabilities.setCapability(CapabilityType.SUPPORTS_ALERTS, false);

        String[] phantomArgs = new String[] {
                // To allow iframe-hacking
                "--web-security=false",
                // No need to load images
                "--load-images=false",
                // For debugging, activate these:
                //"--webdriver-loglevel=DEBUG",
                //"--debug=true",
        };
        capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, phantomArgs);
        return new PhantomJSDriver(capabilities);
    }

    private String getCode(WebDriver driver, String clientId, String state, String redirectUrl, String username, String password)
            throws LoginException {
        String url = new URL(FidorConstants.URL.OPENAPI.SANDBOX_BASE + FidorConstants.URL.OPENAPI.OAUTH_AUTHORIZE)
                .queryParam(FidorConstants.QUERYPARAMS.CLIENT_ID, clientId)
                .queryParam(FidorConstants.QUERYPARAMS.REDRIECT_URI, redirectUrl)
                .queryParam(FidorConstants.QUERYPARAMS.STATE, state)
                .queryParam(FidorConstants.QUERYPARAMS.RESPONSE_TYPE, FidorConstants.QUERYPARAMS.RESPONSE_TYPE_CODE)
                .get();

        driver.navigate().to(url);

        WebElement emailField = null;
        WebElement passwordField = null;
        WebElement submitButton = null;
        try{
            emailField = driver.findElement(By.id(FidorConstants.FORM.EMAIL_ID));
            passwordField = driver.findElement(By.id(FidorConstants.FORM.PASSWORD_ID));
            submitButton = driver.findElement(By.name(FidorConstants.FORM.SUBMIT_NAME));
        }
        catch (org.openqa.selenium.NoSuchElementException e){
            logger.error("{} Selenium could not find element: {}", FidorConstants.LOGGING.AUTHENTICATION_ERROR, e);
        }

        emailField.sendKeys(username);
        passwordField.sendKeys(password);
        submitButton.click();

        if(StringUtils.containsIgnoreCase(driver.getPageSource(), FidorConstants.ERROR.INVALID_CREDENTIALS)){
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);

        String code = driver.getCurrentUrl().split("(=|&)")[1];
        return code;
    }

    @Override
    public void authenticate(String username, String password) throws AuthenticationException, AuthorizationException {
        WebDriver driver = createWebdriver();
        String code = getCode(driver, FidorConstants.SANBOX_CLIENT_ID, FidorConstants.STATE, FidorConstants.SANDBOX_REDIRECT_URL, username, password);
        client.getOpenApiToken(FidorConstants.SANDBOX_BASE64_BASIC_AUTH, code, FidorConstants.SANDBOX_REDIRECT_URL, FidorConstants.SANBOX_CLIENT_ID);
    }
}
