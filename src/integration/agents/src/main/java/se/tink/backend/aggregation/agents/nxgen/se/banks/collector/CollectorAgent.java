package se.tink.backend.aggregation.agents.nxgen.se.banks.collector;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.nxgen.se.banks.collector.authenticator.bankid.CollectorBankIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.banks.collector.session.CollectorSessionHandler;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class CollectorAgent extends NextGenerationAgent {
    private final CollectorApiClient apiClient;

    @Inject
    public CollectorAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        apiClient = new CollectorApiClient(client, sessionStorage);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new BankIdAuthenticationController<>(
                supplementalRequester,
                new CollectorBankIdAuthenticator(apiClient, sessionStorage),
                persistentStorage,
                credentials);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new CollectorSessionHandler(sessionStorage);
    }
}
