package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.steps;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlLocators.LOC_CHANGE_2FA_METHOD_LINK;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.BankIdWebDriver;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.BankIdElementsSearchQuery;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreen;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreensManager;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreensQuery;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class BankIdPerform2FAStep {

    private final BankIdWebDriver webDriver;
    private final BankIdScreensManager screensManager;

    public void perform2FA() {
        BankIdScreen current2FAScreen = get2FAScreen();
        log.info(
                "{} 2FA screen detected: {}", BankIdConstants.BANK_ID_LOG_PREFIX, current2FAScreen);

        logIfLinkToChangeMethodExists();

        // the rest will be implemented in next PRs
    }

    private BankIdScreen get2FAScreen() {
        log.info("{} Searching for any 2FA screen", BankIdConstants.BANK_ID_LOG_PREFIX);
        return screensManager.waitForAnyScreenFromQuery(
                BankIdScreensQuery.builder()
                        .waitForScreens(BankIdScreen.getAll2FAMethodScreens())
                        .build());
    }

    private void logIfLinkToChangeMethodExists() {
        boolean linkExists =
                webDriver
                        .searchForFirstMatchingLocator(
                                BankIdElementsSearchQuery.builder()
                                        .searchFor(LOC_CHANGE_2FA_METHOD_LINK)
                                        .searchOnlyOnce(true)
                                        .build())
                        .isNotEmpty();
        log.info(
                "{} Does link to change 2FA method exist: {}",
                BankIdConstants.BANK_ID_LOG_PREFIX,
                linkExists);
    }
}
