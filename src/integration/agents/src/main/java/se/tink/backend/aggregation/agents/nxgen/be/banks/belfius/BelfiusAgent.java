package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.SAVINGS_ACCOUNTS;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import se.tink.agent.sdk.operation.http.ProxyProfiles;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentplatform.AgentPlatformHttpClient;
import se.tink.backend.aggregation.agents.agentplatform.authentication.AgentPlatformAgent;
import se.tink.backend.aggregation.agents.agentplatform.authentication.ObjectMapperFactory;
import se.tink.backend.aggregation.agents.agentplatform.authentication.storage.AgentPlatformStorageMigration;
import se.tink.backend.aggregation.agents.agentplatform.authentication.storage.AgentPlatformStorageMigrator;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.BefiusAuthenticationConfig;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.persistence.BelfiusAgentPlatformStorageMigrator;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.responsevalidator.AgentPlatformResponseValidator;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.BelfiusTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.sessionhandler.BelfiusSessionHandler;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.signature.BelfiusSignatureCreator;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcess;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.BadGatewayRetryFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.SslHandshakeRetryFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.TimeoutRetryFilter;

@Slf4j
@AgentCapabilities({SAVINGS_ACCOUNTS})
public final class BelfiusAgent extends AgentPlatformAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                AgentPlatformStorageMigration {

    private final ProxyProfiles proxyProfiles;
    private final BelfiusApiClient apiClient;
    private final AgentPlatformBelfiusApiClient agentPlatformApiClient;
    private final BelfiusSessionStorage belfiusSessionStorage;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final BelfiusSignatureCreator belfiusSignatureCreator;
    private BefiusAuthenticationConfig befiusAuthenticationConfig;
    private ObjectMapperFactory objectMapperFactory;

    @Inject
    public BelfiusAgent(AgentComponentProvider agentComponentProvider) {
        super(agentComponentProvider);
        this.proxyProfiles = agentComponentProvider.getProxyProfiles();
        this.belfiusSessionStorage = new BelfiusSessionStorage(this.sessionStorage);
        this.belfiusSignatureCreator = new BelfiusSignatureCreator();
        this.apiClient =
                new BelfiusApiClient(
                        this.client,
                        belfiusSessionStorage,
                        getBelfiusLocale(agentComponentProvider.getUser().getLocale()));
        this.agentPlatformApiClient =
                new AgentPlatformBelfiusApiClient(
                        new AgentPlatformHttpClient(this.client),
                        getBelfiusLocale(agentComponentProvider.getUser().getLocale()));
        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
        this.objectMapperFactory = new ObjectMapperFactory();
        befiusAuthenticationConfig =
                new BefiusAuthenticationConfig(
                        agentPlatformApiClient,
                        belfiusSessionStorage,
                        belfiusSignatureCreator,
                        objectMapperFactory.getInstance(),
                        AgentPlatformResponseValidator.getInstance());
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

    private void configureHttpClient(AgentsServiceConfiguration agentsServiceConfiguration) {
        client.addFilter(
                new TimeoutRetryFilter(
                        BelfiusConstants.HttpClient.MAX_RETRIES,
                        BelfiusConstants.HttpClient.RETRY_SLEEP_MILLISECONDS));
        client.addFilter(
                new BadGatewayRetryFilter(5, BelfiusConstants.HttpClient.RETRY_SLEEP_MILLISECONDS));
        client.addFilter(
                new SslHandshakeRetryFilter(
                        3, BelfiusConstants.HttpClient.RETRY_SLEEP_MILLISECONDS));

        if (agentsServiceConfiguration.isFeatureEnabled("beProxy")) {
            client.setProxyProfile(this.proxyProfiles.getMarketProxyProfile());
        } else {
            client.setProxyProfile(this.proxyProfiles.getAwsProxyProfile());
        }
        client.disableAggregatorHeader();
    }

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        BelfiusTransactionalAccountFetcher transactionalAccountFetcher =
                new BelfiusTransactionalAccountFetcher(this.apiClient, this.belfiusSessionStorage);
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                transactionalAccountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        transactionalAccountFetcher,
                        transactionalAccountFetcher));
    }

    private String getBelfiusLocale(String userLocale) {
        if (Strings.isNullOrEmpty(userLocale)) {
            return BelfiusConstants.Request.LOCALE_DUTCH;
        }
        if (userLocale.toLowerCase().contains(BelfiusConstants.TINK_FRENCH)) {
            return BelfiusConstants.Request.LOCALE_FRENCH;
        }
        return BelfiusConstants.Request.LOCALE_DUTCH;
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new BelfiusSessionHandler(this.apiClient);
    }

    @Override
    public AgentAuthenticationProcess getAuthenticationProcess() {
        return befiusAuthenticationConfig.belfiusAuthProcess();
    }

    @Override
    public boolean isBackgroundRefreshPossible() {
        return true;
    }

    @Override
    public AgentPlatformStorageMigrator getMigrator() {
        return new BelfiusAgentPlatformStorageMigrator(
                credentials, objectMapperFactory.getInstance());
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        configureHttpClient(configuration);
    }
}
