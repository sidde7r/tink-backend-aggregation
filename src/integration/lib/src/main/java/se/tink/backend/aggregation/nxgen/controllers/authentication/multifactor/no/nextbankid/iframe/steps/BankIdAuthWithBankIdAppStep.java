package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.steps;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.BANK_ID_APP_TIMEOUT_IN_SECONDS;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.BANK_ID_LOG_PREFIX;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreen.ENTER_BANK_ID_PASSWORD_SCREEN;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.utils.supplementalfields.NorwegianFields;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreensManager;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.screens.BankIdScreensQuery;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.i18n.Catalog;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class BankIdAuthWithBankIdAppStep {

    private final BankIdScreensManager screensManager;
    private final Catalog catalog;
    private final SupplementalInformationController supplementalInformationController;

    public BankIdAuthWithBankIdAppUserChoice authenticateWithBankIdApp(boolean canChangeMethod) {
        BankIdAuthWithBankIdAppUserChoice userChoice = askUserToConfirmBankIdApp(canChangeMethod);

        if (userChoice == BankIdAuthWithBankIdAppUserChoice.AUTHENTICATE) {
            verifyUserHasConfirmed();
        }

        return userChoice;
    }

    @SuppressWarnings("unused")
    private BankIdAuthWithBankIdAppUserChoice askUserToConfirmBankIdApp(boolean canChangeMethod) {
        log.info(
                "{} Asking user to confirm BankID app. Can change method: {}",
                BANK_ID_LOG_PREFIX,
                canChangeMethod);

        if (canChangeMethod) {
            return askUserToConfirmAuthenticationOrChangeMethod();
        }

        askUserToConfirmAuthentication();
        return BankIdAuthWithBankIdAppUserChoice.AUTHENTICATE;
    }

    private void askUserToConfirmAuthentication() {
        Field confirmBankIdAppField = NorwegianFields.BankIdAppField.build(catalog);
        try {
            supplementalInformationController.askSupplementalInformationSync(confirmBankIdAppField);
        } catch (SupplementalInfoException e) {
            // ignore empty response!
            // we're actually not interested in response at all, we just show a text!
        }
    }

    /*
    This is temporarily hard coded to always continue BankID app authentication until sdk-web helps us to prepare
    supplemental info screen
     */
    private BankIdAuthWithBankIdAppUserChoice askUserToConfirmAuthenticationOrChangeMethod() {
        askUserToConfirmAuthentication();
        return BankIdAuthWithBankIdAppUserChoice.AUTHENTICATE;
    }

    private void verifyUserHasConfirmed() {
        log.info("{} Looking for private password screen after BankID app", BANK_ID_LOG_PREFIX);

        screensManager.waitForAnyScreenFromQuery(
                BankIdScreensQuery.builder()
                        .waitForScreens(ENTER_BANK_ID_PASSWORD_SCREEN)
                        .waitForSeconds(BANK_ID_APP_TIMEOUT_IN_SECONDS)
                        .verifyNoErrorScreens(true)
                        .build());
    }
}
