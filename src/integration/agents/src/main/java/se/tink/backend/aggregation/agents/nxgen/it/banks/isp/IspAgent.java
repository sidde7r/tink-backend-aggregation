package se.tink.backend.aggregation.agents.nxgen.it.banks.isp;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.progressive.ProgressiveAuthAgent;
import se.tink.backend.aggregation.nxgen.agents.SubsequentProgressiveGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class IspAgent extends SubsequentProgressiveGenerationAgent implements ProgressiveAuthAgent {

    private final IspApiClient apiClient;

    @Inject
    public IspAgent(final AgentComponentProvider componentProvider) {
        super(componentProvider);
        applyFilters();
        apiClient = new IspApiClient(client, this.sessionStorage);
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
