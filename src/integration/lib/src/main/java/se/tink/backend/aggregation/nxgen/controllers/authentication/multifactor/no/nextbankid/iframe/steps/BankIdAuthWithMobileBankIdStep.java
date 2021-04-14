package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.steps;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.BANK_ID_LOG_PREFIX;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlLocators.LOC_REFERENCE_WORDS;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlLocators.LOC_SUBMIT_BUTTON;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.MOBILE_BANK_ID_TIMEOUT_IN_SECONDS;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreen.ENTER_BANK_ID_PASSWORD_SCREEN;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebElement;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.utils.supplementalfields.NorwegianFields;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.BankIdWebDriver;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.BankIdElementsSearchQuery;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreensManager;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreensQuery;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.i18n.Catalog;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class BankIdAuthWithMobileBankIdStep {

    private final BankIdWebDriver webDriver;
    private final BankIdScreensManager screensManager;

    private final Catalog catalog;
    private final SupplementalInformationController supplementalInformationController;

    public void authenticateWithMobileBankId() {
        sendMobileRequest();

        String referenceWords = searchForReferenceWords();
        askUserToConfirmBankIdMobile(referenceWords);

        verifyUserConfirmation();
    }

    private void sendMobileRequest() {
        log.info("{} Sending BankID mobile request", BANK_ID_LOG_PREFIX);
        webDriver.clickButton(LOC_SUBMIT_BUTTON);
    }

    private String searchForReferenceWords() {
        log.info("{} Searching for reference words", BANK_ID_LOG_PREFIX);

        WebElement referenceWordsElement =
                webDriver
                        .searchForFirstMatchingLocator(
                                BankIdElementsSearchQuery.builder()
                                        .searchFor(LOC_REFERENCE_WORDS)
                                        .searchForSeconds(10)
                                        .build())
                        .getFirstFoundElement()
                        .orElseThrow(
                                () -> new IllegalStateException("Could not find reference words"));

        return referenceWordsElement.getText().trim();
    }

    private void askUserToConfirmBankIdMobile(String referenceWords) {
        log.info("{} Asking user to confirm BankID mobile", BANK_ID_LOG_PREFIX);

        Field confirmBankIdMobileField =
                NorwegianFields.BankIdReferenceInfo.build(catalog, referenceWords);
        try {
            supplementalInformationController.askSupplementalInformationSync(
                    confirmBankIdMobileField);
        } catch (SupplementalInfoException e) {
            // ignore empty response!
            // we're actually not interested in response at all, we just show a text!
        }
    }

    private void verifyUserConfirmation() {
        log.info("{} Waiting for private password screen after mobile BankID", BANK_ID_LOG_PREFIX);
        screensManager.waitForAnyScreenFromQuery(
                BankIdScreensQuery.builder()
                        .waitForScreens(ENTER_BANK_ID_PASSWORD_SCREEN)
                        .waitForSeconds(MOBILE_BANK_ID_TIMEOUT_IN_SECONDS)
                        .verifyNoErrorScreens(true)
                        .build());
    }
}
