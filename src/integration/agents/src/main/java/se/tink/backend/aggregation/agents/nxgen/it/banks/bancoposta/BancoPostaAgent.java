package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.BancoPostaAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.BancoPostaStorage;
import se.tink.backend.aggregation.nxgen.agents.SubsequentProgressiveGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

@AgentCapabilities
public final class BancoPostaAgent extends SubsequentProgressiveGenerationAgent {

    private final BancoPostaApiClient apiClient;
    private final BancoPostaStorage storage;

    @Inject
    public BancoPostaAgent(AgentComponentProvider agentComponentProvider) {
        super(agentComponentProvider);
        this.storage = new BancoPostaStorage(persistentStorage);
        this.apiClient = new BancoPostaApiClient(client, storage);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public StatelessProgressiveAuthenticator getAuthenticator() {
        return new BancoPostaAuthenticator(apiClient, storage, catalog);
    }
}
