package se.tink.backend.aggregation.agents.nxgen.de.banks.fidor.authenticator;

import java.io.File;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fidor.FidorApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fidor.FidorConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.URL;

public class FidorPasswordAutenticator implements PasswordAuthenticator {

    private FidorApiClient client;
    private static final File phantomJsFile;

    public FidorPasswordAutenticator(FidorApiClient client){
        this.client = client;
    }

    static {
        boolean mac = System.getProperty("os.name").toLowerCase().contains("mac");

        if (mac) {
            phantomJsFile = new File("tools/phantomjs-tink-mac64-2.1.1");
        } else {
            phantomJsFile = new File("tools/phantomjs-tink-linux-x86_64-1.9.8");
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
        capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "userAgent",
                DanskeBankConstants.Javascript.USER_AGENT);
        return new PhantomJSDriver(capabilities);
    }

    private String getCode(WebDriver driver, String clientId, String state, String redirectUrl, String username, String password){
        String url = new URL(FidorConstants.URL.OPENAPI.SANDBOX_BASE + FidorConstants.URL.OPENAPI.OAUTH_AUTHORIZE)
                .queryParam(FidorConstants.QUERYPARAMS.CLIENT_ID, clientId)
                .queryParam(FidorConstants.QUERYPARAMS.REDRIECT_URI, redirectUrl)
                .queryParam(FidorConstants.QUERYPARAMS.STATE, state)
                .queryParam(FidorConstants.QUERYPARAMS.RESPONSE_TYPE, FidorConstants.QUERYPARAMS.RESPONSE_TYPE_CODE)
                .get();

        driver.navigate().to(url);

        WebElement emailField = driver.findElement(By.id(FidorConstants.FORM.EMAIL_ID));
        emailField.sendKeys(username);
        WebElement passwordField = driver.findElement(By.id(FidorConstants.FORM.PASSWORD_ID));
        passwordField.sendKeys(password);
        WebElement submitButton = driver.findElement(By.name(FidorConstants.FORM.SUBMIT_NAME));
        submitButton.click();

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
