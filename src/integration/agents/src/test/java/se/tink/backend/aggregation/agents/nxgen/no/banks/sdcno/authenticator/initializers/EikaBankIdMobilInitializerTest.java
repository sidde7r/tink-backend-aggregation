package se.tink.backend.aggregation.agents.nxgen.no.banks.sdc.authenticator.initializers;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.authenticator.bankidinitializers.EikaBankIdMobilInitializer;
import se.tink.integration.webdriver.WebDriverHelper;

public class EikaBankIdMobilInitializerTest {
    private WebDriver driver;
    private WebDriverHelper webDriverHelper;
    private EikaBankIdMobilInitializer objUnderTest;
    private InOrder inOrder;

    private static final String DUMMY_MOBILE_NUMBER = "DUMMY_MOBILE_NUMBER";
    private static final String DUMMY_MOBILE_NATIONAL_ID_NUMBER = "DUMMY_MOBILE_NATIONAL_ID_NUMBER";

    private static final By MOBILENUMBER_INPUT_XPATH = By.xpath("//input[@id='txtMobilnummer']");
    private static final By DATE_OF_BIRTH_INPUT_XPATH = By.xpath("//input[@id='txtFodselsdato']");
    private static final By BANK_ID_MOBIL = By.xpath("//a[@id='tab-mobil']");
    private static final By FORM_XPATH = By.xpath("//form[@id='mobileLoginForm']");

    @Before
    public void initSetup() {
        driver = mock(WebDriver.class);
        webDriverHelper = mock(WebDriverHelper.class);
        objUnderTest =
                new EikaBankIdMobilInitializer(
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
        given(webDriverHelper.getElement(driver, BANK_ID_MOBIL)).willReturn(dummyElement);
        given(webDriverHelper.getElement(driver, MOBILENUMBER_INPUT_XPATH))
                .willReturn(dummyElement);
        given(webDriverHelper.getElement(driver, DATE_OF_BIRTH_INPUT_XPATH))
                .willReturn(dummyElement);
        given(webDriverHelper.getElement(driver, FORM_XPATH)).willReturn(dummyElement);

        // when
        objUnderTest.initializeBankIdMobilAuthentication();

        // then
        inOrder.verify(webDriverHelper).getElement(driver, BANK_ID_MOBIL);
        inOrder.verify(webDriverHelper).clickButton(dummyElement);
        inOrder.verify(webDriverHelper).getElement(driver, MOBILENUMBER_INPUT_XPATH);
        inOrder.verify(webDriverHelper).sendInputValue(dummyElement, DUMMY_MOBILE_NUMBER);
        inOrder.verify(webDriverHelper).getElement(driver, DATE_OF_BIRTH_INPUT_XPATH);
        inOrder.verify(webDriverHelper)
                .sendInputValue(dummyElement, DUMMY_MOBILE_NATIONAL_ID_NUMBER);
        inOrder.verify(webDriverHelper).submitForm(driver, FORM_XPATH);
    }
}
