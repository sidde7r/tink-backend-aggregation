package se.tink.backend.aggregation.agents.nxgen.se.business.nordea;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator.NordeaSEBankIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.rpc.filter.NordeaSEFilter;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.session.NordeaSESessionHandler;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class NordeaSEAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {
    private final NordeaSEApiClient apiClient;

    @Inject
    public NordeaSEAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        client.addFilter(new NordeaSEFilter());
        apiClient = new NordeaSEApiClient(client, sessionStorage);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new BankIdAuthenticationController<>(
                supplementalRequester,
                new NordeaSEBankIdAuthenticator(apiClient, sessionStorage),
                persistentStorage,
                credentials);
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return null;
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return null;
    }

    @Override
    public FetchAccountsResponse fetchSavingsAccounts() {
        return null;
    }

    @Override
    public FetchTransactionsResponse fetchSavingsTransactions() {
        return null;
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new NordeaSESessionHandler(sessionStorage);
    }
}
