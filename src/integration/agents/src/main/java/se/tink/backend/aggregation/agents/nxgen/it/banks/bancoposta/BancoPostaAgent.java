package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.BancoPostaAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.UserContext;
import se.tink.backend.aggregation.nxgen.agents.SubsequentProgressiveGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class BancoPostaAgent extends SubsequentProgressiveGenerationAgent {

    private final BancoPostaApiClient apiClient;
    private UserContext userContext;

    @Inject
    public BancoPostaAgent(AgentComponentProvider agentComponentProvider) {
        super(agentComponentProvider);

        this.userContext = new UserContext(persistentStorage);
        this.apiClient = new BancoPostaApiClient(client, userContext);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public StatelessProgressiveAuthenticator getAuthenticator() {
        return new BancoPostaAuthenticator(apiClient, userContext, catalog);
    }
}
