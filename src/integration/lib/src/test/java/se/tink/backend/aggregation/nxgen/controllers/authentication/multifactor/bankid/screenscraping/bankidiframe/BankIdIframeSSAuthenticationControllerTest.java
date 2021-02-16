package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.bankidiframe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.WebScrapingConstants.Xpath;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.bankidiframe.initializer.IframeInitializer;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
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
    private WebDriver driver;

    private WebElement buttonBankId;
    private WebElement selectAuthenticationButton;
    private WebElement passwordInputMock;
    private WebElement referenceWordsSpanMock;

    private static final By IFRAME_XPATH = By.tagName("iframe");
    private static final String PASSWORD_INPUT = "PASSWORD-INPUT";
    private static final String TEST_PASSWORD = "PASSWORD-EXAMPLE";
    private static final String REFERENCE_WORDS = "TEST TESTO";
    private static final String REFERENCE_WORDS_EXCEPTION_MESSAGE = "Couldn't find reference words";
    private static final String TEST_STRING = "Test translated string";

    @Before
    public void init() {
        driver = mock(WebDriver.class);
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
                        mock(SupplementalInformationController.class),
                        catalog);
        initializeWebElements();
    }

    private void initializeWebElements() {
        // bank id mobil input
        given(webDriverHelper.waitForElement(driver, Xpath.MOBILE_BANK_ID_INPUT_XPATH))
                .willReturn(Optional.of(mock(WebElement.class)));

        // bank id app title or input
        given(
                        webDriverHelper.waitForOneOfElements(
                                driver,
                                Xpath.BANK_ID_APP_TITLE_XPATH,
                                Xpath.BANK_ID_PASSWORD_INPUT_XPATH))
                .willReturn(Optional.of(mock(WebElement.class)));

        // authentication list button
        selectAuthenticationButton = mock(WebElement.class);
        given(webDriverHelper.waitForElement(driver, Xpath.AUTHENTICATION_LIST_BUTTON_XPATH))
                .willReturn(Optional.ofNullable(selectAuthenticationButton));

        // bank id mobil button
        buttonBankId = mock(WebElement.class);
        given(webDriverHelper.getElement(driver, Xpath.BANK_ID_MOBIL_BUTTON))
                .willReturn(buttonBankId);

        // iframe mock
        WebElement iframeMock = mock(WebElement.class);
        given(webDriverHelper.getElement(driver, IFRAME_XPATH)).willReturn(iframeMock);

        // password input
        passwordInputMock = mock(WebElement.class);
        given(webDriverHelper.checkIfElementEnabledIfNotWait(passwordInputMock)).willReturn(true);
        given(driver.findElements(Xpath.PASSWORD_INPUT_XPATH))
                .willReturn(Collections.singletonList(passwordInputMock));

        // reference words
        referenceWordsSpanMock = mock(WebElement.class);
        given(webDriverHelper.waitForElement(driver, Xpath.REFERENCE_WORDS_XPATH))
                .willReturn(Optional.of(referenceWordsSpanMock));
        given(referenceWordsSpanMock.getText()).willReturn(REFERENCE_WORDS);
    }

    @Test
    public void shouldAuthenticateWithoutErrorUsingMobileBankId() throws AuthenticationException {
        // given & when
        controller.doLogin(PASSWORD_INPUT);

        // then

        // initialize bank id iframe
        inOrder.verify(iframeInitializer).initializeBankIdAuthentication();

        // check if it is mobile bank id
        inOrder.verify(webDriverHelper).waitForElement(driver, Xpath.MOBILE_BANK_ID_INPUT_XPATH);

        // submit form
        inOrder.verify(webDriverHelper).submitForm(driver, Xpath.FORM_XPATH);

        // display reference words
        inOrder.verify(webDriverHelper).waitForElement(driver, Xpath.REFERENCE_WORDS_XPATH);

        // wait for user accepting bank id and submit bank Id password
        verifyWaitForUserInteractionAndSendBankIdPasswordInOrder();

        inOrder.verifyNoMoreInteractions();
        verify(webDriverHelper, never())
                .waitForElement(driver, Xpath.AUTHENTICATION_LIST_BUTTON_XPATH);
    }

    private void verifyWaitForUserInteractionAndSendBankIdPasswordInOrder() {
        inOrder.verify(webDriverHelper).switchToIframe(driver);
        inOrder.verify(driver).findElements(Xpath.PASSWORD_INPUT_XPATH);
        inOrder.verify(webDriverHelper).checkIfElementEnabledIfNotWait(passwordInputMock);
        inOrder.verify(webDriverHelper).sendInputValue(passwordInputMock, PASSWORD_INPUT);
        inOrder.verify(webDriverHelper).submitForm(driver, Xpath.FORM_XPATH);
    }

    @Test
    public void shouldAuthenticateWithoutErrorUsingBankIdApp() throws AuthenticationException {
        // given
        given(webDriverHelper.waitForElement(driver, Xpath.MOBILE_BANK_ID_INPUT_XPATH))
                .willReturn(Optional.empty());

        // when
        controller.doLogin(PASSWORD_INPUT);

        // then

        // initialize bank id iframe
        inOrder.verify(iframeInitializer).initializeBankIdAuthentication();

        // check if it is mobile bank id
        inOrder.verify(webDriverHelper).waitForElement(driver, Xpath.MOBILE_BANK_ID_INPUT_XPATH);

        // check if it is bank id app
        inOrder.verify(webDriverHelper)
                .waitForOneOfElements(
                        driver, Xpath.BANK_ID_APP_TITLE_XPATH, Xpath.BANK_ID_PASSWORD_INPUT_XPATH);

        // wait for user accepting bank id and submit bank Id password
        verifyWaitForUserInteractionAndSendBankIdPasswordInOrder();

        inOrder.verifyNoMoreInteractions();
        verify(webDriverHelper, never())
                .waitForElement(driver, Xpath.AUTHENTICATION_LIST_BUTTON_XPATH);
    }

    @Test
    public void shouldChangeAuthenticationToMobileBankIdAndAuthenticateWithoutErrors()
            throws AuthenticationException {
        // given
        makeMobileBankIbAndBankIdAppReturnOptionalEmpty();

        // when
        controller.doLogin(PASSWORD_INPUT);

        // then

        // initialize bank id iframe
        inOrder.verify(iframeInitializer).initializeBankIdAuthentication();

        // check if it is mobile bank id
        inOrder.verify(webDriverHelper).waitForElement(driver, Xpath.MOBILE_BANK_ID_INPUT_XPATH);

        // check if it is bank id app
        inOrder.verify(webDriverHelper)
                .waitForOneOfElements(
                        driver, Xpath.BANK_ID_APP_TITLE_XPATH, Xpath.BANK_ID_PASSWORD_INPUT_XPATH);

        // get list of authentication methods and choose mobile bank id
        verify(webDriverHelper).waitForElement(driver, Xpath.AUTHENTICATION_LIST_BUTTON_XPATH);
        verify(webDriverHelper).clickButton(selectAuthenticationButton);

        verify(webDriverHelper).getElement(driver, Xpath.BANK_ID_MOBIL_BUTTON);
        verify(webDriverHelper).clickButton(buttonBankId);

        // submit form
        inOrder.verify(webDriverHelper).submitForm(driver, Xpath.FORM_XPATH);

        // display reference words
        inOrder.verify(webDriverHelper).waitForElement(driver, Xpath.REFERENCE_WORDS_XPATH);

        // wait for user accepting bank id and submit bank Id password
        verifyWaitForUserInteractionAndSendBankIdPasswordInOrder();

        inOrder.verifyNoMoreInteractions();
    }

    private void makeMobileBankIbAndBankIdAppReturnOptionalEmpty() {
        given(webDriverHelper.waitForElement(driver, Xpath.MOBILE_BANK_ID_INPUT_XPATH))
                .willReturn(Optional.empty());

        given(
                        webDriverHelper.waitForOneOfElements(
                                driver,
                                Xpath.BANK_ID_APP_TITLE_XPATH,
                                Xpath.BANK_ID_PASSWORD_INPUT_XPATH))
                .willReturn(Optional.empty());
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
    public void
            doLoginShouldThrowNoAvailableScaMethodsExceptionWhenAuthenticationListButtonIsNotFound() {
        // given
        makeMobileBankIbAndBankIdAppReturnOptionalEmpty();

        given(webDriverHelper.waitForElement(driver, Xpath.AUTHENTICATION_LIST_BUTTON_XPATH))
                .willReturn(Optional.empty());

        // when
        Throwable throwable = Assertions.catchThrowable(() -> controller.doLogin(TEST_PASSWORD));

        // then
        assertThat(throwable)
                .isInstanceOf(LoginException.class)
                .hasMessage(LoginError.NO_AVAILABLE_SCA_METHODS.exception().getMessage());
    }

    @Test
    public void doLoginShouldThrowNotSupportedLoginExceptionWhenNoAuthenticationInputFound() {
        // given
        makeMobileBankIbAndBankIdAppReturnOptionalEmpty();

        given(webDriverHelper.getElement(driver, Xpath.BANK_ID_MOBIL_BUTTON))
                .willThrow(HtmlElementNotFoundException.class);

        // when
        Throwable throwable = Assertions.catchThrowable(() -> controller.doLogin(TEST_PASSWORD));

        // then
        assertThat(throwable)
                .isInstanceOf(LoginException.class)
                .hasMessage(LoginError.NOT_SUPPORTED.exception().getMessage());
    }

    @Test
    public void doLoginShouldThrowExceptionWhenReferenceWordsAreNotFound() {
        // given
        given(webDriverHelper.waitForElement(driver, Xpath.REFERENCE_WORDS_XPATH))
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
