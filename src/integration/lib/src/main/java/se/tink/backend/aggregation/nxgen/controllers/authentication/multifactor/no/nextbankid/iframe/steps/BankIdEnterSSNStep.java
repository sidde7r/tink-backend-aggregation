package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.steps;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlLocators.LOC_SSN_INPUT;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlLocators.LOC_SUBMIT_BUTTON;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreen;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreensManager;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreensQuery;
import se.tink.integration.webdriver.service.WebDriverService;
import se.tink.integration.webdriver.service.searchelements.ElementsSearchQuery;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class BankIdEnterSSNStep {

    private final WebDriverService driver;
    private final BankIdScreensManager screensManager;

    public void enterSSN(String ssn) {
        waitForSSNScreen();
        waitForSSNInput();

        setSsnInput(ssn);
        clickSubmit();
    }

    private void waitForSSNScreen() {
        log.info("{} Waiting for SSN screen", BankIdConstants.BANK_ID_LOG_PREFIX);
        screensManager.waitForAnyScreenFromQuery(
                BankIdScreensQuery.builder()
                        .waitForScreens(BankIdScreen.ENTER_SSN_SCREEN)
                        .waitForSeconds(10)
                        .verifyNoErrorScreens(true)
                        .build());
    }

    private void waitForSSNInput() {
        log.info("{} Waiting for SSN input", BankIdConstants.BANK_ID_LOG_PREFIX);
        boolean ssnInputFound =
                driver.searchForFirstMatchingLocator(
                                ElementsSearchQuery.builder()
                                        .searchFor(LOC_SSN_INPUT)
                                        .searchForSeconds(10)
                                        .build())
                        .isNotEmpty();
        if (!ssnInputFound) {
            throw new IllegalStateException("Cannot find SSN input");
        }
    }

    private void setSsnInput(String ssn) {
        log.info("{} Entering SSN", BankIdConstants.BANK_ID_LOG_PREFIX);
        driver.setValueToElement(ssn, LOC_SSN_INPUT);
    }

    private void clickSubmit() {
        log.info("{} Clicking submit SSN button", BankIdConstants.BANK_ID_LOG_PREFIX);
        driver.clickButton(LOC_SUBMIT_BUTTON);
    }
}
