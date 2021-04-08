package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.steps;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlLocators.LOC_CHANGE_2FA_METHOD_LINK;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants;
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

    private final BankIdAuthWithChipCodeStep authWithChipCodeStep;
    private final BankIdAuthWithMobileBankIdStep authWithMobileBankIdStep;
    private final BankIdAuthWithBankIdAppStep authWithBankIdAppStep;

    public void perform2FA() {
        BankIdScreen current2FAScreen = get2FAScreen();
        log.info(
                "{} 2FA screen detected: {}", BankIdConstants.BANK_ID_LOG_PREFIX, current2FAScreen);

        BankId2FAMethod bankId2FAMethod = BankId2FAMethod.get2FAMethodByScreen(current2FAScreen);
        logIfLinkToChangeMethodExists();

        authenticateWithMethod(bankId2FAMethod);
    }

    private BankIdScreen get2FAScreen() {
        log.info("{} Searching for any 2FA screen", BankIdConstants.BANK_ID_LOG_PREFIX);
        return screensManager.waitForAnyScreenFromQuery(
                BankIdScreensQuery.builder()
                        .waitForScreens(BankIdScreen.getAll2FAMethodScreens())
                        .verifyNoErrorScreens(true)
                        .build());
    }

    private void authenticateWithMethod(BankId2FAMethod bankId2FAMethod) {
        switch (bankId2FAMethod) {
            case CODE_CHIP_METHOD:
                authWithChipCodeStep.authenticateWithChipCode();
                break;
            case MOBILE_BANK_ID_METHOD:
                authWithMobileBankIdStep.authenticateWithMobileBankId();
                break;
            case BANK_ID_APP_METHOD:
                authWithBankIdAppStep.authenticateWithBankIdApp();
                break;
            default:
                throw new IllegalStateException("Unknown BankID 2FA method: " + bankId2FAMethod);
        }
    }

    private void logIfLinkToChangeMethodExists() {
        boolean linkExists =
                webDriver
                        .searchForFirstMatchingLocator(
                                BankIdElementsSearchQuery.builder()
                                        .searchFor(LOC_CHANGE_2FA_METHOD_LINK)
                                        .waitForSeconds(0)
                                        .build())
                        .isNotEmpty();
        log.info(
                "{} Does link to change 2FA method exist: {}",
                BankIdConstants.BANK_ID_LOG_PREFIX,
                linkExists);
    }
}
