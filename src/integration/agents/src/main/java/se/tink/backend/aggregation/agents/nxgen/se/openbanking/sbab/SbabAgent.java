package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.authenticator.SbabAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.configuration.SbabConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.SbabPaymentController;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.SbabPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.fetcher.transactionalaccount.SbabTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.fetcher.transactionalaccount.SbabTransactionalAccountTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.fliter.SbabBadGatewayFilter;
import se.tink.backend.aggregation.agents.utils.transfer.InferredTransferDestinations;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.filter.BankServiceInternalErrorFilter;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class SbabAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshTransferDestinationExecutor {

    private final String clientName;
    private final SbabApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public SbabAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        apiClient = new SbabApiClient(client, persistentStorage);
        clientName = request.getProvider().getPayload();

        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        SbabConfiguration sbabConfiguration = getClientConfiguration();
        apiClient.setConfiguration(sbabConfiguration);

        this.client.setEidasProxy(configuration.getEidasProxy());
        this.client.addFilter(new BankServiceInternalErrorFilter());
        this.client.addFilter(new SbabBadGatewayFilter());
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        SbabPaymentExecutor sbabPaymentExecutor =
                new SbabPaymentExecutor(apiClient, getClientConfiguration(), sessionStorage);

        return Optional.of(
                new SbabPaymentController(
                        sbabPaymentExecutor,
                        sbabPaymentExecutor,
                        supplementalInformationHelper,
                        sessionStorage));
    }

    protected SbabConfiguration getClientConfiguration() {
        return getAgentConfigurationController().getAgentConfiguration(SbabConfiguration.class);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        SbabAuthenticator sbabAuthenticator = new SbabAuthenticator(apiClient, persistentStorage);
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
                                transactionFetcher, 4, 6, ChronoUnit.MONTHS)));
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
