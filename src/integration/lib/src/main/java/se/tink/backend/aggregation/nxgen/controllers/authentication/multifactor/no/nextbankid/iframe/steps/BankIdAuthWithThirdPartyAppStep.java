package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.steps;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.BANK_ID_LOG_PREFIX;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.THIRD_PARTY_APP_TIMEOUT_IN_SECONDS;
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
import se.tink.libraries.i18n_aggregation.Catalog;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class BankIdAuthWithThirdPartyAppStep {

    private final BankIdScreensManager screensManager;
    private final Catalog catalog;
    private final SupplementalInformationController supplementalInformationController;

    public BankIdAuthWithThirdPartyAppUserChoice authenticateWithThirdPartyApp(
            boolean canChangeMethod) {
        BankIdAuthWithThirdPartyAppUserChoice userChoice =
                askUserToConfirmAuthInApp(canChangeMethod);

        if (userChoice == BankIdAuthWithThirdPartyAppUserChoice.AUTHENTICATE) {
            verifyUserHasConfirmed();
        }

        return userChoice;
    }

    private BankIdAuthWithThirdPartyAppUserChoice askUserToConfirmAuthInApp(
            boolean canChangeMethod) {
        log.info(
                "{} Asking user to confirm third party app authentication. Can change method: {}",
                BANK_ID_LOG_PREFIX,
                canChangeMethod);

        if (canChangeMethod) {
            return askUserToConfirmAuthenticationOrChangeMethod();
        }

        askUserToConfirmAuthentication();
        return BankIdAuthWithThirdPartyAppUserChoice.AUTHENTICATE;
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
    This is temporarily hard coded to always continue third party app authentication until sdk-web helps us to prepare
    supplemental info screen
     */
    private BankIdAuthWithThirdPartyAppUserChoice askUserToConfirmAuthenticationOrChangeMethod() {
        askUserToConfirmAuthentication();
        return BankIdAuthWithThirdPartyAppUserChoice.AUTHENTICATE;
    }

    private void verifyUserHasConfirmed() {
        log.info(
                "{} Looking for private password screen after third party app authentication",
                BANK_ID_LOG_PREFIX);

        screensManager.waitForAnyScreenFromQuery(
                BankIdScreensQuery.builder()
                        .waitForScreens(ENTER_BANK_ID_PASSWORD_SCREEN)
                        .waitForSeconds(THIRD_PARTY_APP_TIMEOUT_IN_SECONDS)
                        .verifyNoErrorScreens(true)
                        .build());
    }
}
