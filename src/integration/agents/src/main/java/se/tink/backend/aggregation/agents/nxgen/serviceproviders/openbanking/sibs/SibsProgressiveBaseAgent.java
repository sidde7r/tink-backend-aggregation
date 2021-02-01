package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs;

import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.SibsAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.configuration.SibsConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.SibsPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.sign.SignPaymentStrategy;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.sign.SignPaymentStrategyFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.fetcher.transactionalaccount.SibsTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.fetcher.transactionalaccount.SibsTransactionalAccountTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.filter.ConsentInvalidErrorFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.filter.RateLimitErrorFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.filter.ServiceInvalidErrorFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.filter.SibsRetryFilter;
import se.tink.backend.aggregation.agents.progressive.ProgressiveAuthAgent;
import se.tink.backend.aggregation.agents.utils.transfer.InferredTransferDestinations;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.agents.SubsequentProgressiveGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.BankServiceInternalErrorFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.ExecutionTimeLoggingFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.ServiceUnavailableBankServiceErrorFilter;
import se.tink.libraries.account.AccountIdentifier;

public abstract class SibsProgressiveBaseAgent extends SubsequentProgressiveGenerationAgent
        implements RefreshTransferDestinationExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                ProgressiveAuthAgent {

    protected final SibsBaseApiClient apiClient;
    protected final SibsUserState userState;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final StatelessProgressiveAuthenticator authenticator;

    public SibsProgressiveBaseAgent(
            AgentComponentProvider agentComponentProvider,
            AgentsServiceConfiguration agentsServiceConfiguration) {
        super(agentComponentProvider);
        userState = new SibsUserState(persistentStorage);
        setConfiguration(agentsServiceConfiguration);
        apiClient =
                new SibsBaseApiClient(
                        client, userState, request.getProvider().getPayload(), request.isManual());
        final AgentConfiguration<SibsConfiguration> agentConfiguration = getAgentConfiguration();
        final SibsConfiguration sibsConfiguration =
                agentConfiguration.getProviderSpecificConfiguration();
        apiClient.setConfiguration(agentConfiguration);
        client.setMessageSignInterceptor(
                new SibsMessageSignInterceptor(
                        sibsConfiguration,
                        configuration.getEidasProxy(),
                        new EidasIdentity(
                                context.getClusterId(), context.getAppId(), this.getAgentClass())));
        applyFilters(client);

        client.setEidasProxy(configuration.getEidasProxy());
        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
        authenticator =
                new SibsAuthenticator(apiClient, userState, credentials, strongAuthenticationState);
    }

    private void applyFilters(TinkHttpClient client) {
        client.addFilter(new ExecutionTimeLoggingFilter());
        client.addFilter(new SibsRetryFilter());
        client.addFilter(new BankServiceInternalErrorFilter());
        client.addFilter(new ServiceInvalidErrorFilter());
        client.addFilter(new ConsentInvalidErrorFilter());
        client.addFilter(new ServiceUnavailableBankServiceErrorFilter());
        client.addFilter(new RateLimitErrorFilter());
    }

    private AgentConfiguration<SibsConfiguration> getAgentConfiguration() {
        return getAgentConfigurationController().getAgentConfiguration(SibsConfiguration.class);
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
        final SibsTransactionalAccountFetcher accountFetcher =
                new SibsTransactionalAccountFetcher(apiClient);
        final SibsTransactionalAccountTransactionFetcher transactionFetcher =
                new SibsTransactionalAccountTransactionFetcher(apiClient, request, userState);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(transactionFetcher)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        SignPaymentStrategy signPaymentStrategy =
                SignPaymentStrategyFactory.buildSignPaymentRedirectStrategy(
                        apiClient, supplementalInformationHelper);
        SibsPaymentExecutor sibsPaymentExecutor =
                new SibsPaymentExecutor(apiClient, signPaymentStrategy, strongAuthenticationState);
        return Optional.of(new PaymentController(sibsPaymentExecutor, sibsPaymentExecutor));
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return InferredTransferDestinations.forPaymentAccounts(
                accounts, AccountIdentifier.Type.SEPA_EUR, AccountIdentifier.Type.IBAN);
    }

    @Override
    public StatelessProgressiveAuthenticator getAuthenticator() {
        return authenticator;
    }
}
