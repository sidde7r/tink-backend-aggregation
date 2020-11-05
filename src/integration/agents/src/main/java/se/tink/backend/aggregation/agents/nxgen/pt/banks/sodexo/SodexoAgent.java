package se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.fetchers.SodexoAccountsFetcher;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.fetchers.SodexoTransactionsFetcher;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

@AgentCapabilities({CHECKING_ACCOUNTS})
public class SodexoAgent extends NextGenerationAgent implements RefreshCheckingAccountsExecutor {

    private SodexoApiClient sodexoApiClient;
    private TransactionalAccountRefreshController transactionalAccountRefreshController;
    private SodexoStorage sodexoStorage;

    @Inject
    public SodexoAgent(final AgentComponentProvider componentProvider) {
        super(componentProvider);

        this.sodexoStorage = new SodexoStorage(persistentStorage, sessionStorage);
        this.sodexoApiClient = new SodexoApiClient(client, sodexoStorage);

        transactionalAccountRefreshController =
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        new SodexoAccountsFetcher(sodexoApiClient, sodexoStorage),
                        new SodexoTransactionsFetcher(sodexoApiClient));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(
                new SodexoAuthenticator(sodexoApiClient, sodexoStorage));
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return transactionalAccountRefreshController.fetchCheckingAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return transactionalAccountRefreshController.fetchCheckingTransactions();
    }
}
