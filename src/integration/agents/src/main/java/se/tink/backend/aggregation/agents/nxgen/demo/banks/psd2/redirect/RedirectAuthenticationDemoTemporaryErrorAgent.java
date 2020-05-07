package se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect;

import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class RedirectAuthenticationDemoTemporaryErrorAgent extends RedirectAuthenticationDemoAgent {

    public RedirectAuthenticationDemoTemporaryErrorAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return credentials -> {
            throw new RuntimeException("something went wrong");
        };
    }
}
