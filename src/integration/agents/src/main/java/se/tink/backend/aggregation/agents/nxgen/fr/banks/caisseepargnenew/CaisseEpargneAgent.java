package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.authenticator.CaisseEpargnePasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class CaisseEpargneAgent extends NextGenerationAgent {
    private final CaisseEpargneApiClient apiClient;

    @Inject
    protected CaisseEpargneAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        apiClient = new CaisseEpargneApiClient(client, sessionStorage);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(
                new CaisseEpargnePasswordAuthenticator(apiClient));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }
}
