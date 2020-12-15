package se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebDriver.Timeouts;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.exception.NemIdException;
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

    private InOrder inOrder;

    // web page elements
    private WebElement iframeMock;
    private WebElement userinputMock;
    private WebElement errorMessageMock;
    private WebElement nemIdCodeCardMock;
    private WebElement otpIconMock;
    private WebElement nemIdTokenMock;

    private Credentials credentials;

    @SneakyThrows
    @Before
    public void setUp() {
        sleeper = mock(Sleeper.class);

        driver = mock(PhantomJSDriver.class, Answers.RETURNS_DEEP_STUBS);

        TargetLocator targetLocator = mock(TargetLocator.class);
        when(targetLocator.frame(any(WebElement.class))).thenReturn(driver);
        Timeouts timeouts = mock(Timeouts.class);

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
                        Catalog.getCatalog("en"),
                        mock(StatusUpdater.class));

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

        // nemid code card info
        nemIdCodeCardMock = mock(WebElement.class);
        given(webdriverHelper.waitForElement(driver, NEMID_CODE_CARD))
                .willReturn(Optional.of(nemIdCodeCardMock));

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
                .hasMessage("[NemId] Can't instantiate iframe element with NemId form.");
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
                .hasMessage("[NemId] Can't instantiate iframe element with NemId form.");
    }

    @Test
    public void
            doLoginWithShouldFailWhenOtpIconAndNemIdCodeCardAndNemIdCodeTokenDoesNotAppearAfterEnteringCredentials() {
        // given
        given(webdriverHelper.waitForElement(driver, OTP_ICON)).willReturn(Optional.empty());
        given(webdriverHelper.waitForElement(driver, NEMID_CODE_CARD)).willReturn(Optional.empty());
        given(webdriverHelper.waitForElement(driver, NEMID_CODE_TOKEN))
                .willReturn(Optional.empty());

        // when
        Throwable throwable = Assertions.catchThrowable(() -> controller.doLoginWith(credentials));

        // then
        verify(sleeper, times(120)).sleepFor(1_000);
        // and
        assertThat(throwable)
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("[NemId] Can't validate NemId credentials.");
    }

    @Test
    public void
            doLoginWithShouldFailWhenOtpIconDoesNotAppearAndNemIdCodeCardAppearAfterEnteringCredentials() {
        // given
        given(webdriverHelper.waitForElement(driver, OTP_ICON)).willReturn(Optional.empty());
        given(webdriverHelper.waitForElement(driver, NEMID_CODE_TOKEN))
                .willReturn(Optional.empty());
        given(webdriverHelper.waitForElement(driver, NEMID_CODE_CARD))
                .willReturn(Optional.of(nemIdCodeCardMock));

        // when
        Throwable throwable = Assertions.catchThrowable(() -> controller.doLoginWith(credentials));

        // then
        verify(sleeper, times(120)).sleepFor(1_000);
        // and
        assertThat(throwable)
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("[NemId] User has code card.");
    }

    @Test
    public void
            doLoginWithShouldFailWhenOtpIconDoesNotAppearAndNemIdCodeTokenAppearAfterEnteringCredentials() {
        // given
        given(webdriverHelper.waitForElement(driver, OTP_ICON)).willReturn(Optional.empty());
        given(webdriverHelper.waitForElement(driver, NEMID_CODE_CARD)).willReturn(Optional.empty());
        given(webdriverHelper.waitForElement(driver, NEMID_CODE_TOKEN))
                .willReturn(Optional.of(nemIdCodeCardMock));

        // when
        Throwable throwable = Assertions.catchThrowable(() -> controller.doLoginWith(credentials));

        // then
        verify(sleeper, times(120)).sleepFor(1_000);
        // and
        assertThat(throwable)
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("[NemId] User has code token.");
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
                .hasMessage("[NemId] Unknown login error: --- WRONG CREDENTIALS UNKNOWN ERROR ---");
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
            assertThat(throwable)
                    .isInstanceOf(LoginException.class)
                    .hasMessage("[NemId]" + errorMsg);
        }
    }

    @Test
    public void doLoginWithShouldFailWhenNemIdReturnKnownErrorForWhenUserNeedsASpecialPassword() {
        // given
        String errMessage = "Enter activation password.";
        given(errorMessageMock.getText()).willReturn(errMessage);

        // when
        Throwable throwable = Assertions.catchThrowable(() -> controller.doLoginWith(credentials));

        // then
        assertThat(throwable).isInstanceOf(LoginException.class);
        assertThat(((LoginException) throwable).getUserMessage().get()).isEqualTo(errMessage);
    }

    @Test
    public void doLoginWithShouldFailWhenUserDoesNotAuthorizeNemIdRequestIn3rdPartyApp() {
        // given
        given(webdriverHelper.waitForElement(driver, OTP_ICON))
                .willReturn(Optional.of(otpIconMock));
        given(webdriverHelper.waitForElement(driver, NEMID_CODE_CARD)).willReturn(Optional.empty());
        given(webdriverHelper.waitForElement(driver, NEMID_TOKEN)).willReturn(Optional.empty());

        // when
        Throwable throwable = Assertions.catchThrowable(() -> controller.doLoginWith(credentials));

        // then
        verify(sleeper, times(120)).sleepFor(1_000);
        // and
        assertThat(throwable)
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("[NemId] NemID request was not approved.");
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
                .isInstanceOf(NemIdException.class)
                .hasMessage("Cause: NemIdError.TIMEOUT");
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
                .isInstanceOf(NemIdException.class)
                .hasMessage("Cause: NemIdError.TIMEOUT");
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
