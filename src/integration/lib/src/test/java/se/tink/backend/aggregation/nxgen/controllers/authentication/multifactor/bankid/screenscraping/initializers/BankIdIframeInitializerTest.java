package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.initializers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.initializer.BankIdIframeInitializer;
import se.tink.libraries.selenium.WebDriverHelper;
import se.tink.libraries.selenium.exceptions.HtmlElementNotFoundException;
import se.tink.libraries.selenium.exceptions.ScreenScrapingException;

public class BankIdIframeInitializerTest {
    private WebDriver driver;
    private WebDriverHelper webDriverHelper;
    private WebElement usernameInput;
    private BankIdIframeInitializer objUnderTest;
    private InOrder inOrder;

    private static final By USERNAME_XPATH = By.xpath("//form//input[@maxlength='11']");
    private static final By FORM_XPATH = By.xpath("//form");
    private static final String DUMMY_USERNAME = "dummy_username";

    @Before
    public void initSetup() {
        driver = mock(PhantomJSDriver.class);
        webDriverHelper = mock(WebDriverHelper.class);
        usernameInput = mock(WebElement.class);
        objUnderTest = new BankIdIframeInitializer(DUMMY_USERNAME, driver, webDriverHelper);
        inOrder = Mockito.inOrder(driver, webDriverHelper);
    }

    @Test
    public void authenticateShouldFinishWithoutError() {
        // given
        given(webDriverHelper.getElement(driver, USERNAME_XPATH)).willReturn(usernameInput);
        // when
        objUnderTest.initializeBankIdAuthentication();
        // then
        inOrder.verify(webDriverHelper).switchToIframe(driver);
        inOrder.verify(webDriverHelper).getElement(driver, USERNAME_XPATH);
        inOrder.verify(webDriverHelper).sendInputValue(usernameInput, DUMMY_USERNAME);
        inOrder.verify(webDriverHelper).submitForm(driver, FORM_XPATH);
    }

    @Test
    public void initializeBankIdAuthenticationShouldThrowExceptionWhenBankIdTemplateNotLoaded() {
        // given
        given(webDriverHelper.getElement(driver, USERNAME_XPATH))
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
