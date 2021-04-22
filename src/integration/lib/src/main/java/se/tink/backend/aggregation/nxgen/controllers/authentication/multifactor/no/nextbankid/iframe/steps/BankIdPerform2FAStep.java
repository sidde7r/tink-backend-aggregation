package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.steps;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.BANK_ID_LOG_PREFIX;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlLocators.LOC_CHANGE_2FA_METHOD_LINK;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlLocators.LOC_CHOOSE_2FA_METHOD_OPTION_BUTTON_WITH_LABEL;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.BankId2FAMethod.BANK_ID_APP_METHOD;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.BankIdWebDriver;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.BankIdElementsSearchQuery;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.BankId2FAMethod;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreen;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreensManager;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreensQuery;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class BankIdPerform2FAStep {

    private final BankIdWebDriver webDriver;
    private final BankIdScreensManager screensManager;

    private final BankIdAskUserToChoose2FAMethodNameStep askUserToChoose2FAMethodNameStep;

    private final BankIdAuthWithOneTimeCodeStep authWithOneTimeCodeStep;
    private final BankIdAuthWithMobileBankIdStep authWithMobileBankIdStep;
    private final BankIdAuthWithBankIdAppStep authWithBankIdAppStep;

    public void perform2FA() {
        BankIdScreen currentScreen =
                screensManager.waitForAnyScreenFromQuery(
                        BankIdScreensQuery.builder()
                                .waitForScreens(BankIdScreen.getAll2FAMethodScreens())
                                .waitForScreens(BankIdScreen.CHOOSE_2FA_METHOD_SCREEN)
                                .verifyNoErrorScreens(true)
                                .build());

        if (currentScreen == BankIdScreen.CHOOSE_2FA_METHOD_SCREEN) {
            handleFirstScreenIsChoose2FAMethod();
        } else {
            handleFirstScreenIs2FAMethod(currentScreen);
        }
    }

    private void handleFirstScreenIsChoose2FAMethod() {
        log.info("{} Starting with select 2FA method screen", BANK_ID_LOG_PREFIX);
        BankId2FAMethod chosenMethod = choose2FAMethod();
        authenticateWithMethod(chosenMethod);
    }

    private void handleFirstScreenIs2FAMethod(BankIdScreen currentScreen) {
        log.info("{} Starting with 2FA method screen: {}", BANK_ID_LOG_PREFIX, currentScreen);
        BankId2FAMethod method = BankId2FAMethod.get2FAMethodByScreen(currentScreen);

        boolean canChangeMethod = checkIfLinkToChange2FAMethodExists();
        if (!canChangeMethod) {
            authenticateWithMethod(method);
            return;
        }

        if (method == BANK_ID_APP_METHOD) {
            BankIdAuthWithBankIdAppUserChoice result =
                    authWithBankIdAppStep.authenticateWithBankIdApp(true);
            if (result == BankIdAuthWithBankIdAppUserChoice.AUTHENTICATE) {
                /*
                User chose to continue BankID app authentication and we didn't get any error - this means that
                authentication was successful
                 */
                return;
            }
        }

        clickLinkToChange2FAMethod();
        BankId2FAMethod chosenMethod = choose2FAMethod();
        authenticateWithMethod(chosenMethod);
    }

    private BankId2FAMethod choose2FAMethod() {
        String selectedMethodName = askUserToChoose2FAMethodNameStep.choose2FAMethodName();
        clickOptionButtonWithLabel(selectedMethodName);

        BankId2FAMethod selectedMethod = detectCurrentMethod();
        log.info(
                "{} 2FA method mapping: [{}={}]",
                BANK_ID_LOG_PREFIX,
                selectedMethodName,
                selectedMethod);

        return selectedMethod;
    }

    private BankId2FAMethod detectCurrentMethod() {
        log.info("{} Searching for any 2FA screen", BANK_ID_LOG_PREFIX);
        BankIdScreen current2FAScreen =
                screensManager.waitForAnyScreenFromQuery(
                        BankIdScreensQuery.builder()
                                .waitForScreens(BankIdScreen.getAll2FAMethodScreens())
                                .verifyNoErrorScreens(true)
                                .build());

        log.info("{} 2FA screen detected: {}", BANK_ID_LOG_PREFIX, current2FAScreen);
        return BankId2FAMethod.get2FAMethodByScreen(current2FAScreen);
    }

    private boolean checkIfLinkToChange2FAMethodExists() {
        boolean linkExists =
                webDriver
                        .searchForFirstMatchingLocator(
                                BankIdElementsSearchQuery.builder()
                                        .searchFor(LOC_CHANGE_2FA_METHOD_LINK)
                                        .searchForSeconds(2)
                                        .build())
                        .isNotEmpty();
        log.info("{} Does link to change 2FA method exist: {}", BANK_ID_LOG_PREFIX, linkExists);
        return linkExists;
    }

    private void clickLinkToChange2FAMethod() {
        webDriver.clickButton(LOC_CHANGE_2FA_METHOD_LINK);
    }

    private void clickOptionButtonWithLabel(String label) {
        webDriver.clickButton(LOC_CHOOSE_2FA_METHOD_OPTION_BUTTON_WITH_LABEL.apply(label));
    }

    private void authenticateWithMethod(BankId2FAMethod bankId2FAMethod) {
        switch (bankId2FAMethod) {
            case ONE_TIME_CODE_METHOD:
                authWithOneTimeCodeStep.authenticateWithOneTimeCode();
                break;
            case MOBILE_BANK_ID_METHOD:
                authWithMobileBankIdStep.authenticateWithMobileBankId();
                break;
            case BANK_ID_APP_METHOD:
                authWithBankIdAppStep.authenticateWithBankIdApp(false);
                break;
            default:
                throw new IllegalStateException("Unknown BankID 2FA method: " + bankId2FAMethod);
        }
    }
}
