package se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdConstantsV2.Errors.ENTER_ACTIVATION_PASSWORD;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdConstantsV2.Errors.INCORRECT_CREDENTIALS_ERROR_PATTERNS;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdConstantsV2.HtmlElements.ERROR_MESSAGE;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdConstantsV2.HtmlElements.IFRAME;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdConstantsV2.HtmlElements.NEMID_APP_BUTTON;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdConstantsV2.HtmlElements.NEMID_CODE_CARD;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdConstantsV2.HtmlElements.NEMID_CODE_TOKEN;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdConstantsV2.HtmlElements.NEMID_TOKEN;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdConstantsV2.HtmlElements.OTP_ICON;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdConstantsV2.HtmlElements.PASSWORD_INPUT;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdConstantsV2.HtmlElements.SUBMIT_BUTTON;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdConstantsV2.HtmlElements.USERNAME_INPUT;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdConstantsV2.NEM_ID_PREFIX;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdConstantsV2.PHANTOMJS_TIMEOUT_SECONDS;

import com.google.common.base.Strings;
import java.util.Base64;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.utils.supplementalfields.DanishFields;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.NemIdCodeAppConstants.UserMessage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.exception.NemIdError;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Slf4j
@RequiredArgsConstructor
public class NemIdIFrameController {
    // NemId Javascript Client Integration for mobile:
    // https://www.nets.eu/dk-da/kundeservice/nemid-tjenesteudbyder/NemID-tjenesteudbyderpakken/Documents/NemID%20Integration%20-%20Mobile.pdf

    private final WebdriverHelper webdriverHelper;
    private final Sleeper sleeper;
    private final NemIdParametersFetcher nemIdParametersFetcher;
    private final SupplementalRequester supplementalRequester;
    private final Catalog catalog;
    private final StatusUpdater statusUpdater;

    public NemIdIFrameController(
            NemIdParametersFetcher nemIdParametersFetcher,
            SupplementalRequester supplementalRequester,
            Catalog catalog,
            StatusUpdater statusUpdater) {
        this(
                new WebdriverHelper(),
                new Sleeper(),
                nemIdParametersFetcher,
                supplementalRequester,
                catalog,
                statusUpdater);
    }

    public String doLoginWith(Credentials credentials) throws AuthenticationException {
        log.info("{} Start authentication process with nem-id iframe.", NEM_ID_PREFIX);
        WebDriver driver = webdriverHelper.constructWebDriver(PHANTOMJS_TIMEOUT_SECONDS);
        try {
            initNemIdIFrame(credentials, driver);
            login(credentials, driver);
            validateResponse(credentials, driver);

            final long askForNemIdStartTime = System.currentTimeMillis();
            // credentials are valid let's ask for 2nd factor
            sendNemIdApprovalNotification(driver);
            displayPromptToOpenNemIdApp(credentials);
            return waitFor2ndFactorAndGetToken(driver, askForNemIdStartTime);

        } finally {
            driver.quit();
        }
    }

    private void initNemIdIFrame(Credentials credentials, WebDriver driver) {
        instantiateIFrameWithNemIdForm(driver);
        log.info("{} iframe is initialized", NEM_ID_PREFIX);

        updateStatusPayload(credentials, catalog.getString(UserMessage.NEM_ID_PROCESS_INIT));
    }

    public void login(Credentials credentials, WebDriver driver) {
        setUserName(driver, credentials.getField(Field.Key.USERNAME));
        setPassword(driver, credentials.getField(Field.Key.PASSWORD));
        clickLogin(driver);

        updateStatusPayload(credentials, catalog.getString(UserMessage.VERIFYING_CREDS));
    }

    private void validateResponse(Credentials credentials, WebDriver driver) {
        validateCredentials(driver);
        log.info("{} Provided credentials are valid.", NEM_ID_PREFIX);

        updateStatusPayload(credentials, catalog.getString(UserMessage.VALID_CREDS));
    }

    private String waitFor2ndFactorAndGetToken(WebDriver driver, long askForNemIdStartTime) {
        String nemIdToken = verifyOtpAndTryToGetNemIdToken(driver);

        log.info(
                "{} Whole 2fa process took {} ms.",
                NEM_ID_PREFIX,
                System.currentTimeMillis() - askForNemIdStartTime);

        return nemIdToken != null ? nemIdToken : collectToken(driver);
    }

    private void instantiateIFrameWithNemIdForm(WebDriver driver) throws AuthenticationException {
        if (!isNemIdInitialized(driver)) {
            throw new IllegalStateException(
                    NEM_ID_PREFIX + " Can't instantiate iframe element with NemId form.");
        }
    }

    private boolean isNemIdInitialized(WebDriver driver) throws AuthenticationException {
        for (int i = 0; i < 5; i++) {
            NemIdParametersV2 nemIdParameters = nemIdParametersFetcher.getNemIdParameters();

            // this will setup browser with values specific to nemid page, like current url, etc.
            driver.get(NemIdConstantsV2.NEM_ID_APPLET_URL);

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

        for (int i = 0; i < 120; i++) {
            checkForErrorMessage(driver);
            if (isApproveWithKeyAppPageRendered(driver)) {
                isValid = true;
                break;
            }
            sleeper.sleepFor(1_000);
        }

        if (!isValid) {
            tryThrowingNoCodeAppException(driver);
            throwNemIdCredentialsError(driver);
        }
    }

    private void checkForErrorMessage(WebDriver driver) throws LoginException {
        Optional<String> errorText =
                webdriverHelper
                        .waitForElement(driver, ERROR_MESSAGE)
                        .map(WebElement::getText)
                        .filter(e -> !e.isEmpty());
        if (errorText.isPresent()) {
            log.error(
                    "{} Error occured when validating NemID credentials for page: {}",
                    NEM_ID_PREFIX,
                    driver.getPageSource());
            throwError(errorText.get());
        }
    }

    private boolean isApproveWithKeyAppPageRendered(WebDriver driver) {
        return webdriverHelper.waitForElement(driver, OTP_ICON).isPresent();
    }

    private void tryThrowingNoCodeAppException(WebDriver driver) {
        if (isNemIdSuggestingCodeCard(driver)) {
            throw NemIdError.CODEAPP_NOT_REGISTERED.exception(
                    NEM_ID_PREFIX + " User has code card.");
        }
        if (isNemIdSuggestingCodeToken(driver)) {
            throw NemIdError.CODEAPP_NOT_REGISTERED.exception(
                    NEM_ID_PREFIX + " User has code token.");
        }
    }

    private boolean isNemIdSuggestingCodeCard(WebDriver driver) {
        return webdriverHelper.waitForElement(driver, NEMID_CODE_CARD).isPresent();
    }

    private boolean isNemIdSuggestingCodeToken(WebDriver driver) {
        return webdriverHelper.waitForElement(driver, NEMID_CODE_TOKEN).isPresent();
    }

    private void throwNemIdCredentialsError(WebDriver driver) {
        Optional<WebElement> maybeNemIdIframeElement =
                webdriverHelper.waitForElement(driver, IFRAME);
        if (maybeNemIdIframeElement.isPresent()) {
            /*
            ITE-1859
            We observe a lot of credential verification errors with empty NemId iframe content in logged page source.
            This means that iframe is probably not selected by driver (driver.switchTo()) - otherwise we won't see
            it's <iframe> tag at all. There is no point in code where we deliberately switch to parent window so
            we have to investigate it.
            */
            String currentPageSource = driver.getPageSource();
            String nemIdIframePageSource =
                    driver.switchTo().frame(maybeNemIdIframeElement.get()).getPageSource();
            log.warn(
                    "{} NemId iframe is not selected, please verify current page source: {} and NemId iframe source: {}",
                    NEM_ID_PREFIX,
                    currentPageSource,
                    nemIdIframePageSource);
        } else {
            log.warn(
                    "{} Can't validate NemId = please verify page source: {}",
                    NEM_ID_PREFIX,
                    driver.getPageSource());
        }

        throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception(
                NEM_ID_PREFIX + " Can't validate NemId credentials.");
    }

    private String collectToken(WebDriver driver) {
        driver.switchTo().defaultContent();
        String token = waitForNemIdToken(driver);
        if (token == null) {
            log.error(
                    "{} Failed to obtain NemID token, please verify page source: {}",
                    NEM_ID_PREFIX,
                    driver.getPageSource());
            throw NemIdError.TIMEOUT.exception();
        }
        return token;
    }

    private String waitForNemIdToken(WebDriver driver) {
        for (int i = 0; i < 7; i++) {
            Optional<String> nemIdToken = getNemIdToken(driver);

            if (nemIdToken.isPresent()) {
                return nemIdToken.get();
            }
        }
        return null;
    }

    private void throwError(String errorText) throws LoginException {
        String err = errorText.toLowerCase();

        if (INCORRECT_CREDENTIALS_ERROR_PATTERNS.stream()
                .map(p -> p.matcher(err))
                .anyMatch(Matcher::matches)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception(NEM_ID_PREFIX + err);
        } else if (ENTER_ACTIVATION_PASSWORD.equalsIgnoreCase(err)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception(
                    new LocalizableKey(ENTER_ACTIVATION_PASSWORD));
        } else {
            throw new IllegalStateException(NEM_ID_PREFIX + " Unknown login error: " + errorText);
        }
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

    private void sendNemIdApprovalNotification(WebDriver driver) {
        webdriverHelper.clickButton(driver, NEMID_APP_BUTTON);
    }

    private Optional<String> getNemIdToken(WebDriver driver) {
        Optional<WebElement> tokenElement = webdriverHelper.waitForElement(driver, NEMID_TOKEN);
        return tokenElement.map(webElement -> Strings.emptyToNull(webElement.getText()));
    }

    private void displayPromptToOpenNemIdApp(Credentials credentials) {
        updateStatusPayload(
                credentials, catalog.getString(UserMessage.OPEN_NEM_ID_APP_AND_CLICK_BUTTON));

        Field field = DanishFields.NemIdInfo.build(catalog);

        credentials.setSupplementalInformation(
                SerializationUtils.serializeToString(Collections.singletonList(field)));
        credentials.setStatus(CredentialsStatus.AWAITING_SUPPLEMENTAL_INFORMATION);

        supplementalRequester.requestSupplementalInformation(credentials, true);
    }

    private String verifyOtpAndTryToGetNemIdToken(WebDriver driver) {
        // According to recorded page sources there is a possibility that
        // opt icon is not presented, but nemIdToken is successfully rendered
        String nemIdToken = null;
        boolean hasOTPIconAppeared = waitForOTPIcon(driver);
        if (!hasOTPIconAppeared) {
            nemIdToken = waitForNemIdToken(driver);
            if (nemIdToken == null) {
                throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception(
                        NEM_ID_PREFIX + " NemID request was not approved.");
            }
        }
        return nemIdToken;
    }

    private boolean waitForOTPIcon(WebDriver driver) throws LoginException {
        boolean hasOTPIconAppeared = false;
        for (int i = 0; i < 120; i++) {

            Optional<WebElement> otpIconPhone = webdriverHelper.waitForElement(driver, OTP_ICON);
            if (!otpIconPhone.isPresent()) {
                hasOTPIconAppeared = true;
                break;
            }
            sleeper.sleepFor(1_000);
        }

        return hasOTPIconAppeared;
    }

    private void updateStatusPayload(final Credentials credentials, final String message) {
        log.info(
                "Updating payload: {} (current credential status: {})",
                message,
                credentials.getStatus());
        statusUpdater.updateStatus(credentials.getStatus(), message);
    }
}
