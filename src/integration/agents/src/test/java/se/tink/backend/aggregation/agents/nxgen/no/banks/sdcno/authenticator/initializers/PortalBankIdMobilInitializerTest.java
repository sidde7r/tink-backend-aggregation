package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.authenticator.initializers;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.authenticator.bankidinitializers.PortalBankIdMobilInitializer;
import se.tink.integration.webdriver.WebDriverHelper;

public class PortalBankIdMobilInitializerTest {
    private WebDriver driver;
    private WebDriverHelper webDriverHelper;
    private PortalBankIdMobilInitializer objUnderTest;
    private InOrder inOrder;

    private static final String DUMMY_MOBILE_NUMBER = "DUMMY_MOBILE_NUMBER";
    private static final String DUMMY_MOBILE_NATIONAL_ID_NUMBER = "DUMMY_MOBILE_NATIONAL_ID_NUMBER";

    private static final By MOBILENUMBER_INPUT_XPATH = By.xpath("//input[@id='mobileNumber']");
    private static final By NATIONAL_ID_NUMBER_INPUT_XPATH =
            By.xpath("//input[@id='mobileCustomerId']");
    private static final By FORM_XPATH = By.xpath("//form[@id='mobileLoginForm']");

    @Before
    public void initSetup() {
        driver = mock(WebDriver.class);
        webDriverHelper = mock(WebDriverHelper.class);
        objUnderTest =
                new PortalBankIdMobilInitializer(
                        DUMMY_MOBILE_NUMBER,
                        DUMMY_MOBILE_NATIONAL_ID_NUMBER,
                        driver,
                        webDriverHelper);
        inOrder = Mockito.inOrder(driver, webDriverHelper);
    }

    @Test
    public void initializeBankIdMobilAuthenticationWithSucceed() {
        // given
        WebElement dummyElement = mock(WebElement.class);
        given(webDriverHelper.getElement(driver, MOBILENUMBER_INPUT_XPATH))
                .willReturn(dummyElement);
        given(webDriverHelper.getElement(driver, NATIONAL_ID_NUMBER_INPUT_XPATH))
                .willReturn(dummyElement);
        given(webDriverHelper.getElement(driver, FORM_XPATH)).willReturn(dummyElement);

        // when
        objUnderTest.initializeBankIdMobilAuthentication();

        // then
        inOrder.verify(webDriverHelper).getElement(driver, MOBILENUMBER_INPUT_XPATH);
        inOrder.verify(webDriverHelper).sendInputValue(dummyElement, DUMMY_MOBILE_NUMBER);
        inOrder.verify(webDriverHelper).getElement(driver, NATIONAL_ID_NUMBER_INPUT_XPATH);
        inOrder.verify(webDriverHelper)
                .sendInputValue(dummyElement, DUMMY_MOBILE_NATIONAL_ID_NUMBER);
        inOrder.verify(webDriverHelper).submitForm(driver, FORM_XPATH);
    }
}
