package se.tink.backend.aggregation.agents.nxgen.it.banks.isp;

import se.tink.backend.aggregation.agents.ProgressiveAuthAgent;
import se.tink.backend.aggregation.nxgen.agents.SubsequentProgressiveGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class IspAgent extends SubsequentProgressiveGenerationAgent implements ProgressiveAuthAgent {

    private final IspApiClient apiClient;

    public IspAgent(final AgentComponentProvider componentProvider) {
        super(componentProvider);
        applyFilters();
        apiClient = new IspApiClient(client, this.sessionStorage, this.persistentStorage);
    }

    private void applyFilters() {
        client.addFilter(new IspSignEncryptFilter());
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public StatelessProgressiveAuthenticator getAuthenticator() {
        return new IspAuthenticator(
                apiClient, supplementalInformationFormer, sessionStorage, persistentStorage);
    }
}
