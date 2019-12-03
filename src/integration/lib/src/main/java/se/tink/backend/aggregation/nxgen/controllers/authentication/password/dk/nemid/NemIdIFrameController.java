package se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid;

import com.google.common.base.Strings;
import java.util.Base64;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.NemIdConstants.ErrorStrings;

// Temporarily renaming this to V2. V1 will be removed once the Nordea DK update is finished
public class NemIdIFrameController {

    private static final AggregationLogger LOGGER =
            new AggregationLogger(NemIdIFrameController.class);
    private static final Pattern PATTERN_INCORRECT_CREDENTIALS =
            Pattern.compile("^incorrect (user|password).*");
    private static final long WAIT_FOR_INIT_MILLIS = 1000;
    private static final long WAIT_FOR_RENDER_MILLIS = 5000;
    private static final long PHANTOMJS_TIMEOUT_SECONDS = 30;

    private static final By USERNAME_INPUT = By.cssSelector("input[type=text]");
    private static final By ERROR_MESSAGE = By.cssSelector("p.error");
    private static final By PASSWORD_INPUT = By.cssSelector("input[type=password]");
    static final By SUBMIT_BUTTON = By.cssSelector("button.button--submit");
    private static final By NEMID_TOKEN = By.cssSelector("div#tink_nemIdToken");
    private static final By IFRAME = By.tagName("iframe");

    private static final By NEMID_APP_BUTTON = By.cssSelector("button.button--submit");

    private WebDriver driver;
    private final WebdriverHelper webdriverHelper;

    public NemIdIFrameController() {
        this.webdriverHelper = new WebdriverHelper();
    }

    public void clickLogin() {
        webdriverHelper.clickButton(SUBMIT_BUTTON);
    }

    String doLoginWith(String username, String password, NemIdParameters nemIdParameters)
            throws AuthenticationException, AuthorizationException {
        try {
            driver = webdriverHelper.constructWebDriver();

            // The base URL MUST be `https://applet.danid.dk`. This is a hack to make it work,
            // phantomjs/selenium does
            // not have features for this. I expect we'd have to recompile phantomjs to accommodate
            // this feature.
            // How it works:
            //  1. Make a normal request to the base url
            //  2. Inject JavaScript code to replace the html contents (base64 encoded in order to
            // avoid string escaping)
            //  3. The new contents will execute and we can go forward

            webdriverHelper.initBrowser(nemIdParameters.getInitialUrl());

            /*
            // 2 - Inject javascript to do magic
            injectJavascript(nemIdParameters);

            Thread.sleep(5000);
             */

            // 3 - check all javascript executions are done
            // switchToIframe();
            webdriverHelper.switchToIframeAndExecuteJavascript(
                    IFRAME, USERNAME_INPUT, nemIdParameters);

            setUserName(username);
            setPassword(password);

            clickLogin();
            webdriverHelper.lookForErrorAndThrowIfFound();

            pollNemidApp();

            return collectToken();

        } catch (Exception e) {
            throw e;
        }
    }

    void pollNemidApp() {

        try {
            // TODO: change this to check if page is still loading
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        webdriverHelper.clickButton(NEMID_APP_BUTTON);
    }

    private String collectToken() throws AuthenticationException, AuthorizationException {
        // Try to get the token/errors multiple times. It (both token or error) might not have
        // loaded yet.

        driver.switchTo().defaultContent();
        for (int i = 0; i < 7; i++) {
            Optional<String> nemIdToken = getNemIdToken();

            if (nemIdToken.isPresent()) {
                return nemIdToken.get();
            }

            try {
                webdriverHelper.switchToIframe(IFRAME, USERNAME_INPUT);
                if (driver.getPageSource().contains(ErrorStrings.INVALID_CREDENTIALS)) {
                    driver.switchTo().defaultContent();
                    throw LoginError.INCORRECT_CREDENTIALS.exception();
                } else if (driver.getPageSource().contains(ErrorStrings.NEMID_NOT_ACTIVATED)) {
                    driver.switchTo().defaultContent();
                    throw LoginError.NO_ACCESS_TO_MOBILE_BANKING.exception();
                }
                driver.switchTo().defaultContent();
            } catch (IllegalStateException ex) {
                // If we cannot find iframe, switchToIframe method throws IllegalStateException
                // in this case we just want to try again so we do not throw exception
            }
        }

        // We will only reach this state if we could not find the nemId token -> something went
        // wrong in the authentication.
        throw new IllegalStateException("[nemid] Could not find nemId token.");
    }

    public void throwError(String errorText) throws LoginException {
        // Seen errors:
        // - "Incorrect user ID or password. Enter user ID and password. Changed your password
        // recently, perhaps?"
        // - "Incorrect password."
        String err = errorText.toLowerCase();

        Matcher matcher = PATTERN_INCORRECT_CREDENTIALS.matcher(err);
        if (matcher.matches()) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        throw new IllegalStateException(
                String.format("[nemid] Unknown login error '%s'.", errorText));
    }

    // TODO: implement retry here since the server can return 500
    private void injectJavascript(NemIdParameters nemIdParameters) {

        String html = String.format(NemIdConstants.BASE_HTML, nemIdParameters.getNemIdElements());
        String b64Html = Base64.getEncoder().encodeToString(html.getBytes());

        webdriverHelper.executeJavascript("document.write(atob(\"" + b64Html + "\"));");
    }

    private void setUserName(String username) {
        webdriverHelper.setValueToElement(username, USERNAME_INPUT);
    }

    private void setPassword(String password) {
        webdriverHelper.setValueToElement(password, PASSWORD_INPUT);
    }

    private Optional<String> getNemIdToken() {
        Optional<WebElement> tokenElement = webdriverHelper.waitForElement(NEMID_TOKEN);

        if (!tokenElement.isPresent()) {
            return Optional.empty();
        }
        return Optional.ofNullable(Strings.emptyToNull(tokenElement.get().getText()));
    }
}
