package se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.authenticator.NordnetBankIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.session.NordnetSessionHandler;
import se.tink.backend.aggregation.constants.CommonHeaders;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class NordnetAgent extends NextGenerationAgent {

    private final SessionStorage sessionStorage;
    private final NordnetApiClient apiClient;

    @Inject
    public NordnetAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        this.sessionStorage = new SessionStorage();
        this.apiClient = new NordnetApiClient(client);
        client.setUserAgent(CommonHeaders.DEFAULT_USER_AGENT);
        client.setFollowRedirects(false);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new BankIdAuthenticationController<>(
                supplementalRequester,
                new NordnetBankIdAuthenticator(apiClient, sessionStorage),
                persistentStorage,
                credentials);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new NordnetSessionHandler(apiClient, persistentStorage);
    }
}
