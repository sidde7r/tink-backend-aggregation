package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.steps;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlLocators.LOC_PRIVATE_PASSWORD_INPUT;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlLocators.LOC_SUBMIT_BUTTON;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.BankIdWebDriver;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.ElementLocator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.ElementsSearchQuery;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.ElementsSearchResult;

public class BankIdEnterPasswordStepTest {

    private static final String SAMPLE_USER_BANK_ID_PASSWORD = "some password";

    /*
    Mocks
     */
    private BankIdWebDriver webDriver;

    /*
    Real
     */
    private BankIdEnterPasswordStep enterPasswordStep;

    @Before
    public void setup() {
        webDriver = mock(BankIdWebDriver.class);

        enterPasswordStep = new BankIdEnterPasswordStep(webDriver);
    }

    @Test
    public void should_enter_password_and_click_next() {
        // given
        WebElement passwordInputElement = mock(WebElement.class);
        mockElementExists(LOC_PRIVATE_PASSWORD_INPUT, passwordInputElement);

        // when
        enterPasswordStep.enterPrivatePassword(SAMPLE_USER_BANK_ID_PASSWORD);

        // then
        verify(webDriver)
                .searchForFirstMatchingLocator(
                        ElementsSearchQuery.builder()
                                .searchFor(LOC_PRIVATE_PASSWORD_INPUT)
                                .searchForSeconds(10)
                                .build());
        verify(webDriver).sleepFor(1_000);
        verify(webDriver)
                .setValueToElement(SAMPLE_USER_BANK_ID_PASSWORD, LOC_PRIVATE_PASSWORD_INPUT);
        verify(webDriver).clickButton(LOC_SUBMIT_BUTTON);
        verifyNoMoreInteractions(webDriver);
    }

    @SuppressWarnings("SameParameterValue")
    private void mockElementExists(ElementLocator locator, WebElement element) {
        when(webDriver.searchForFirstMatchingLocator(any()))
                .thenReturn(ElementsSearchResult.of(locator, element));
    }
}
