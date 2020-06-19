package se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.util.Base64;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.libraries.serialization.utils.SerializationUtils;

// Temporarily renaming this to V2. V1 will be removed once the Nordea DK update is finished
public class NemIdIFrameController {

    private static final Logger log = LoggerFactory.getLogger(NemIdIFrameController.class);

    // NemId Javascript Client Integration for mobile:
    // https://www.nets.eu/dk-da/kundeservice/nemid-tjenesteudbyder/NemID-tjenesteudbyderpakken/Documents/NemID%20Integration%20-%20Mobile.pdf

    private static final ImmutableList<Pattern> INCORRECT_CREDENTIALS_ERROR_PATTERNS =
            ImmutableList.<Pattern>builder()
                    .add(
                            Pattern.compile("^incorrect (user|password).*"),
                            Pattern.compile("^fejl i (bruger|adgangskode).*"),
                            Pattern.compile("^indtast (bruger|adgangskode).*"))
                    .build();

    private static final By USERNAME_INPUT = By.cssSelector("input[type=text]");
    private static final By ERROR_MESSAGE = By.cssSelector("p.error");
    private static final By PASSWORD_INPUT = By.cssSelector("input[type=password]");
    private static final By SUBMIT_BUTTON = By.cssSelector("button.button--submit");
    private static final By NEMID_TOKEN = By.cssSelector("div#tink_nemIdToken");
    private static final By IFRAME = By.tagName("iframe");
    private static final By OTP_ICON = By.className("otp__icon-phone-pulse");

    private static final By NEMID_APP_BUTTON = By.cssSelector("button.button--submit");

    private static final long PHANTOMJS_TIMEOUT_SECONDS = 30;

    private final WebdriverHelper webdriverHelper;
    private final Sleeper sleeper;
    private final NemIdParametersFetcher nemIdParametersFetcher;
    private final SupplementalRequester supplementalRequester;

    public NemIdIFrameController(
            final NemIdParametersFetcher nemIdParametersFetcher,
            final SupplementalRequester supplementalRequester) {
        this(new WebdriverHelper(), new Sleeper(), nemIdParametersFetcher, supplementalRequester);
    }

    NemIdIFrameController(
            final WebdriverHelper webdriverHelper,
            final Sleeper sleeper,
            final NemIdParametersFetcher nemIdParametersFetcher,
            final SupplementalRequester supplementalRequester) {
        this.webdriverHelper = webdriverHelper;
        this.sleeper = sleeper;
        this.nemIdParametersFetcher = nemIdParametersFetcher;
        this.supplementalRequester = supplementalRequester;
    }

    public String doLoginWith(Credentials credentials) throws AuthenticationException {
        log.info("Start authentication process with nem-id iframe.");
        WebDriver driver = webdriverHelper.constructWebDriver(PHANTOMJS_TIMEOUT_SECONDS);
        try {
            // inject nemId form into iframe
            instantiateIFrameWithNemIdForm(driver);
            log.info("NemId iframe is initialized");

            // provide credentials and submit
            setUserName(driver, credentials.getField(Field.Key.USERNAME));
            setPassword(driver, credentials.getField(Field.Key.PASSWORD));
            clickLogin(driver);

            // validate response
            validateCredentials(driver);
            log.info("Provided credentials are valid.");

            displayPrompt(credentials);

            final long askForNemIdStartTime = System.currentTimeMillis();
            // credentials are valid let's ask for 2nd factor
            pollNemidApp(driver);

            // wait some time for user's 2nd factor and token
            waitForNemidToken(driver);
            log.info(
                    "Whole 2fa process took {} ms.",
                    System.currentTimeMillis() - askForNemIdStartTime);

            return collectToken(driver);
        } finally {
            driver.quit();
        }
    }

    private void instantiateIFrameWithNemIdForm(WebDriver driver) throws AuthenticationException {
        if (!isNemIdInitialized(driver)) {
            throw new IllegalStateException("Can't instantiate iframe element with NemId form.");
        }
    }

    private boolean isNemIdInitialized(WebDriver driver) throws AuthenticationException {
        for (int i = 0; i < 5; i++) {
            NemIdParametersV2 nemIdParameters = nemIdParametersFetcher.getNemIdParameters();

            // this will setup browser with values specific to nemid page, like current url, etc.
            driver.get(nemIdParameters.getInitialUrl().get());

            // create initial html to inject
            String html =
                    String.format(NemIdConstantsV2.BASE_HTML, nemIdParameters.getNemIdElements());
            String b64Html = Base64.getEncoder().encodeToString(html.getBytes());

            if (isNemIdInitialized(driver, b64Html)) {
                driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
                return true;
            }
        }
        return false;
    }

    private boolean isNemIdInitialized(WebDriver driver, String b64Html) {
        driver.switchTo().defaultContent();

        ((JavascriptExecutor) driver).executeScript("document.write(atob(\"" + b64Html + "\"));");

        sleeper.sleepFor(5_000);

        driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);

        boolean result =
                webdriverHelper
                        .waitForElement(driver, IFRAME)
                        .map(
                                element -> {
                                    driver.switchTo().frame(element);
                                    return webdriverHelper
                                            .waitForElement(driver, USERNAME_INPUT)
                                            .isPresent();
                                })
                        .orElse(false);
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        return result;
    }

    private void validateCredentials(WebDriver driver) throws LoginException {
        boolean isValid = false;

        for (int i = 0; i < 20; i++) {
            checkForErrorMessage(driver);
            if (isApproveWithKeyAppPageRendered(driver)) {
                isValid = true;
                break;
            }
            sleeper.sleepFor(1_000);
        }

        if (!isValid) {
            throw LoginError.INVALIDATED_CREDENTIALS.exception("Can't validate NemId credentials.");
        }
    }

    private void checkForErrorMessage(WebDriver driver) throws LoginException {
        Optional<String> errorText =
                webdriverHelper
                        .waitForElement(driver, ERROR_MESSAGE)
                        .map(WebElement::getText)
                        .filter(e -> !e.isEmpty());
        if (errorText.isPresent()) {
            throwError(errorText.get());
        }
    }

    private boolean isApproveWithKeyAppPageRendered(WebDriver driver) {
        Optional<WebElement> otpIconPhone = webdriverHelper.waitForElement(driver, OTP_ICON);
        return otpIconPhone.isPresent();
    }

    private void waitForNemidToken(WebDriver driver) throws LoginException {
        boolean isNemIdApproved = false;
        for (int i = 0; i < 120; i++) {

            Optional<WebElement> otpIconPhone = webdriverHelper.waitForElement(driver, OTP_ICON);
            if (!otpIconPhone.isPresent()) {
                isNemIdApproved = true;
                break;
            }
            sleeper.sleepFor(1_000);
        }

        if (!isNemIdApproved) {
            throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception(
                    "NemID request was not approved.");
        }
    }

    private String collectToken(WebDriver driver) {
        driver.switchTo().defaultContent();
        for (int i = 0; i < 7; i++) {
            Optional<String> nemIdToken = getNemIdToken(driver);

            if (nemIdToken.isPresent()) {
                return nemIdToken.get();
            }
        }
        throw new IllegalStateException("Could not find nemId token.");
    }

    private void throwError(String errorText) throws LoginException {
        String err = errorText.toLowerCase();

        if (INCORRECT_CREDENTIALS_ERROR_PATTERNS.stream()
                .map(p -> p.matcher(err))
                .anyMatch(Matcher::matches)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception(err);
        }

        throw new IllegalStateException(String.format("Unknown login error '%s'.", errorText));
    }

    private void setUserName(WebDriver driver, String username) {
        webdriverHelper.setValueToElement(driver, username, USERNAME_INPUT);
    }

    private void setPassword(WebDriver driver, String password) {
        webdriverHelper.setValueToElement(driver, password, PASSWORD_INPUT);
    }

    private void clickLogin(WebDriver driver) {
        webdriverHelper.clickButton(driver, SUBMIT_BUTTON);
    }

    private void pollNemidApp(WebDriver driver) {
        webdriverHelper.clickButton(driver, NEMID_APP_BUTTON);
    }

    private Optional<String> getNemIdToken(WebDriver driver) {
        Optional<WebElement> tokenElement = webdriverHelper.waitForElement(driver, NEMID_TOKEN);
        return tokenElement.map(webElement -> Strings.emptyToNull(webElement.getText()));
    }

    private void displayPrompt(Credentials credentials) {
        Field field =
                Field.builder()
                        .immutable(true)
                        .description("Please open the NemId app and confirm login.")
                        .value("Please open the NemId app and confirm login")
                        .name("name")
                        .build();

        credentials.setSupplementalInformation(
                SerializationUtils.serializeToString(Collections.singletonList(field)));
        credentials.setStatus(CredentialsStatus.AWAITING_SUPPLEMENTAL_INFORMATION);

        supplementalRequester.requestSupplementalInformation(credentials, false);
    }
}
