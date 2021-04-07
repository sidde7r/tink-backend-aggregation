package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.BANK_ID_LOG_PREFIX;

import com.google.inject.Inject;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdIframeFirstWindow;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.steps.BankIdEnterPasswordStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.steps.BankIdEnterSSNStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.steps.BankIdPerform2FAStep;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PACKAGE, onConstructor = @__({@Inject}))
public class BankIdIframeController {

    private final BankIdEnterSSNStep enterSSNStep;
    private final BankIdPerform2FAStep perform2FAStep;
    private final BankIdEnterPasswordStep enterPrivatePasswordStep;

    public void authenticateWithCredentials(
            Credentials credentials, BankIdIframeFirstWindow firstWindow) {
        log.info(
                "{} Starting iframe authentication with window: {}",
                BANK_ID_LOG_PREFIX,
                firstWindow);

        if (firstWindow == BankIdIframeFirstWindow.ENTER_SSN) {
            enterSSNStep.enterSSN(credentials);
            log.info("{} SSN entered successfully", BANK_ID_LOG_PREFIX);
        }

        perform2FAStep.perform2FA();
        log.info("{} 2FA completed", BANK_ID_LOG_PREFIX);

        enterPrivatePasswordStep.enterPrivatePassword(credentials);
        log.info("{} Private password entered successfully", BANK_ID_LOG_PREFIX);
    }
}
