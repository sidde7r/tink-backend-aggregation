package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.steps;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlLocators.LOC_SSN_INPUT;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlLocators.LOC_SUBMIT_BUTTON;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.openqa.selenium.WebElement;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.BankIdWebDriver;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.BankIdElementLocator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.BankIdElementsSearchQuery;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.BankIdElementsSearchResult;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreen;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreensManager;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreensQuery;

public class BankIdEnterSSNStepTest {

    private static final String SAMPLE_USER_SSN = "some SSN";

    /*
    Mocks
     */
    private BankIdWebDriver webDriver;
    private BankIdScreensManager screensManager;
    private Credentials credentials;

    private InOrder mocksToVerifyInOrder;

    /*
    Real
     */
    private BankIdEnterSSNStep enterSSNStep;

    @Before
    public void setup() {
        webDriver = mock(BankIdWebDriver.class);
        screensManager = mock(BankIdScreensManager.class);

        credentials = mock(Credentials.class);
        when(credentials.getField(Field.Key.DATE_OF_BIRTH)).thenReturn(SAMPLE_USER_SSN);

        mocksToVerifyInOrder = inOrder(webDriver, screensManager, credentials);

        enterSSNStep = new BankIdEnterSSNStep(webDriver, screensManager);
    }

    @Test
    public void should_enter_SSN_and_click_next() {
        // given
        mockScreenExists(BankIdScreen.ENTER_SSN_SCREEN);

        WebElement ssnInputElement = mock(WebElement.class);
        mockElementExists(LOC_SSN_INPUT, ssnInputElement);

        // when
        enterSSNStep.enterSSN(credentials);

        // then
        mocksToVerifyInOrder
                .verify(screensManager)
                .waitForAnyScreenFromQuery(
                        BankIdScreensQuery.builder()
                                .waitForScreens(BankIdScreen.ENTER_SSN_SCREEN)
                                .waitForSeconds(10)
                                .build());
        mocksToVerifyInOrder
                .verify(webDriver)
                .searchForFirstMatchingLocator(
                        BankIdElementsSearchQuery.builder()
                                .searchFor(LOC_SSN_INPUT)
                                .searchForSeconds(10)
                                .build());
        mocksToVerifyInOrder.verify(webDriver).setValueToElement(SAMPLE_USER_SSN, LOC_SSN_INPUT);
        mocksToVerifyInOrder.verify(webDriver).clickButton(LOC_SUBMIT_BUTTON);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @SuppressWarnings("SameParameterValue")
    private void mockScreenExists(BankIdScreen screen) {
        when(screensManager.waitForAnyScreenFromQuery(any())).thenReturn(screen);
    }

    @SuppressWarnings("SameParameterValue")
    private void mockElementExists(BankIdElementLocator selector, WebElement element) {
        when(webDriver.searchForFirstMatchingLocator(any()))
                .thenReturn(BankIdElementsSearchResult.of(selector, element));
    }
}
