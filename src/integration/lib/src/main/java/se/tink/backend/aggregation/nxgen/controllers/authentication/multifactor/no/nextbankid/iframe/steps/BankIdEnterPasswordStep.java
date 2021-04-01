package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.steps;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.BankIdWebDriver;

@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class BankIdEnterPasswordStep {

    private final BankIdWebDriver webDriver;

    public void enterPrivatePassword(Credentials credentials) {
        // will be implemented in next PR
    }
}
