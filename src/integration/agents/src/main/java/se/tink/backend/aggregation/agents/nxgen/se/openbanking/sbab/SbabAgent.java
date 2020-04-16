package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SbabConstants.HttpClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SbabConstants.TransactionFetching;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.authenticator.SbabAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.configuration.SbabConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.SbabPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.fetcher.transactionalaccount.SbabTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.fetcher.transactionalaccount.SbabTransactionalAccountTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.fliter.SbabBadGatewayFilter;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.fliter.SbabRetryFilter;
import se.tink.backend.aggregation.agents.utils.transfer.InferredTransferDestinations;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.BankServiceInternalErrorFilter;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class SbabAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshTransferDestinationExecutor {

    private final SbabApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public SbabAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration agentsServiceConfiguration) {
        super(request, context, agentsServiceConfiguration.getSignatureKeyPair());

        apiClient = new SbabApiClient(client, persistentStorage);
        transactionalAccountRefreshController = getTransactionalAccountRefreshController();

        SbabConfiguration sbabConfiguration = getClientConfiguration();
        apiClient.setConfiguration(sbabConfiguration);

        this.client.setEidasProxy(agentsServiceConfiguration.getEidasProxy());
        configureHttpClient(this.client);
    }

    private void configureHttpClient(TinkHttpClient client) {

        client.addFilter(
                new SbabRetryFilter(HttpClient.MAX_RETRIES, HttpClient.RETRY_SLEEP_MILLISECONDS));
        this.client.addFilter(new BankServiceInternalErrorFilter());
        this.client.addFilter(new SbabBadGatewayFilter());
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        SbabPaymentExecutor sbabPaymentExecutor =
                new SbabPaymentExecutor(apiClient, supplementalRequester);

        return Optional.of(new PaymentController(sbabPaymentExecutor, sbabPaymentExecutor));
    }

    protected SbabConfiguration getClientConfiguration() {
        return getAgentConfigurationController().getAgentConfiguration(SbabConfiguration.class);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        SbabAuthenticator sbabAuthenticator = new SbabAuthenticator(apiClient);
        BankIdAuthenticationController bankIdAuthenticationController =
                new BankIdAuthenticationController<>(
                        supplementalRequester, sbabAuthenticator, persistentStorage, credentials);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                bankIdAuthenticationController,
                bankIdAuthenticationController);
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

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        final SbabTransactionalAccountFetcher accountFetcher =
                new SbabTransactionalAccountFetcher(apiClient);

        final SbabTransactionalAccountTransactionFetcher transactionFetcher =
                new SbabTransactionalAccountTransactionFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(
                                transactionFetcher,
                                TransactionFetching.MAX_CONSECUTIVE_EMPTY_PAGES,
                                TransactionFetching.DAYS_TO_FETCH,
                                ChronoUnit.DAYS)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return InferredTransferDestinations.forPaymentAccounts(accounts, AccountIdentifier.Type.SE);
    }
}
