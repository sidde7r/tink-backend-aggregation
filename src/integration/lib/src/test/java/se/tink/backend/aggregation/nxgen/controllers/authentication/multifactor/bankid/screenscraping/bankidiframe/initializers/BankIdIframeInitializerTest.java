package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.bankidiframe.initializers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.WebScrapingConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.bankidiframe.initializer.BankIdIframeInitializer;
import se.tink.integration.webdriver.WebDriverHelper;
import se.tink.integration.webdriver.exceptions.HtmlElementNotFoundException;
import se.tink.integration.webdriver.exceptions.ScreenScrapingException;

public class BankIdIframeInitializerTest {
    private WebDriver driver;
    private WebDriverHelper webDriverHelper;
    private WebElement usernameInput;
    private BankIdIframeInitializer objUnderTest;
    private InOrder inOrder;

    private static final String DUMMY_USERNAME = "dummy_username";

    @Before
    public void initSetup() {
        driver = mock(WebDriver.class);
        webDriverHelper = mock(WebDriverHelper.class);
        usernameInput = mock(WebElement.class);
        objUnderTest = new BankIdIframeInitializer(DUMMY_USERNAME, driver, webDriverHelper);
        inOrder = Mockito.inOrder(driver, webDriverHelper);
    }

    @Test
    public void authenticateShouldFinishWithoutError() {
        // given
        given(webDriverHelper.getElement(driver, WebScrapingConstants.Xpath.USERNAME_XPATH))
                .willReturn(usernameInput);
        // when
        objUnderTest.initializeBankIdAuthentication();
        // then
        inOrder.verify(webDriverHelper).switchToIframe(driver);
        inOrder.verify(webDriverHelper)
                .getElement(driver, WebScrapingConstants.Xpath.USERNAME_XPATH);
        inOrder.verify(webDriverHelper).sendInputValue(usernameInput, DUMMY_USERNAME);
        inOrder.verify(webDriverHelper).submitForm(driver, WebScrapingConstants.Xpath.FORM_XPATH);
    }

    @Test
    public void initializeBankIdAuthenticationShouldThrowExceptionWhenBankIdTemplateNotLoaded() {
        // given
        given(webDriverHelper.getElement(driver, WebScrapingConstants.Xpath.USERNAME_XPATH))
                .willThrow(HtmlElementNotFoundException.class);
        // when
        Throwable throwable =
                Assertions.catchThrowable(() -> objUnderTest.initializeBankIdAuthentication());
        // then
        assertThat(throwable)
                .isInstanceOf(ScreenScrapingException.class)
                .hasMessage("Bank Id template not loaded");
    }
}
