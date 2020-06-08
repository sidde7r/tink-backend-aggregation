package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator.N26AppAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator.N26PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.passwordandapp.PasswordExternalAppAuthenticationController;

public class N26ExternalAppAuthAgent extends N26Agent {

    @Inject
    public N26ExternalAppAuthAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        N26AppAuthenticator authenticator =
                new N26AppAuthenticator(n26APiClient, sessionStorage, persistentStorage);
        N26PasswordAuthenticator passwordAuthenticator =
                new N26PasswordAuthenticator(n26APiClient, sessionStorage);
        return new PasswordExternalAppAuthenticationController(
                passwordAuthenticator, authenticator, supplementalInformationHelper);
    }
}
