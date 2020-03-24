package se.tink.backend.aggregation.agents.nxgen.no.banks.sdc.authenticator.initializers;

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
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.authenticator.bankidinitializers.PortalBankIframeInitializer;
import se.tink.libraries.selenium.WebDriverHelper;
import se.tink.libraries.selenium.exceptions.HtmlElementNotFoundException;
import se.tink.libraries.selenium.exceptions.ScreenScrapingException;

public class PortalBankIframeInitializerTest {
    private WebDriver driver;
    private WebDriverHelper webDriverHelper;
    private WebElement usernameInput;
    private PortalBankIframeInitializer objUnderTest;
    private InOrder inOrder;

    private static final By USERNAME_XPATH =
            By.xpath("//form[@id='loginForm']//input[@maxlength='11']");
    private static final By FORM_XPATH = By.xpath("//form[@id='loginForm']");
    private static final By AUTHENTICATION_LIST_BUTTON_XPATH =
            By.xpath("//button[@class='link' and span[contains(text(),'BankID')]]");
    private static final String DUMMY_USERNAME = "dummy_username";

    @Before
    public void initSetup() {
        driver = mock(PhantomJSDriver.class);
        webDriverHelper = mock(WebDriverHelper.class);
        usernameInput = mock(WebElement.class);
        objUnderTest = new PortalBankIframeInitializer(DUMMY_USERNAME, driver, webDriverHelper);
        inOrder = Mockito.inOrder(driver, webDriverHelper);
    }

    @Test
    public void authenticateShouldFinishWithoutError() {
        // given
        given(webDriverHelper.getElement(driver, USERNAME_XPATH)).willReturn(usernameInput);
        // when
        objUnderTest.initializeBankIdAuthentication();
        // then
        inOrder.verify(webDriverHelper).getElement(driver, USERNAME_XPATH);
        inOrder.verify(webDriverHelper).sendInputValue(usernameInput, DUMMY_USERNAME);
        inOrder.verify(webDriverHelper).submitForm(driver, FORM_XPATH);
        inOrder.verify(webDriverHelper).switchToIframe(driver);
        inOrder.verify(webDriverHelper).getElement(driver, AUTHENTICATION_LIST_BUTTON_XPATH);
    }

    @Test
    public void initializeBankIdAuthenticationShouldThrowExceptionWhenBankIdTemplateNotLoaded() {
        // given
        given(webDriverHelper.getElement(driver, USERNAME_XPATH)).willReturn(usernameInput);
        given(webDriverHelper.getElement(driver, AUTHENTICATION_LIST_BUTTON_XPATH))
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
