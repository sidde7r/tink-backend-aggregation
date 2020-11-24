package se.tink.backend.aggregation.agents.nxgen.se.banks.minpension;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.nxgen.se.banks.minpension.authenticator.MinPensionAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.banks.minpension.session.MinPensionSessionHandler;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class MinPensionAgent extends NextGenerationAgent {
    private final MinPensionApiClient minPensionApiClient;

    @Inject
    public MinPensionAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        minPensionApiClient = new MinPensionApiClient(client, sessionStorage);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new BankIdAuthenticationController<>(
                context,
                new MinPensionAuthenticator(minPensionApiClient, sessionStorage),
                persistentStorage,
                credentials);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new MinPensionSessionHandler(minPensionApiClient);
    }
}
