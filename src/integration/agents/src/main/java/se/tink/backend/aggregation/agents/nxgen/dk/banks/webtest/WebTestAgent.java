package se.tink.backend.aggregation.agents.nxgen.dk.banks.webtest;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;

import com.google.inject.Inject;
import java.util.Collections;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorage;

// Zietek - Temporary agent buildt to test chromedriver, will be removed after tests.
@AgentCapabilities({CHECKING_ACCOUNTS})
public class WebTestAgent extends NextGenerationAgent implements RefreshCheckingAccountsExecutor {

    private final AgentTemporaryStorage agentStorage;

    @Inject
    public WebTestAgent(
            AgentComponentProvider componentProvider, AgentTemporaryStorage agentTemporaryStorage) {
        super(componentProvider);
        this.agentStorage = agentTemporaryStorage;
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return new FetchAccountsResponse(Collections.emptyList());
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return new FetchTransactionsResponse(null);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        WebTestAuthenticator webTestAuthenticator = new WebTestAuthenticator(agentStorage);
        return new AutoAuthenticationController(
                request, systemUpdater, webTestAuthenticator, webTestAuthenticator);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }
}
