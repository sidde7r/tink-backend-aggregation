package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.steps;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.BankIdWebDriver;

@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class BankIdPerform2FAStep {

    private final BankIdWebDriver webDriver;

    public void perform2FA() {
        // will be implemented in next PR
    }
}
