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
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.BankIdWebDriver;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.BankIdElementLocator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.BankIdElementsSearchQuery;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.BankIdElementsSearchResult;

public class BankIdEnterPasswordStepTest {

    private static final String SAMPLE_USER_BANK_ID_PASSWORD = "some password";

    /*
    Mocks
     */
    private BankIdWebDriver webDriver;
    private Credentials credentials;

    /*
    Real
     */
    private BankIdEnterPasswordStep enterPasswordStep;

    @Before
    public void setup() {
        webDriver = mock(BankIdWebDriver.class);

        credentials = mock(Credentials.class);
        when(credentials.getField(Field.Key.BANKID_PASSWORD))
                .thenReturn(SAMPLE_USER_BANK_ID_PASSWORD);

        enterPasswordStep = new BankIdEnterPasswordStep(webDriver);
    }

    @Test
    public void should_enter_password_and_click_next() {
        // given
        WebElement passwordInputElement = mock(WebElement.class);
        mockElementExists(LOC_PRIVATE_PASSWORD_INPUT, passwordInputElement);

        // when
        enterPasswordStep.enterPrivatePassword(credentials);

        // then
        verify(webDriver)
                .searchForFirstMatchingLocator(
                        BankIdElementsSearchQuery.builder()
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
    private void mockElementExists(BankIdElementLocator locator, WebElement element) {
        when(webDriver.searchForFirstMatchingLocator(any()))
                .thenReturn(BankIdElementsSearchResult.of(locator, element));
    }
}
