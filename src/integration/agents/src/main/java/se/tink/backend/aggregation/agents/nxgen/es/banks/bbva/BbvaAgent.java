package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.INVESTMENTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.LOANS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.Defaults;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.Proxy;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator.BbvaAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.creditcard.BbvaCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.creditcard.BbvaCreditCardTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.identitydata.BbvaIdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.BbvaInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.loan.BbvaLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.BbvaAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.BbvaTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.session.BbvaSessionHandler;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.PasswordBasedProxyConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.TimeoutRetryFilter;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.libraries.credentials.service.CredentialsRequest;

@AgentCapabilities({
    CHECKING_ACCOUNTS,
    SAVINGS_ACCOUNTS,
    CREDIT_CARDS,
    INVESTMENTS,
    IDENTITY_DATA,
    LOANS
})
@Slf4j
public final class BbvaAgent extends NextGenerationAgent
        implements RefreshIdentityDataExecutor,
                RefreshInvestmentAccountsExecutor,
                RefreshLoanAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor {
    private final BbvaApiClient apiClient;
    private final InvestmentRefreshController investmentRefreshController;
    private final LoanRefreshController loanRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public BbvaAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration configuration) {
        super(request, context, configuration.getSignatureKeyPair());
        this.apiClient =
                new BbvaApiClient(
                        client, sessionStorage, supplementalInformationHelper, persistentStorage);
        configureHttpClient(client, configuration);

        this.investmentRefreshController =
                new InvestmentRefreshController(
                        metricRefreshController,
                        updateController,
                        new BbvaInvestmentFetcher(apiClient));

        this.loanRefreshController =
                new LoanRefreshController(
                        metricRefreshController, updateController, new BbvaLoanFetcher(apiClient));

        this.creditCardRefreshController = constructCreditCardRefreshController();

        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return transactionalAccountRefreshController.fetchCheckingAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        checkIfFirstLogin();
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
    public FetchLoanAccountsResponse fetchLoanAccounts() {
        return loanRefreshController.fetchLoanAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchLoanTransactions() {
        return loanRefreshController.fetchLoanTransactions();
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        final IdentityDataFetcher fetcher = new BbvaIdentityDataFetcher(apiClient);
        return new FetchIdentityDataResponse(fetcher.fetchIdentityData());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        Authenticator authenticator =
                new BbvaAuthenticator(apiClient, supplementalInformationHelper, request);
        log.info(
                "Credentials status after authenticating is equal {}",
                this.credentials.getStatus());
        return authenticator;
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new BbvaSessionHandler(apiClient);
    }

    protected void configureHttpClient(
            TinkHttpClient client, AgentsServiceConfiguration componentProvider) {
        client.addFilter(
                new TimeoutRetryFilter(
                        BbvaConstants.TimeoutFilter.NUM_TIMEOUT_RETRIES,
                        BbvaConstants.TimeoutFilter.TIMEOUT_RETRY_SLEEP_MILLISECONDS));

        if (componentProvider.isFeatureEnabled(Proxy.ES_PROXY)) {
            // Setting proxy for Spain via TPP
            final PasswordBasedProxyConfiguration proxyConfiguration =
                    componentProvider.getCountryProxy(
                            Proxy.COUNTRY, credentials.getUserId().hashCode());
            log.info(
                    "Using proxy {} with username {}",
                    proxyConfiguration.getHost(),
                    proxyConfiguration.getUsername());
            client.setProductionProxy(
                    proxyConfiguration.getHost(),
                    proxyConfiguration.getUsername(),
                    proxyConfiguration.getPassword());
        }
    }

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new BbvaAccountFetcher(apiClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(
                                new BbvaTransactionFetcher(apiClient))));
    }

    private CreditCardRefreshController constructCreditCardRefreshController() {
        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                new BbvaCreditCardFetcher(apiClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(
                                new BbvaCreditCardTransactionFetcher(apiClient))));
    }

    private void checkIfFirstLogin() {
        log.info("Credentials status is equal {}", this.credentials.getStatus());
        if (this.credentials.getStatus() == CredentialsStatus.CREATED) {
            log.info("Fetching transactions over 90 days");
            persistentStorage.put(Defaults.FIRST_LOGIN, true);
        } else {
            log.info("Fetching the last 90 days of transactions");
            persistentStorage.put(Defaults.FIRST_LOGIN, false);
        }
    }
}
