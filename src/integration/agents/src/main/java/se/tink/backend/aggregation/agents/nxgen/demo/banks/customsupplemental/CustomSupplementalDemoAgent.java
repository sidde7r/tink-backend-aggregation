package se.tink.backend.aggregation.agents.nxgen.demo.banks.customsupplemental;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.customsupplemental.authenticator.CustomSupplementalAuthenticator;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class CustomSupplementalDemoAgent extends NextGenerationAgent {

    @Inject
    public CustomSupplementalDemoAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new CustomSupplementalAuthenticator(supplementalInformationController);
    }
}
