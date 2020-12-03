package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.bankidiframe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.bankidiframe.initializer.IframeInitializer;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.libraries.selenium.WebDriverHelper;
import se.tink.libraries.selenium.exceptions.HtmlElementNotFoundException;
import se.tink.libraries.selenium.exceptions.ScreenScrapingException;

public class BankIdIframeSSAuthenticationControllerTest {

    private BankIdIframeSSAuthenticationController controller;
    private IframeInitializer iframeInitializer;
    private WebDriverHelper webDriverHelper;
    private InOrder inOrder;
    private PhantomJSDriver driver;

    private WebElement buttonbankId;
    private WebElement selectAuthenticationButton;
    private WebElement passwordInputMock;
    private WebElement authenticationInputMock;
    private WebElement referenceWordsSpanMock;

    private static final By FORM_XPATH = By.xpath("//form");
    private static final By PASSWORD_INPUT_XPATH =
            By.xpath("//form//input[@type='password'][@maxlength]");
    private static final By AUTHENTICATION_LIST_BUTTON_XPATH =
            By.xpath("//button[@class='link' and span[contains(text(),'BankID')]]");
    private static final By BANK_ID_MOBIL_BUTTON =
            By.xpath(
                    "//ul/child::li/child::button[span[contains(text(),'mobil') and contains(text(),'BankID')]]");
    private static final By AUTHENTICATION_INPUT_XPATH =
            By.xpath("//form//input[@type='password'][@maxlength]");
    private static final By REFERENCE_WORDS_XPATH =
            By.xpath("//span[@data-bind='text: reference']");

    private static final By IFRAME_XPATH = By.tagName("iframe");
    private static final String PASSWORD_INPUT = "PASSWORD-INPUT";
    private static final String TEST_PASSWORD = "PASSWORD-EXAMPLE";
    private static final String REFERENCE_WORDS = "TEST TESTO";
    private static final String REFERENCE_WORDS_EXCEPTION_MESSAGE = "Couldn't find reference words";
    private static final String TEST_STRING = "Test translated string";

    @Before
    public void init() {
        driver = mock(PhantomJSDriver.class);
        webDriverHelper = mock(WebDriverHelper.class);
        iframeInitializer = mock(IframeInitializer.class);
        inOrder = Mockito.inOrder(iframeInitializer, driver, webDriverHelper);
        Catalog catalog = mock(Catalog.class);
        given(catalog.getString(any(LocalizableKey.class))).willReturn(TEST_STRING);
        controller =
                new BankIdIframeSSAuthenticationController(
                        webDriverHelper,
                        driver,
                        iframeInitializer,
                        new Credentials(),
                        mock(SupplementalRequester.class),
                        catalog);
        initializeWebElements();
    }

    private void initializeWebElements() {
        // authentication list button
        selectAuthenticationButton = mock(WebElement.class);
        given(webDriverHelper.waitForElement(driver, AUTHENTICATION_LIST_BUTTON_XPATH))
                .willReturn(Optional.ofNullable(selectAuthenticationButton));

        // bank id mobil
        buttonbankId = mock(WebElement.class);
        given(webDriverHelper.getElement(driver, BANK_ID_MOBIL_BUTTON)).willReturn(buttonbankId);

        // iframe mock
        WebElement iframeMock = mock(WebElement.class);
        given(webDriverHelper.getElement(driver, IFRAME_XPATH)).willReturn(iframeMock);

        // password input
        passwordInputMock = mock(WebElement.class);
        given(webDriverHelper.checkIfElementEnabledIfNotWait(passwordInputMock)).willReturn(true);
        given(driver.findElements(PASSWORD_INPUT_XPATH))
                .willReturn(Collections.singletonList(passwordInputMock));

        // authentication input
        authenticationInputMock = mock(WebElement.class);
        given(webDriverHelper.waitForElement(driver, AUTHENTICATION_INPUT_XPATH))
                .willReturn(Optional.of(authenticationInputMock));

        // reference words
        referenceWordsSpanMock = mock(WebElement.class);
        given(webDriverHelper.waitForElement(driver, REFERENCE_WORDS_XPATH))
                .willReturn(Optional.of(referenceWordsSpanMock));
        given(referenceWordsSpanMock.getText()).willReturn(REFERENCE_WORDS);
    }

    @Test
    public void authenticateShouldFinishWithoutError() throws AuthenticationException {
        // given
        given(authenticationInputMock.isEnabled()).willReturn(true);
        // when
        controller.doLogin(PASSWORD_INPUT);
        // then

        // initialize bank id iframe
        inOrder.verify(iframeInitializer).initializeBankIdAuthentication();

        // List authentication methods
        inOrder.verify(webDriverHelper).waitForElement(driver, AUTHENTICATION_LIST_BUTTON_XPATH);
        inOrder.verify(webDriverHelper).clickButton(selectAuthenticationButton);

        // Choose Bank Id Mobil
        inOrder.verify(webDriverHelper).getElement(driver, BANK_ID_MOBIL_BUTTON);
        inOrder.verify(webDriverHelper).clickButton(buttonbankId);
        inOrder.verify(webDriverHelper).submitForm(driver, FORM_XPATH);

        // wait for user accepting bank id and submit bank Id password
        inOrder.verify(webDriverHelper).switchToIframe(driver);
        inOrder.verify(driver).findElements(PASSWORD_INPUT_XPATH);
        inOrder.verify(webDriverHelper).checkIfElementEnabledIfNotWait(passwordInputMock);
    }

    @Test
    public void doLoginShouldThrowExceptionWhenNoPasswordInputAvailable() {
        // given

        given(webDriverHelper.checkIfElementEnabledIfNotWait(passwordInputMock)).willReturn(false);
        // when
        Throwable throwable = Assertions.catchThrowable(() -> controller.doLogin(TEST_PASSWORD));
        // then
        assertThat(throwable)
                .isInstanceOf(LoginException.class)
                .hasMessage("Password can't be reached, probably user did not accept bank id");
    }

    @Test
    public void doLoginShouldThrowLoginExceptionWhenNotAuthenticationInputFound() {
        // given
        given(webDriverHelper.waitForElement(driver, AUTHENTICATION_INPUT_XPATH))
                .willReturn(Optional.empty());
        given(webDriverHelper.getElement(driver, BANK_ID_MOBIL_BUTTON))
                .willThrow(HtmlElementNotFoundException.class);

        // when
        Throwable throwable = Assertions.catchThrowable(() -> controller.doLogin(TEST_PASSWORD));

        // then
        assertThat(throwable).isInstanceOf(LoginException.class);
    }

    @Test
    public void doLoginShouldNotCallAuthenticationsListMethodIfDefaultBankIdMobilIsChosen()
            throws AuthenticationException {
        // given
        given(authenticationInputMock.isEnabled()).willReturn(false);

        // when
        controller.doLogin(TEST_PASSWORD);

        // then
        verify(webDriverHelper, never()).getElement(driver, AUTHENTICATION_LIST_BUTTON_XPATH);
    }

    @Test
    public void doLoginShouldCallCheckAuthenticationsListMeyhodIfDefaultBankIdMobilIsNotChosen()
            throws AuthenticationException {
        // given
        given(authenticationInputMock.isEnabled()).willReturn(true);

        // when
        controller.doLogin(TEST_PASSWORD);

        // then
        verify(webDriverHelper, times(1)).waitForElement(driver, AUTHENTICATION_LIST_BUTTON_XPATH);
    }

    @Test
    public void doLoginShouldThrowExceptionWhenReferenceWordsAreNotFound() {
        // given
        given(webDriverHelper.waitForElement(driver, REFERENCE_WORDS_XPATH))
                .willReturn(Optional.empty());

        // when
        Throwable throwable = Assertions.catchThrowable(() -> controller.doLogin(TEST_PASSWORD));

        // then
        assertThat(throwable)
                .isInstanceOf(ScreenScrapingException.class)
                .hasMessage(REFERENCE_WORDS_EXCEPTION_MESSAGE);
    }

    @Test
    public void doLoginShouldThrowExceptionWhenReferenceWordsTextIsNull() {
        // given
        given(referenceWordsSpanMock.getText()).willReturn(null);

        // when
        Throwable throwable = Assertions.catchThrowable(() -> controller.doLogin(TEST_PASSWORD));

        // then
        assertThat(throwable)
                .isInstanceOf(ScreenScrapingException.class)
                .hasMessage(REFERENCE_WORDS_EXCEPTION_MESSAGE);
    }
}
