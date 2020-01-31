package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs;

import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.ProgressiveAuthAgent;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.SibsAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.configuration.SibsConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.SibsPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.sign.SignPaymentStrategy;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.sign.SignPaymentStrategyFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.filter.ConsentInvalidErrorFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.filter.RateLimitErrorFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.filter.ServiceInvalidErrorFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount.SibsTransactionalAccountAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount.SibsTransactionalAccountTransactionFetcher;
import se.tink.backend.aggregation.agents.utils.transfer.InferredTransferDestinations;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.agents.SubsequentProgressiveGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.SupplementalInformationProvider;
import se.tink.backend.aggregation.nxgen.agents.strategy.SubsequentGenerationAgentStrategyFactory;
import se.tink.backend.aggregation.nxgen.controllers.authentication.StatelessProgressiveAuthenticator;
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
import se.tink.libraries.credentials.service.CredentialsRequest;

public abstract class SibsProgressiveBaseAgent extends SubsequentProgressiveGenerationAgent
        implements RefreshTransferDestinationExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                ProgressiveAuthAgent {

    protected final SibsBaseApiClient apiClient;

    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final StatelessProgressiveAuthenticator authenticator;
    private final SibsUserState userState;

    public SibsProgressiveBaseAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration configuration) {
        super(
                SubsequentGenerationAgentStrategyFactory.nxgen(
                        request, context, configuration.getSignatureKeyPair()));
        userState = new SibsUserState(persistentStorage);
        setConfiguration(configuration);
        apiClient = new SibsBaseApiClient(client, userState, request.isManual());
        apiClient.setConfiguration(getClientConfiguration());
        client.setMessageSignInterceptor(
                new SibsMessageSignInterceptor(
                        getClientConfiguration(),
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
        client.addFilter(new BankServiceInternalErrorFilter());
        client.addFilter(new ServiceInvalidErrorFilter());
        client.addFilter(new ConsentInvalidErrorFilter());
        client.addFilter(new ServiceUnavailableBankServiceErrorFilter());
        client.addFilter(new RateLimitErrorFilter());
    }

    protected abstract String getIntegrationName();

    private SibsConfiguration getClientConfiguration() {
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
        final SibsTransactionalAccountAccountFetcher accountFetcher =
                new SibsTransactionalAccountAccountFetcher(apiClient);
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
        SupplementalInformationProvider supplementalInformationProvider =
                new SupplementalInformationProvider(request, supplementalRequester, credentials);
        SignPaymentStrategy signPaymentStrategy =
                SignPaymentStrategyFactory.buildSignPaymentRedirectStrategy(
                        apiClient,
                        supplementalInformationProvider.getSupplementalInformationHelper());
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
