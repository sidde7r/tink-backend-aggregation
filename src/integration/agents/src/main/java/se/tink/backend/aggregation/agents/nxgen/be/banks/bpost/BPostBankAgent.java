package se.tink.backend.aggregation.agents.nxgen.be.banks.bpost;

import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication.BPostBankAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.entity.BPostBankEntityManager;
import se.tink.backend.aggregation.nxgen.agents.SubsequentProgressiveGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.strategy.SubsequentGenerationAgentStrategy;
import se.tink.backend.aggregation.nxgen.controllers.authentication.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SteppableAuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SteppableAuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class BPostBankAgent extends SubsequentProgressiveGenerationAgent {

    private BPostBankEntityManager entityManager;
    private BPostBankAuthenticator authenticator;
    private BPostBankApiClient apiClient;

    public BPostBankAgent(SubsequentGenerationAgentStrategy strategy) {
        super(strategy);
        apiClient = new BPostBankApiClient(client);
    }

    @Override
    public SteppableAuthenticationResponse login(SteppableAuthenticationRequest request)
            throws Exception {
        SteppableAuthenticationResponse loginResponse = super.login(request);
        getEntityManager().save();
        return loginResponse;
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public StatelessProgressiveAuthenticator getAuthenticator() {
        if (authenticator == null) {
            authenticator =
                    new BPostBankAuthenticator(
                            apiClient, getEntityManager().getAuthenticationContext(), request);
        }
        return authenticator;
    }

    private BPostBankEntityManager getEntityManager() {
        if (entityManager == null) {
            entityManager = new BPostBankEntityManager(persistentStorage);
        }
        return entityManager;
    }
}
