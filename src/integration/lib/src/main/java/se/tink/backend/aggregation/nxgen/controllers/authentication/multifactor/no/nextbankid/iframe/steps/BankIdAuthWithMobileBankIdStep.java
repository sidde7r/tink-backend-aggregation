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
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreen;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreensManager;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreensQuery;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.integration.webdriver.service.WebDriverService;
import se.tink.integration.webdriver.service.searchelements.ElementsSearchQuery;
import se.tink.libraries.i18n_aggregation.Catalog;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class BankIdAuthWithMobileBankIdStep {

    private final WebDriverService webDriver;
    private final BankIdScreensManager screensManager;

    private final Catalog catalog;
    private final SupplementalInformationController supplementalInformationController;

    public void authenticateWithMobileBankId() {
        verifyEnteringMobileNumberIsNotRequired();

        sendMobileRequest();
        waitForReferenceWordsScreen();

        String referenceWords = searchForReferenceWords();
        askUserToConfirmBankIdMobile(referenceWords);

        verifyUserConfirmation();
    }

    private void verifyEnteringMobileNumberIsNotRequired() {
        BankIdScreen screen =
                screensManager.waitForAnyScreenFromQuery(
                        BankIdScreensQuery.builder()
                                .waitForScreens(BankIdScreen.MOBILE_BANK_ID_SEND_REQUEST_SCREEN)
                                .waitForScreens(
                                        BankIdScreen.MOBILE_BANK_ID_ENTER_MOBILE_NUMBER_SCREEN)
                                .waitForSeconds(10)
                                .build());

        // details in ITE-2238
        if (screen == BankIdScreen.MOBILE_BANK_ID_ENTER_MOBILE_NUMBER_SCREEN) {
            throw new UnsupportedOperationException("Entering mobile number is not supported yet");
        }
    }

    private void sendMobileRequest() {
        log.info("{} Sending BankID mobile request", BANK_ID_LOG_PREFIX);
        webDriver.clickButton(LOC_SUBMIT_BUTTON);
    }

    private void waitForReferenceWordsScreen() {
        log.info("{} Waiting for reference words screen", BankIdConstants.BANK_ID_LOG_PREFIX);
        screensManager.waitForAnyScreenFromQuery(
                BankIdScreensQuery.builder()
                        .waitForScreens(BankIdScreen.MOBILE_BANK_ID_REFERENCE_WORDS_SCREEN)
                        .waitForSeconds(10)
                        .verifyNoErrorScreens(true)
                        .build());
    }

    private String searchForReferenceWords() {
        log.info("{} Searching for reference words", BANK_ID_LOG_PREFIX);

        WebElement referenceWordsElement =
                webDriver
                        .searchForFirstMatchingLocator(
                                ElementsSearchQuery.builder()
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
