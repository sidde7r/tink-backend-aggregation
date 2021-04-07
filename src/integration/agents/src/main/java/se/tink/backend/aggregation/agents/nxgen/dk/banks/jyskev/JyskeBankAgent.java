package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskev;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskev.authenticator.JyskeBankNemidAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskev.fetcher.identity.JyskeIdentityDataFetcher;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

@AgentCapabilities({IDENTITY_DATA})
public class JyskeBankAgent extends NextGenerationAgent implements RefreshIdentityDataExecutor {
    protected final JyskeBankApiClient apiClient;
    private final StatusUpdater statusUpdater;
    private final JyskeIdentityDataFetcher identityDataFetcher;

    @Inject
    public JyskeBankAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        client.setFollowRedirects(false);
        this.apiClient = new JyskeBankApiClient(client, sessionStorage);
        this.statusUpdater = componentProvider.getContext();
        this.identityDataFetcher =
                new JyskeIdentityDataFetcher(
                        apiClient, new JyskeBankPersistentStorage(persistentStorage));
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

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return new FetchIdentityDataResponse(identityDataFetcher.fetchIdentityData());
    }
}
