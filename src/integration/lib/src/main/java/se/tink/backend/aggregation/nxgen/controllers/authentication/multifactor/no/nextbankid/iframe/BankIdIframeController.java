package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe;

import com.google.inject.Inject;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdIframeFirstStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.steps.BankIdEnterPasswordStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.steps.BankIdEnterSSNStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.steps.BankIdPerform2FAStep;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE, onConstructor = @__({@Inject}))
public class BankIdIframeController {

    private final BankIdEnterSSNStep enterSSNStep;
    private final BankIdPerform2FAStep perform2FAStep;
    private final BankIdEnterPasswordStep enterPrivatePasswordStep;

    public void authenticateWithCredentials(
            Credentials credentials, BankIdIframeFirstStep firstStep) {

        if (firstStep == BankIdIframeFirstStep.ENTER_SSN) {
            enterSSNStep.enterSSN(credentials);
        }

        perform2FAStep.perform2FA();

        enterPrivatePasswordStep.enterPrivatePassword(credentials);
    }
}
