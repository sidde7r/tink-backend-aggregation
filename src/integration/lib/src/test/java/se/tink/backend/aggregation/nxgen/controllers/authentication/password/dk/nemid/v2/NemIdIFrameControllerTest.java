package se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.InOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebDriver.Timeouts;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.libraries.i18n.Catalog;

public class NemIdIFrameControllerTest {

    private static final long PHANTOMJS_TIMEOUT_SECONDS = 30L;
    private static final String USERNAME = "--- SAMPLE USERNAME ---";
    private static final String PASSWORD = "--- SAMPLE PASSWORD ---";
    private static final String NEMID_PARAMS = "--- NEM ID PARAMS ---";
    private static final String SAMPLE_TOKEN = "--- SAMPLE TOKEN ---";
    private static final NemIdParametersV2 NEM_ID_PARAMETERS_V_2 =
            new NemIdParametersV2(NEMID_PARAMS);

    private WebdriverHelper webdriverHelper;
    private Sleeper sleeper;
    private NemIdAuthenticatorV2 authenticator;
    private PhantomJSDriver driver;
    private SupplementalRequester supplementalRequester;

    private NemIdIFrameController controller;

    private static final By IFRAME = By.tagName("iframe");
    private static final By USERNAME_INPUT = By.cssSelector("input[type=text]");
    private static final By PASSWORD_INPUT = By.cssSelector("input[type=password]");
    private static final By SUBMIT_BUTTON = By.cssSelector("button.button--submit");
    private static final By ERROR_MESSAGE = By.cssSelector("p.error");
    private static final By OTP_ICON = By.className("otp__icon-phone-pulse");
    private static final By NEMID_APP_BUTTON = By.cssSelector("button.button--submit");
    private static final By NEMID_TOKEN = By.cssSelector("div#tink_nemIdToken");

    private InOrder inOrder;

    // web page elements
    private WebElement iframeMock;
    private WebElement userinputMock;
    private WebElement errorMessageMock;
    private WebElement otpIconMock;
    private WebElement nemIdTokenMock;

    private Credentials credentials;

    @SneakyThrows
    @Before
    public void setUp() {
        sleeper = mock(Sleeper.class);

        TargetLocator targetLocator = mock(TargetLocator.class);
        Timeouts timeouts = mock(Timeouts.class);

        driver = mock(PhantomJSDriver.class, Answers.RETURNS_DEEP_STUBS);
        given(driver.switchTo()).willReturn(targetLocator);
        given(driver.manage().timeouts()).willReturn(timeouts);

        webdriverHelper = mock(WebdriverHelper.class);

        given(webdriverHelper.constructWebDriver(PHANTOMJS_TIMEOUT_SECONDS)).willReturn(driver);

        supplementalRequester = mock(SupplementalRequester.class);

        inOrder =
                inOrder(
                        webdriverHelper,
                        driver,
                        targetLocator,
                        timeouts,
                        sleeper,
                        supplementalRequester);

        authenticator = mock(NemIdAuthenticatorV2.class);

        given(authenticator.getNemIdParameters()).willReturn(NEM_ID_PARAMETERS_V_2);

        controller =
                new NemIdIFrameController(
                        webdriverHelper,
                        sleeper,
                        authenticator,
                        supplementalRequester,
                        Catalog.getCatalog("en"));

        initializeWebElements();

        credentials = mock(Credentials.class);
        given(credentials.getField(Field.Key.USERNAME)).willReturn(USERNAME);
        given(credentials.getField(Field.Key.PASSWORD)).willReturn(PASSWORD);
    }

    private void initializeWebElements() {
        // iframe element
        iframeMock = mock(WebElement.class);
        given(webdriverHelper.waitForElement(driver, IFRAME)).willReturn(Optional.of(iframeMock));

        // user text input element
        userinputMock = mock(WebElement.class);
        given(webdriverHelper.waitForElement(driver, USERNAME_INPUT))
                .willReturn(Optional.of(userinputMock));

        // error span placeholder for error message
        errorMessageMock = mock(WebElement.class);
        given(webdriverHelper.waitForElement(driver, ERROR_MESSAGE))
                .willReturn(Optional.of(errorMessageMock));

        // otp icon placeholder
        otpIconMock = mock(WebElement.class);
        given(webdriverHelper.waitForElement(driver, OTP_ICON))
                .willReturn(Optional.of(otpIconMock))
                .willReturn(Optional.empty());

        // nemid token div
        nemIdTokenMock = mock(WebElement.class);
        given(nemIdTokenMock.getText()).willReturn(SAMPLE_TOKEN);
        given(webdriverHelper.waitForElement(driver, NEMID_TOKEN))
                .willReturn(Optional.of(nemIdTokenMock));
    }

    @Test
    public void doLoginWithSucceedAndReturnsToken() throws AuthenticationException {
        // given

        // when
        String result = controller.doLoginWith(credentials);

        // then
        // create webdriver and initialize it with danid.dk page
        inOrder.verify(webdriverHelper).constructWebDriver(PHANTOMJS_TIMEOUT_SECONDS);
        inOrder.verify(driver).get("https://applet.danid.dk");

        // initial nemid iframe with content
        inOrder.verify(driver.switchTo()).defaultContent();
        inOrder.verify(driver)
                .executeScript("document.write(atob(\"" + nemIdParamsToBase64Html() + "\"));");
        inOrder.verify(sleeper).sleepFor(5_000);
        inOrder.verify(webdriverHelper).waitForElement(driver, IFRAME);
        inOrder.verify(driver.switchTo()).frame(iframeMock);
        inOrder.verify(webdriverHelper).waitForElement(driver, USERNAME_INPUT);

        inOrder.verify(driver.manage().timeouts()).implicitlyWait(0, TimeUnit.SECONDS);

        // fulfill nemid form and submit
        inOrder.verify(webdriverHelper).setValueToElement(driver, USERNAME, USERNAME_INPUT);
        inOrder.verify(webdriverHelper).setValueToElement(driver, PASSWORD, PASSWORD_INPUT);
        inOrder.verify(webdriverHelper).clickButton(driver, SUBMIT_BUTTON);

        // verify response
        inOrder.verify(webdriverHelper).waitForElement(driver, ERROR_MESSAGE);
        inOrder.verify(webdriverHelper).waitForElement(driver, OTP_ICON);

        // credentials ok, forward request to nemid app
        inOrder.verify(webdriverHelper).clickButton(driver, NEMID_APP_BUTTON);

        // display supplemental information about nemid 2fa
        inOrder.verify(supplementalRequester).requestSupplementalInformation(credentials, true);

        // wait for user interaction with 3rd party nemid app
        inOrder.verify(webdriverHelper).waitForElement(driver, OTP_ICON);

        // collect token
        inOrder.verify(driver.switchTo()).defaultContent();
        inOrder.verify(webdriverHelper).waitForElement(driver, NEMID_TOKEN);
        assertThat(result).isEqualTo("--- SAMPLE TOKEN ---");
    }

    @Test
    public void doLoginWithShouldFailWhenIframeIsNotPresent() {
        // given
        given(webdriverHelper.waitForElement(driver, IFRAME)).willReturn(Optional.empty());

        // when
        Throwable throwable = Assertions.catchThrowable(() -> controller.doLoginWith(credentials));

        // then
        verify(sleeper, times(5)).sleepFor(5_000);
        // and
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Can't instantiate iframe element with NemId form.");
    }

    @SneakyThrows
    @Test
    public void doLoginWithShouldFailWhenUsernameInputIsNotPresentWithinIframe() {
        // given
        given(webdriverHelper.waitForElement(driver, USERNAME_INPUT)).willReturn(Optional.empty());

        // when
        Throwable throwable = Assertions.catchThrowable(() -> controller.doLoginWith(credentials));

        // then
        verify(authenticator, times(5)).getNemIdParameters();
        verify(sleeper, times(5)).sleepFor(5_000);
        // and
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Can't instantiate iframe element with NemId form.");
    }

    @Test
    public void doLoginWithShouldFailWhenOtpIconDoesNotAppearAfterEnteringCredentials() {
        // given
        given(webdriverHelper.waitForElement(driver, OTP_ICON)).willReturn(Optional.empty());

        // when
        Throwable throwable = Assertions.catchThrowable(() -> controller.doLoginWith(credentials));

        // then
        verify(sleeper, times(20)).sleepFor(1_000);
        // and
        assertThat(throwable)
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Can't validate NemId credentials.");
    }

    @Test
    public void doLoginWithShouldFailWhenNemIdReturnUnknownErrorForInvalidCredentials() {
        // given
        given(errorMessageMock.getText()).willReturn("--- WRONG CREDENTIALS UNKNOWN ERROR ---");

        // when
        Throwable throwable = Assertions.catchThrowable(() -> controller.doLoginWith(credentials));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Unknown login error '--- WRONG CREDENTIALS UNKNOWN ERROR ---'.");
    }

    @Test
    public void doLoginWithShouldFailWhenNemIdReturnKnownErrorForInvalidCredentials() {
        List<String> errorMessages =
                Arrays.asList(
                        "incorrect user ---",
                        "incorrect password ---",
                        "fejl i bruger ---",
                        "fejl i adgangskode ---",
                        "indtast bruger ---",
                        "indtast adgangskode ---");

        for (String errorMsg : errorMessages) {
            // given
            given(errorMessageMock.getText()).willReturn(errorMsg);

            // when
            Throwable throwable =
                    Assertions.catchThrowable(() -> controller.doLoginWith(credentials));

            // then
            assertThat(throwable).isInstanceOf(LoginException.class).hasMessage(errorMsg);
        }
    }

    @Test
    public void doLoginWithShouldFailWhenUserDoesNotAuthorizeNemIdRequestIn3rdPartyApp() {
        // given
        given(webdriverHelper.waitForElement(driver, OTP_ICON))
                .willReturn(Optional.of(otpIconMock));

        // when
        Throwable throwable = Assertions.catchThrowable(() -> controller.doLoginWith(credentials));

        // then
        verify(sleeper, times(120)).sleepFor(1_000);
        // and
        assertThat(throwable)
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("NemID request was not approved.");
    }

    @Test
    public void doLoginWithShouldFailWhenNemIdTokenIsNotFound() {
        // given
        given(webdriverHelper.waitForElement(driver, NEMID_TOKEN)).willReturn(Optional.empty());

        // when
        Throwable throwable = Assertions.catchThrowable(() -> controller.doLoginWith(credentials));

        // then
        verify(webdriverHelper, times(7)).waitForElement(driver, NEMID_TOKEN);
        // and
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Could not find nemId token.");
    }

    @Test
    public void doLoginWithShouldFailWhenNemIdTokenIsEmpty() {
        // given
        given(nemIdTokenMock.getText()).willReturn("");

        // when
        Throwable throwable = Assertions.catchThrowable(() -> controller.doLoginWith(credentials));

        // then
        verify(webdriverHelper, times(7)).waitForElement(driver, NEMID_TOKEN);
        // and
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Could not find nemId token.");
    }

    @SneakyThrows
    @Test
    public void doLoginWithShouldRetryFetchingNemIdParamsAndReturnsToken() {
        // given
        given(webdriverHelper.waitForElement(driver, USERNAME_INPUT))
                .willReturn(Optional.empty())
                .willReturn(Optional.of(userinputMock));

        // when
        String result = controller.doLoginWith(credentials);

        // then
        verify(authenticator, times(2)).getNemIdParameters();
        verify(sleeper, times(2)).sleepFor(5_000);
        assertThat(result).isEqualTo("--- SAMPLE TOKEN ---");
    }

    private String nemIdParamsToBase64Html() {
        String html = String.format(NemIdConstantsV2.BASE_HTML, NEMID_PARAMS);
        return Base64.getEncoder().encodeToString(html.getBytes());
    }
}
