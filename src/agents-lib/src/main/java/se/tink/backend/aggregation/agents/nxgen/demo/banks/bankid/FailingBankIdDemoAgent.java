package se.tink.backend.aggregation.agents.nxgen.demo.banks.bankid;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.demo.NextGenerationDemoAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.rpc.CredentialsRequest;


public class FailingBankIdDemoAgent extends NextGenerationDemoAgent {

    private DemoBankIdAuthenticator authenticator;

    public FailingBankIdDemoAgent(CredentialsRequest request,
            AgentContext context,
            SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        this.authenticator = new DemoBankIdAuthenticator(credentials, false);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new TypedAuthenticationController(
                new BankIdAuthenticationController<>(context, authenticator),
                new PasswordAuthenticationController(authenticator));
    }
}
