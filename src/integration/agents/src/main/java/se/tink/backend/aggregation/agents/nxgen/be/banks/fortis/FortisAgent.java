package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import org.apache.http.NoHttpResponseException;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentplatform.AgentPlatformHttpClient;
import se.tink.backend.aggregation.agents.agentplatform.authentication.AgentPlatformAgent;
import se.tink.backend.aggregation.agents.agentplatform.authentication.AgentPlatformAuthenticator;
import se.tink.backend.aggregation.agents.agentplatform.authentication.ObjectMapperFactory;
import se.tink.backend.aggregation.agents.agentplatform.authentication.storage.AgentPlatformStorageMigration;
import se.tink.backend.aggregation.agents.agentplatform.authentication.storage.AgentPlatformStorageMigrator;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.FortisAuthenticationConfig;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisAgentPlatformStorageMigrator;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.FortisTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.helper.FortisRandomTokenGenerator;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.helper.HtmlReader;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcess;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.TimeoutRetryFilter;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS})
public final class FortisAgent extends AgentPlatformAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                AgentPlatformAuthenticator,
                AgentPlatformStorageMigration {

    private final FortisApiClient apiClient;

    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    private final FortisAuthenticationConfig fortisAuthenticationConfig;

    private final ObjectMapperFactory objectMapperFactory;

    @Inject
    public FortisAgent(AgentComponentProvider agentComponentProvider) {
        super(agentComponentProvider);
        configureHttpClient(client);

        String[] payload = request.getProvider().getPayload().split(" ");
        String baseUrl = payload[0];
        String distributorId = payload[1];

        FortisRandomTokenGenerator fortisRandomTokenGenerator = new FortisRandomTokenGenerator();

        client.addFilter(
                new TimeoutRetryFilter(
                        FortisConstants.HttpClient.MAX_RETRIES,
                        FortisConstants.HttpClient.RETRY_SLEEP_MILLISECONDS,
                        NoHttpResponseException.class));

        this.apiClient =
                new FortisApiClient(client, baseUrl, distributorId, fortisRandomTokenGenerator);

        AgentPlatformFortisApiClient agentPlatformFortisApiClient =
                new AgentPlatformFortisApiClient(
                        new AgentPlatformHttpClient(client),
                        fortisRandomTokenGenerator,
                        baseUrl,
                        distributorId);

        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();

        this.objectMapperFactory = new ObjectMapperFactory();

        this.fortisAuthenticationConfig =
                new FortisAuthenticationConfig(
                        agentPlatformFortisApiClient,
                        fortisRandomTokenGenerator,
                        objectMapperFactory.getInstance());
    }

    protected void configureHttpClient(TinkHttpClient client) {
        client.addMessageReader(new HtmlReader());
        client.disableSignatureRequestHeader();
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
        FortisTransactionalAccountFetcher accountFetcher =
                new FortisTransactionalAccountFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionPagePaginationController<>(accountFetcher, 1),
                        accountFetcher));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public AgentAuthenticationProcess getAuthenticationProcess() {
        return fortisAuthenticationConfig.createAuthProcess();
    }

    @Override
    public boolean isBackgroundRefreshPossible() {
        return true;
    }

    @Override
    public AgentPlatformStorageMigrator getMigrator() {
        return new FortisAgentPlatformStorageMigrator(objectMapperFactory.getInstance());
    }
}
