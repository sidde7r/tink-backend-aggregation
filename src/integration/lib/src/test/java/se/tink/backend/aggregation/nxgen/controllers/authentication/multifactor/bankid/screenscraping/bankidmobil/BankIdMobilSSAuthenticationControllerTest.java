package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.bankidmobil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.bankidmobil.initializer.MobilInitializer;
import se.tink.libraries.selenium.WebDriverHelper;
import se.tink.libraries.selenium.exceptions.HtmlElementNotFoundException;

public class BankIdMobilSSAuthenticationControllerTest {
    private WebDriverHelper webDriverHelper;
    private WebDriver driver;
    private BankIdMobilSSAuthenticationController objUnderTest;
    private MobilInitializer mobilInitializer;
    private WebElement dummyElement;
    private InOrder inOrder;

    private static final By WAITING_FOR_AUTHENTICATION_FORM_XPATH =
            By.xpath("//form[@name='mobileForm' and not(descendant::input)]");

    @Before
    public void initSetup() {
        mobilInitializer = mock(MobilInitializer.class);
        driver = mock(PhantomJSDriver.class);
        webDriverHelper = mock(WebDriverHelper.class);
        objUnderTest =
                new BankIdMobilSSAuthenticationController(
                        mobilInitializer, driver, webDriverHelper);
        dummyElement = mock(WebElement.class);
        inOrder = Mockito.inOrder(webDriverHelper, driver, mobilInitializer);
    }

    @Test
    public void doLoginShouldThrowLoginExceptionWhenNotFoundWaitingForAuthenticationElement() {
        // given
        given(webDriverHelper.getElement(driver, WAITING_FOR_AUTHENTICATION_FORM_XPATH))
                .willThrow(new HtmlElementNotFoundException(""));

        // when
        Throwable throwable = catchThrowable(() -> objUnderTest.doLogin());

        // then
        assertThat(throwable)
                .isInstanceOf(LoginException.class)
                .hasMessage(
                        "User provided invalid credentials or bank Id by mobile is not activated");
    }

    @Test
    public void doLoginShouldThrowLoginExceptionWhenUserHasNotAcceptedBankId() {
        // given
        given(webDriverHelper.getElement(driver, WAITING_FOR_AUTHENTICATION_FORM_XPATH))
                .willReturn(dummyElement);
        given(driver.findElements(WAITING_FOR_AUTHENTICATION_FORM_XPATH))
                .willReturn(Arrays.asList(dummyElement));

        // when
        Throwable throwable = catchThrowable(() -> objUnderTest.doLogin());

        // then
        assertThat(throwable)
                .isInstanceOf(LoginException.class)
                .hasMessage("User did not accept bank id");
    }

    @Test
    public void doLoginWithSucceed() throws AuthenticationException {
        // given

        // when
        objUnderTest.doLogin();

        // then
        inOrder.verify(mobilInitializer).initializeBankIdMobilAuthentication();
        inOrder.verify(webDriverHelper).getElement(driver, WAITING_FOR_AUTHENTICATION_FORM_XPATH);
        inOrder.verify(driver).findElements(WAITING_FOR_AUTHENTICATION_FORM_XPATH);
    }
}
