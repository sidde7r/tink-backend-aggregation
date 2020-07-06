package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.authenticator.CaisseEpargnePasswordAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.fetcher.identitydata.IdentityDataFetcher;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.Storage;

public class CaisseEpargneAgent extends NextGenerationAgent implements RefreshIdentityDataExecutor {
    private final CaisseEpargneApiClient apiClient;
    private final Storage instanceStorage;

    @Inject
    protected CaisseEpargneAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        instanceStorage = new Storage();
        apiClient = new CaisseEpargneApiClient(client, sessionStorage, instanceStorage);
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

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return new FetchIdentityDataResponse(
                new IdentityDataFetcher(instanceStorage).fetchIdentityData());
    }
}
