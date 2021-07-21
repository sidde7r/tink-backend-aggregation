package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.authenticator.bankidinitializers;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.authenticator.bankidinitializers.EikaBankIdMobilInitializer.BANK_ID_MOBIL;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.authenticator.bankidinitializers.EikaBankIdMobilInitializer.DATE_OF_BIRTH_INPUT_XPATH;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.authenticator.bankidinitializers.EikaBankIdMobilInitializer.FORM_XPATH;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.authenticator.bankidinitializers.EikaBankIdMobilInitializer.MOBILENUMBER_INPUT_XPATH;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import se.tink.integration.webdriver.WebDriverHelper;

public class EikaBankIdMobilInitializerTest {
    private WebDriver driver;
    private WebDriverHelper webDriverHelper;
    private EikaBankIdMobilInitializer objUnderTest;
    private InOrder inOrder;

    private static final String DUMMY_MOBILE_NUMBER = "DUMMY_MOBILE_NUMBER";
    private static final String DUMMY_MOBILE_NATIONAL_ID_NUMBER = "DUMMY_MOBILE_NATIONAL_ID_NUMBER";

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
