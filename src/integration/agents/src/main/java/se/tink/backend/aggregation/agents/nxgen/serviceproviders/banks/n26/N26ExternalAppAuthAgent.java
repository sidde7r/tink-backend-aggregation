package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26;

import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator.N26AppAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator.N26PasswordAuthenticator;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.passwordandapp.PasswordExternalAppAuthenticationController;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class N26ExternalAppAuthAgent extends N26Agent {

    public N26ExternalAppAuthAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        N26AppAuthenticator authenticator =
                new N26AppAuthenticator(n26APiClient, sessionStorage, persistentStorage);
        N26PasswordAuthenticator passwordAuthenticator = new N26PasswordAuthenticator(n26APiClient);
        return new PasswordExternalAppAuthenticationController(
                passwordAuthenticator, authenticator, supplementalInformationHelper);
    }
}
