package se.tink.backend.aggregation.agents.nxgen.be.banks.ing;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants.Headers;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.IngAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.crypto.IngCryptoUtils;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.IngTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.IngTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.helper.IngRequestFactory;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.helper.ProxyFilter;
import se.tink.backend.aggregation.agents.progressive.ProgressiveAuthAgent;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.SubsequentProgressiveGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.MultiIpGateway;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TimeoutFilter;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS})
public final class IngAgent extends SubsequentProgressiveGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                ProgressiveAuthAgent {

    private final IngDirectApiClient ingDirectApiClient;
    private final IngProxyApiClient ingProxyApiClient;
    private final StatelessProgressiveAuthenticator authenticator;
    private final IngStorage ingStorage;
    private final IngCryptoUtils ingCryptoUtils;
    private final IngRequestFactory ingRequestFactory;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    @Inject
    public IngAgent(
            final AgentComponentProvider componentProvider,
            AgentsServiceConfiguration agentsServiceConfiguration) {
        super(componentProvider);
        configureHttpClient(client, agentsServiceConfiguration);
        this.ingCryptoUtils = new IngCryptoUtils();
        this.ingStorage = new IngStorage(persistentStorage, sessionStorage, ingCryptoUtils);
        this.ingDirectApiClient = new IngDirectApiClient(client);
        ProxyFilter proxyFilter = new ProxyFilter(ingStorage, ingCryptoUtils);
        this.ingProxyApiClient = new IngProxyApiClient(client, proxyFilter, ingStorage);
        this.ingRequestFactory = new IngRequestFactory(ingStorage);

        IngComponents ingComponents =
                new IngComponents(
                        ingProxyApiClient,
                        ingDirectApiClient,
                        ingStorage,
                        ingCryptoUtils,
                        ingRequestFactory);

        authenticator = new IngAuthenticator(ingComponents, supplementalInformationFormer);

        transactionalAccountRefreshController =
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        new IngTransactionalAccountFetcher(ingProxyApiClient),
                        new TransactionFetcherController<>(
                                this.transactionPaginationHelper,
                                new TransactionKeyPaginationController<>(
                                        new IngTransactionFetcher(ingProxyApiClient))));
    }

    protected void configureHttpClient(
            TinkHttpClient client, AgentsServiceConfiguration agentsServiceConfiguration) {
        client.setUserAgent(Headers.USER_AGENT_VALUE);
        client.setFollowRedirects(false);
        client.addFilter(new TimeoutFilter());

        final MultiIpGateway gateway =
                new MultiIpGateway(client, credentials.getUserId(), credentials.getId());
        gateway.setMultiIpGateway(agentsServiceConfiguration.getIntegrations());
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

    @Override
    protected SessionHandler constructSessionHandler() {
        return new IngSessionHandler(ingProxyApiClient);
    }

    @Override
    public StatelessProgressiveAuthenticator getAuthenticator() {
        return authenticator;
    }
}
