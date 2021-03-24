package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskev;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskev.authenticator.JyskeBankNemidAuthenticator;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class JyskeBankAgent extends NextGenerationAgent {
    protected final JyskeBankApiClient apiClient;
    private final StatusUpdater statusUpdater;

    @Inject
    public JyskeBankAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        client.setFollowRedirects(false);
        this.apiClient = new JyskeBankApiClient(client);
        this.statusUpdater = componentProvider.getContext();
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        JyskeBankNemidAuthenticator jyskeBankNemidAuthenticator =
                new JyskeBankNemidAuthenticator(
                        apiClient,
                        sessionStorage,
                        persistentStorage,
                        catalog,
                        statusUpdater,
                        supplementalInformationController,
                        metricContext);

        return new AutoAuthenticationController(
                request, systemUpdater, jyskeBankNemidAuthenticator, jyskeBankNemidAuthenticator);
    }
}
