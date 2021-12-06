package se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.authenticator.LclAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.fetcher.transactionalaccounts.LclTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.fetcher.transactionalaccounts.LclTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.session.LclSessionHandler;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.storage.LclPersistentStorage;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS})
public final class LclAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final LclApiClient apiClient;

    private final LclPersistentStorage lclPersistentStorage;

    private final RandomValueGenerator randomValueGenerator;

    private final LocalDateTimeSource localDateTimeSource;

    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    @Inject
    public LclAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);

        configureHttpClient(client);

        this.randomValueGenerator = componentProvider.getRandomValueGenerator();

        this.localDateTimeSource = componentProvider.getLocalDateTimeSource();

        this.lclPersistentStorage = new LclPersistentStorage(persistentStorage);

        this.apiClient = new LclApiClient(client, lclPersistentStorage, localDateTimeSource);

        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
    }

    protected void configureHttpClient(TinkHttpClient client) {
        // We have to disable the Tink signature header, otherwise LCL will reject our requests.
        client.disableSignatureRequestHeader();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(
                new LclAuthenticator(
                        apiClient,
                        lclPersistentStorage,
                        randomValueGenerator,
                        localDateTimeSource));
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return transactionalAccountRefreshController.fetchCheckingAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return transactionalAccountRefreshController.fetchCheckingTransactions();
    }

    @Override
    public FetchAccountsResponse fetchSavingsAccounts() {
        return transactionalAccountRefreshController.fetchSavingsAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchSavingsTransactions() {
        return transactionalAccountRefreshController.fetchSavingsTransactions();
    }

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new LclTransactionalAccountFetcher(apiClient),
                new LclTransactionFetcher(apiClient));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new LclSessionHandler(apiClient);
    }
}
