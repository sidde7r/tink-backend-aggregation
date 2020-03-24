package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.initializer.IframeInitializer;
import se.tink.libraries.selenium.WebDriverHelper;

public class BankIdIframeSSAuthenticationControllerTest {
    private BankIdIframeSSAuthenticationController controller;
    private IframeInitializer iframeInitializer;
    private WebDriverHelper webDriverHelper;
    private InOrder inOrder;
    private PhantomJSDriver driver;

    private WebElement buttonbankId;
    private WebElement selectAuthenticationButton;
    private WebElement passwordInputMock;
    private WebElement iframeMock;

    private static final By FORM_XPATH = By.xpath("//form");
    private static final By PASSWORD_INPUT_XPATH =
            By.xpath("//form//input[@type='password'][@maxlength]");
    private static final By AUTHENTICATION_LIST_BUTTON_XPATH =
            By.xpath("//button[@class='link' and span[contains(text(),'BankID')]]");
    private static final By BANK_ID_MOBIL_BUTTON =
            By.xpath(
                    "//ul/child::li/child::button[span[contains(text(),'mobil') and contains(text(),'BankID')]]");

    private static final By IFRAME_XPATH = By.tagName("iframe");
    private static String PASSWORD_INPUT = "PASSWORD-INPUT";

    @Before
    public void init() {
        driver = mock(PhantomJSDriver.class);
        webDriverHelper = mock(WebDriverHelper.class);
        iframeInitializer = mock(IframeInitializer.class);
        inOrder = Mockito.inOrder(iframeInitializer, driver, webDriverHelper);
        controller =
                new BankIdIframeSSAuthenticationController(
                        iframeInitializer, driver, webDriverHelper);
        initializeWebElements();
    }

    private void initializeWebElements() {
        // authentication list button
        selectAuthenticationButton = mock(WebElement.class);
        given(webDriverHelper.getElement(driver, AUTHENTICATION_LIST_BUTTON_XPATH))
                .willReturn(selectAuthenticationButton);

        // bank id mobil
        buttonbankId = mock(WebElement.class);
        given(webDriverHelper.getElement(driver, BANK_ID_MOBIL_BUTTON)).willReturn(buttonbankId);

        // iframe mock
        iframeMock = mock(WebElement.class);
        given(webDriverHelper.getElement(driver, IFRAME_XPATH)).willReturn(iframeMock);

        // password input
        passwordInputMock = mock(WebElement.class);
        given(webDriverHelper.checkIfElementEnabledIfNotWait(passwordInputMock)).willReturn(true);
        given(driver.findElements(PASSWORD_INPUT_XPATH))
                .willReturn(Arrays.asList(passwordInputMock));
    }

    @Test
    public void authenticateShouldFinishWithoutError() throws AuthenticationException {
        // given
        // when
        controller.doLogin(PASSWORD_INPUT);
        // then

        // initialize bank id iframe
        inOrder.verify(iframeInitializer).initializeBankIdAuthentication();

        // List authentication methods
        inOrder.verify(webDriverHelper).getElement(driver, AUTHENTICATION_LIST_BUTTON_XPATH);
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
        Throwable throwable =
                Assertions.catchThrowable(() -> controller.doLogin("PASSWORD-EXAMPLE"));
        // then
        assertThat(throwable)
                .isInstanceOf(LoginException.class)
                .hasMessage("Password can't be reached, probably user did not accept bank id");
    }
}
