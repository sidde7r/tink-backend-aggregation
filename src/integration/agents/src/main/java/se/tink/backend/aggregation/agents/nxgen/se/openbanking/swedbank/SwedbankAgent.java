package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank;

import java.time.temporal.ChronoUnit;
import java.util.Optional;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankConstants.TimeValues;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.SwedbankAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.SwedbankAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.SwedbankPaymentAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.configuration.SwedbankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.SwedbankPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.SwedbankTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.SwedbankTransactionalAccountFetcher;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

/** This agent is not ready for production. Its for test and documentation of the flow. */
public final class SwedbankAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final SwedbankApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private SwedbankTransactionFetcher transactionFetcher;

    public SwedbankAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration agentsServiceConfiguration) {
        super(request, context, agentsServiceConfiguration.getSignatureKeyPair());

        apiClient =
                new SwedbankApiClient(
                        client,
                        persistentStorage,
                        agentsServiceConfiguration,
                        this.getEidasIdentity());

        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
        apiClient.setConfiguration(getAgentConfiguration());
        client.setFollowRedirects(false);
        client.setEidasProxy(agentsServiceConfiguration.getEidasProxy());
    }

    private AgentConfiguration<SwedbankConfiguration> getAgentConfiguration() {
        return getAgentConfigurationController()
                .getAgentCommonConfiguration(SwedbankConfiguration.class);
    }

    @Override
    protected Authenticator constructAuthenticator() {

        SwedbankAuthenticator authenticator =
                new SwedbankAuthenticator(apiClient, persistentStorage);

        SwedbankAuthenticationController swedbankAuthenticationController =
                new SwedbankAuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        authenticator,
                        credentials,
                        strongAuthenticationState,
                        transactionFetcher);

        return new AutoAuthenticationController(
                request,
                context,
                new ThirdPartyAppAuthenticationController<>(
                        swedbankAuthenticationController, supplementalInformationHelper),
                swedbankAuthenticationController);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
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
        SwedbankTransactionalAccountFetcher accountFetcher =
                new SwedbankTransactionalAccountFetcher(apiClient);

        this.transactionFetcher =
                new SwedbankTransactionFetcher(apiClient, supplementalInformationHelper);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(
                                transactionFetcher,
                                4,
                                TimeValues.MONTHS_TO_FETCH_MAX,
                                ChronoUnit.MONTHS)));
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        SwedbankPaymentAuthenticator paymentAuthenticator =
                new SwedbankPaymentAuthenticator(supplementalInformationHelper);
        SwedbankPaymentExecutor swedbankPaymentExecutor =
                new SwedbankPaymentExecutor(
                        apiClient, paymentAuthenticator, strongAuthenticationState);

        return Optional.of(new PaymentController(swedbankPaymentExecutor, swedbankPaymentExecutor));
    }
}
