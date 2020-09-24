package se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.authenticator.EdenredAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.fetcher.EdenredAccountsFetcher;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.fetcher.EdenredTransactionsFetcher;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.storage.EdenredStorage;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@AgentCapabilities({CHECKING_ACCOUNTS})
public final class EdenredAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor {

    private final EdenredStorage edenredStorage;

    private final EdenredApiClient edenredApiClient;

    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    @Inject
    protected EdenredAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);

        edenredStorage = new EdenredStorage(persistentStorage, sessionStorage);
        edenredApiClient = new EdenredApiClient(client, edenredStorage);

        transactionalAccountRefreshController =
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        createAccountFetcher(),
                        createTransactionFetcher());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(
                new EdenredAuthenticator(edenredStorage, edenredApiClient));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return transactionalAccountRefreshController.fetchCheckingAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return transactionalAccountRefreshController.fetchCheckingTransactions();
    }

    private AccountFetcher<TransactionalAccount> createAccountFetcher() {
        return new EdenredAccountsFetcher(edenredApiClient, edenredStorage);
    }

    private TransactionFetcher<TransactionalAccount> createTransactionFetcher() {
        return new EdenredTransactionsFetcher(edenredApiClient, edenredStorage);
    }
}
