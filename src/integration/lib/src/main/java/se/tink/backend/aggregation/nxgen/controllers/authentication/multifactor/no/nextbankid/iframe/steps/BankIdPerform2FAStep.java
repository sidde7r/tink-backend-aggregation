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

    private final BankIdChoose2FAMethodNameStep choose2FAStep;

    private final BankIdAuthWithOneTimeCodeStep authWithOneTimeCodeStep;
    private final BankIdAuthWithMobileBankIdStep authWithMobileBankIdStep;
    private final BankIdAuthWithBankIdAppStep authWithBankIdAppStep;

    public void perform2FA() {
        BankId2FAMethod bankId2FAMethod = detectCurrentMethod();
        boolean canChangeMethod = checkIfLinkToChange2FAMethodExists();

        authenticateStartingFromMethod(bankId2FAMethod, canChangeMethod);
    }

    private BankId2FAMethod detectCurrentMethod() {
        BankIdScreen current2FAScreen = get2FAScreen();
        log.info("{} 2FA screen detected: {}", BANK_ID_LOG_PREFIX, current2FAScreen);
        return BankId2FAMethod.get2FAMethodByScreen(current2FAScreen);
    }

    private BankIdScreen get2FAScreen() {
        log.info("{} Searching for any 2FA screen", BANK_ID_LOG_PREFIX);
        return screensManager.waitForAnyScreenFromQuery(
                BankIdScreensQuery.builder()
                        .waitForScreens(BankIdScreen.getAll2FAMethodScreens())
                        .verifyNoErrorScreens(true)
                        .build());
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

    private void authenticateStartingFromMethod(
            BankId2FAMethod bankId2FAMethod, boolean canChangeMethod) {

        if (!canChangeMethod) {
            authenticateWithMethod(bankId2FAMethod);
            return;
        }

        if (bankId2FAMethod == BANK_ID_APP_METHOD) {
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

        BankId2FAMethod chosenMethod = choose2FAMethod();
        authenticateWithMethod(chosenMethod);
    }

    private BankId2FAMethod choose2FAMethod() {
        String selectedMethodName = choose2FAStep.choose2FAMethodName();
        clickOptionButtonWithLabel(selectedMethodName);
        return detectCurrentMethod();
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
