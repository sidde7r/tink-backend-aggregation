package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.steps;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlLocators.LOC_CHANGE_2FA_METHOD_LINK;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.BankIdWebDriver;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.BankIdElementsSearchQuery;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.BankIdElementsSearchResult;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreen;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreensManager;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreensQuery;

@RunWith(JUnitParamsRunner.class)
public class BankIdPerform2FAStepTest {

    /*
    Mocks
     */
    private BankIdWebDriver webDriver;
    private BankIdScreensManager screensManager;
    private BankIdAuthWithChipCodeStep authWithChipCodeStep;
    private BankIdAuthWithMobileBankIdStep authWithMobileBankIdStep;
    private BankIdAuthWithBankIdAppStep authWithBankIdAppStep;

    private InOrder mocksToVerifyInOrder;

    /*
    Real
     */
    private BankIdPerform2FAStep perform2FAStep;

    @Before
    public void setup() {
        webDriver = mock(BankIdWebDriver.class);
        screensManager = mock(BankIdScreensManager.class);
        authWithChipCodeStep = mock(BankIdAuthWithChipCodeStep.class);
        authWithMobileBankIdStep = mock(BankIdAuthWithMobileBankIdStep.class);
        authWithBankIdAppStep = mock(BankIdAuthWithBankIdAppStep.class);

        mocksToVerifyInOrder =
                inOrder(
                        webDriver,
                        screensManager,
                        authWithChipCodeStep,
                        authWithMobileBankIdStep,
                        authWithBankIdAppStep);

        perform2FAStep =
                new BankIdPerform2FAStep(
                        webDriver,
                        screensManager,
                        authWithChipCodeStep,
                        authWithMobileBankIdStep,
                        authWithBankIdAppStep);
    }

    @Test
    @Parameters(method = "all2FAMethodScreens")
    public void should_perform_2FA(BankIdScreen currentScreen) {
        // given
        mockDetectCurrentScreenResult(currentScreen);
        mockNoOtherElementsExist();

        // when
        perform2FAStep.perform2FA();

        // then
        verifyLogsIfLinkToChangeMethodExists();
        switch (currentScreen) {
            case CODE_CHIP_METHOD_SCREEN:
                mocksToVerifyInOrder.verify(authWithChipCodeStep).authenticateWithChipCode();
                break;
            case MOBILE_BANK_ID_METHOD_SCREEN:
                mocksToVerifyInOrder
                        .verify(authWithMobileBankIdStep)
                        .authenticateWithMobileBankId();
                break;
            case BANK_ID_APP_METHOD_SCREEN:
                mocksToVerifyInOrder.verify(authWithBankIdAppStep).authenticateWithBankIdApp();
                break;
        }
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @SuppressWarnings("unused")
    private static Object[] all2FAMethodScreens() {
        return BankIdScreen.getAll2FAMethodScreens().toArray();
    }

    private void verifyLogsIfLinkToChangeMethodExists() {
        mocksToVerifyInOrder
                .verify(webDriver)
                .searchForFirstMatchingLocator(
                        BankIdElementsSearchQuery.builder()
                                .searchFor(LOC_CHANGE_2FA_METHOD_LINK)
                                .searchOnlyOnce()
                                .build());
    }

    private void mockDetectCurrentScreenResult(BankIdScreen screen) {
        when(screensManager.waitForAnyScreenFromQuery(any(BankIdScreensQuery.class)))
                .thenReturn(screen);
    }

    private void mockNoOtherElementsExist() {
        when(webDriver.searchForFirstMatchingLocator(any()))
                .thenReturn(BankIdElementsSearchResult.empty());
    }
}
