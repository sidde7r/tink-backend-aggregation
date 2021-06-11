package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlLocators.LOC_CHANGE_2FA_METHOD_LINK;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlLocators.LOC_CHOOSE_2FA_METHOD_OPTION_BUTTON_WITH_LABEL;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdTestUtils.mockLocatorDoesNotExists;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdTestUtils.mockLocatorExists;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreen.CHOOSE_2FA_METHOD_SCREEN;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreen.THIRD_PARTY_APP_METHOD_SCREEN;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.BankIdWebDriver;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.BankIdElementLocator;
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
    private BankIdAskUserToChoose2FAMethodNameStep choose2FAStep;
    private BankIdAuthWithOneTimeCodeStep authWithOneTimeCodeStep;
    private BankIdAuthWithMobileBankIdStep authWithMobileBankIdStep;
    private BankIdAuthWithThirdPartyAppStep authWithBankIdAppStep;

    private InOrder mocksToVerifyInOrder;

    /*
    Real
     */
    private BankIdPerform2FAStep perform2FAStep;

    @Before
    public void setup() {
        webDriver = mock(BankIdWebDriver.class);
        screensManager = mock(BankIdScreensManager.class);
        choose2FAStep = mock(BankIdAskUserToChoose2FAMethodNameStep.class);
        authWithOneTimeCodeStep = mock(BankIdAuthWithOneTimeCodeStep.class);
        authWithMobileBankIdStep = mock(BankIdAuthWithMobileBankIdStep.class);
        authWithBankIdAppStep = mock(BankIdAuthWithThirdPartyAppStep.class);

        mocksToVerifyInOrder =
                inOrder(
                        webDriver,
                        screensManager,
                        choose2FAStep,
                        authWithOneTimeCodeStep,
                        authWithMobileBankIdStep,
                        authWithBankIdAppStep);

        perform2FAStep =
                new BankIdPerform2FAStep(
                        webDriver,
                        screensManager,
                        choose2FAStep,
                        authWithOneTimeCodeStep,
                        authWithMobileBankIdStep,
                        authWithBankIdAppStep);
    }

    @Test
    @Parameters(method = "all2FAMethodScreens")
    public void
            when_initial_screen_is_any_2FA_method_and_link_doesnt_exist_should_authenticate_with_current_method(
                    BankIdScreen currentScreen) {
        // given
        mockDetectCurrentScreenResults(currentScreen);
        mockLocatorDoesNotExists(LOC_CHANGE_2FA_METHOD_LINK, webDriver);

        // when
        perform2FAStep.perform2FA();

        // then
        verifyDetects2FAMethodOrChangeMethodScreen();
        verifyAuthenticatesWithCorrectMethod(currentScreen);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    @Parameters(method = "currentScreenIsNotBankIdAppTestParams")
    public void
            when_initial_screen_is_2FA_but_not_third_party_app_and_link_exists_should_authenticate_with_user_chosen_method(
                    BankIdScreen currentScreen, BankIdScreen screenAfterChoosingMethod) {
        // given
        mockDetectCurrentScreenResults(currentScreen, screenAfterChoosingMethod);
        mockLocatorExists(LOC_CHANGE_2FA_METHOD_LINK, webDriver);

        when(authWithBankIdAppStep.authenticateWithThirdPartyApp(anyBoolean()))
                .thenReturn(BankIdAuthWithThirdPartyAppUserChoice.CHANGE_METHOD);
        when(choose2FAStep.choose2FAMethodName()).thenReturn("SOME_SCREEN_SCRAPED_METHOD_NAME123");

        // when
        perform2FAStep.perform2FA();

        // then
        verifyDetects2FAMethodOrChangeMethodScreen();
        mocksToVerifyInOrder.verify(choose2FAStep).choose2FAMethodName();
        verifyClicksButtonWithLabel("SOME_SCREEN_SCRAPED_METHOD_NAME123");
        verifyDetects2FAMethodScreen();
        verifyAuthenticatesWithCorrectMethod(screenAfterChoosingMethod);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void
            when_initial_screen_is_third_party_app_and_link_exists_should_allow_user_to_continue_authentication() {
        // given
        mockDetectCurrentScreenResults(THIRD_PARTY_APP_METHOD_SCREEN);
        mockLocatorExists(LOC_CHANGE_2FA_METHOD_LINK, webDriver);

        when(authWithBankIdAppStep.authenticateWithThirdPartyApp(anyBoolean()))
                .thenReturn(BankIdAuthWithThirdPartyAppUserChoice.AUTHENTICATE);

        // when
        perform2FAStep.perform2FA();

        // then
        verifyDetects2FAMethodOrChangeMethodScreen();
        mocksToVerifyInOrder.verify(authWithBankIdAppStep).authenticateWithThirdPartyApp(true);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    @Parameters(method = "all2FAMethodScreens")
    public void
            when_initial_screen_is_third_party_app_and_link_exists_should_allow_user_to_authenticate_with_different_method(
                    BankIdScreen screenAfterChoosingOtherMethod) {
        // given
        mockDetectCurrentScreenResults(
                THIRD_PARTY_APP_METHOD_SCREEN, screenAfterChoosingOtherMethod);
        mockLocatorExists(LOC_CHANGE_2FA_METHOD_LINK, webDriver);

        when(authWithBankIdAppStep.authenticateWithThirdPartyApp(anyBoolean()))
                .thenReturn(BankIdAuthWithThirdPartyAppUserChoice.CHANGE_METHOD);
        when(choose2FAStep.choose2FAMethodName()).thenReturn("SOME_SCREEN_SCRAPED_METHOD_NAME");

        // when
        perform2FAStep.perform2FA();

        // then
        verifyDetects2FAMethodOrChangeMethodScreen();
        mocksToVerifyInOrder.verify(authWithBankIdAppStep).authenticateWithThirdPartyApp(true);
        mocksToVerifyInOrder.verify(choose2FAStep).choose2FAMethodName();
        verifyClicksButtonWithLabel("SOME_SCREEN_SCRAPED_METHOD_NAME");
        verifyDetects2FAMethodScreen();
        verifyAuthenticatesWithCorrectMethod(screenAfterChoosingOtherMethod);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @Test
    @Parameters(method = "all2FAMethodScreens")
    public void
            when_initial_screen_is_choose_2FA_method_should_authenticate_with_user_chosen_method(
                    BankIdScreen screenAfterChoosingMethod) {
        // given
        mockDetectCurrentScreenResults(CHOOSE_2FA_METHOD_SCREEN, screenAfterChoosingMethod);

        when(choose2FAStep.choose2FAMethodName()).thenReturn("SOME_SCREEN_SCRAPED_METHOD_NAME$$$");

        // when
        perform2FAStep.perform2FA();

        // then
        verifyDetects2FAMethodOrChangeMethodScreen();
        mocksToVerifyInOrder.verify(choose2FAStep).choose2FAMethodName();
        verifyClicksButtonWithLabel("SOME_SCREEN_SCRAPED_METHOD_NAME$$$");
        verifyDetects2FAMethodScreen();
        verifyAuthenticatesWithCorrectMethod(screenAfterChoosingMethod);
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @SuppressWarnings("unused")
    private static Object[] all2FAMethodScreens() {
        return BankIdScreen.getAll2FAMethodScreens().toArray();
    }

    @SuppressWarnings("unused")
    private static Object[] currentScreenIsNotBankIdAppTestParams() {
        BankIdScreen[] notBankIdAppScreens =
                BankIdScreen.getAll2FAMethodScreens().stream()
                        .filter(screen -> screen != THIRD_PARTY_APP_METHOD_SCREEN)
                        .toArray(BankIdScreen[]::new);

        List<Object[]> allTestParams = new ArrayList<>();
        for (BankIdScreen screen1 : notBankIdAppScreens) {
            for (BankIdScreen screen2 : BankIdScreen.getAll2FAMethodScreens()) {
                allTestParams.add(new Object[] {screen1, screen2});
            }
        }
        return allTestParams.toArray();
    }

    private void mockDetectCurrentScreenResults(BankIdScreen... screens) {
        BankIdScreen firstDetectedScreen = screens[0];
        BankIdScreen[] screensToBeDetectedLater =
                Stream.of(screens).skip(1).toArray(BankIdScreen[]::new);

        when(screensManager.waitForAnyScreenFromQuery(any(BankIdScreensQuery.class)))
                .thenReturn(firstDetectedScreen, screensToBeDetectedLater);
    }

    private void verifyDetects2FAMethodOrChangeMethodScreen() {
        mocksToVerifyInOrder
                .verify(screensManager)
                .waitForAnyScreenFromQuery(
                        BankIdScreensQuery.builder()
                                .waitForScreens(BankIdScreen.getAll2FAMethodScreens())
                                .waitForScreens(CHOOSE_2FA_METHOD_SCREEN)
                                .verifyNoErrorScreens(true)
                                .build());
    }

    private void verifyDetects2FAMethodScreen() {
        mocksToVerifyInOrder
                .verify(screensManager)
                .waitForAnyScreenFromQuery(
                        BankIdScreensQuery.builder()
                                .waitForScreens(BankIdScreen.getAll2FAMethodScreens())
                                .verifyNoErrorScreens(true)
                                .build());
    }

    private void verifyAuthenticatesWithCorrectMethod(BankIdScreen currentScreen) {
        switch (currentScreen) {
            case ONE_TIME_CODE_METHOD_SCREEN:
                mocksToVerifyInOrder.verify(authWithOneTimeCodeStep).authenticateWithOneTimeCode();
                break;
            case MOBILE_BANK_ID_SEND_REQUEST_SCREEN:
            case MOBILE_BANK_ID_ENTER_MOBILE_NUMBER_SCREEN:
                mocksToVerifyInOrder
                        .verify(authWithMobileBankIdStep)
                        .authenticateWithMobileBankId();
                break;
            case THIRD_PARTY_APP_METHOD_SCREEN:
                mocksToVerifyInOrder
                        .verify(authWithBankIdAppStep)
                        .authenticateWithThirdPartyApp(false);
                break;
        }
    }

    private void verifyClicksButtonWithLabel(String label) {
        ArgumentCaptor<BankIdElementLocator> locatorArgumentCaptor =
                ArgumentCaptor.forClass(BankIdElementLocator.class);

        mocksToVerifyInOrder.verify(webDriver).clickButton(locatorArgumentCaptor.capture());

        assertThat(locatorArgumentCaptor.getValue())
                .isEqualToComparingFieldByFieldRecursively(
                        LOC_CHOOSE_2FA_METHOD_OPTION_BUTTON_WITH_LABEL.apply(label));
    }
}
