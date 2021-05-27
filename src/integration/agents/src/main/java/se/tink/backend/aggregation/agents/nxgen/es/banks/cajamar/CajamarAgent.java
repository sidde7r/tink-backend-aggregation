package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.INVESTMENTS;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.CajamarConstants.Proxy;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.CajamarConstants.TimeoutFilter;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.authenticator.CajamarAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.account.CajamarAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.account.CajamarAccountTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.creditcard.CajamarCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.creditcard.CajamarCreditCardTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.identitydata.CajamarIdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.investment.CajamarInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.session.CajamarSessionHandler;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.PasswordBasedProxyConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.TimeoutRetryFilter;

@Slf4j
@AgentCapabilities({CHECKING_ACCOUNTS, CREDIT_CARDS, INVESTMENTS, IDENTITY_DATA})
public class CajamarAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshInvestmentAccountsExecutor,
                RefreshIdentityDataExecutor {

    private final CajamarApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final InvestmentRefreshController investmentRefreshController;

    @Inject
    protected CajamarAgent(
            AgentComponentProvider agentComponentProvider,
            AgentsServiceConfiguration configuration) {
        super(agentComponentProvider);
        configureHttpClient(configuration);
        this.apiClient = new CajamarApiClient(this.client, sessionStorage);
        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
        this.creditCardRefreshController = constructCreditCardRefreshController();
        this.investmentRefreshController = constructInvestmentRefreshController();
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
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return creditCardRefreshController.fetchCreditCardTransactions();
    }

    @Override
    public FetchInvestmentAccountsResponse fetchInvestmentAccounts() {
        return investmentRefreshController.fetchInvestmentAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchInvestmentTransactions() {
        return investmentRefreshController.fetchInvestmentTransactions();
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        final IdentityDataFetcher fetcher =
                new CajamarIdentityDataFetcher(apiClient, sessionStorage);
        return new FetchIdentityDataResponse(fetcher.fetchIdentityData());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new CajamarAuthenticator(apiClient);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new CajamarSessionHandler(apiClient);
    }

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new CajamarAccountFetcher(apiClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(
                                new CajamarAccountTransactionFetcher(apiClient))));
    }

    private CreditCardRefreshController constructCreditCardRefreshController() {
        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                new CajamarCreditCardFetcher(apiClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(
                                new CajamarCreditCardTransactionFetcher(apiClient))));
    }

    private InvestmentRefreshController constructInvestmentRefreshController() {
        return new InvestmentRefreshController(
                metricRefreshController, updateController, new CajamarInvestmentFetcher(apiClient));
    }

    private void configureHttpClient(AgentsServiceConfiguration componentProvider) {
        this.client.addFilter(
                new TimeoutRetryFilter(
                        TimeoutFilter.NUM_TIMEOUT_RETRIES,
                        TimeoutFilter.TIMEOUT_RETRY_SLEEP_MILLISECONDS));

        if (componentProvider.isFeatureEnabled(Proxy.ES_PROXY)) {
            final PasswordBasedProxyConfiguration proxyConfiguration =
                    componentProvider.getCountryProxy(
                            Proxy.COUNTRY, credentials.getUserId().hashCode());
            log.info(
                    "Using proxy {} with username {}",
                    proxyConfiguration.getHost(),
                    proxyConfiguration.getUsername());
            this.client.setProductionProxy(
                    proxyConfiguration.getHost(),
                    proxyConfiguration.getUsername(),
                    proxyConfiguration.getPassword());
        }
    }
}
