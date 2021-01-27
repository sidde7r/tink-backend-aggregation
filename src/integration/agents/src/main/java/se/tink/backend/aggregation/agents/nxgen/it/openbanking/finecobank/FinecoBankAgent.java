package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import java.time.ZoneId;
import java.util.Optional;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.FinecoBankAuthenticationHelper;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.FinecoBankAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.configuration.FinecoBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.FinecoBankCreditCardAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.FinecoBankTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.executor.FinecoBankPaymentController;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.executor.FinecoBankPaymentExecutor;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionMonthPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS})
public final class FinecoBankAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshCreditCardAccountsExecutor {

    private final FinecoBankApiClient apiClient;
    private final AgentConfiguration<FinecoBankConfiguration> agentConfiguration;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private AutoAuthenticationController authenticationController;

    public FinecoBankAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration agentsServiceConfiguration) {
        super(request, context, agentsServiceConfiguration.getSignatureKeyPair());

        this.agentConfiguration =
                getAgentConfigurationController()
                        .getAgentConfiguration(FinecoBankConfiguration.class);

        super.setConfiguration(agentsServiceConfiguration);

        this.apiClient =
                new FinecoBankApiClient(
                        client,
                        persistentStorage,
                        this.agentConfiguration,
                        request.isManual(),
                        userIp);

        this.client.setEidasProxy(agentsServiceConfiguration.getEidasProxy());
        this.transactionalAccountRefreshController = getTransactionalAccountRefreshController();
        this.creditCardRefreshController = getCreditCardRefreshController();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return getAuthenticationController();
    }

    private AutoAuthenticationController getAuthenticationController() {
        if (authenticationController == null) {
            final FinecoBankAuthenticator finecoBankAuthenticator =
                    new FinecoBankAuthenticator(
                            supplementalInformationHelper,
                            persistentStorage,
                            new FinecoBankAuthenticationHelper(
                                    apiClient, persistentStorage, credentials),
                            strongAuthenticationState);

            authenticationController =
                    new AutoAuthenticationController(
                            request,
                            systemUpdater,
                            new ThirdPartyAppAuthenticationController<>(
                                    finecoBankAuthenticator, supplementalInformationHelper),
                            finecoBankAuthenticator);
        }
        return authenticationController;
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
        final FinecoBankTransactionalAccountFetcher accountFetcher =
                new FinecoBankTransactionalAccountFetcher(apiClient, persistentStorage);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(accountFetcher)
                                .setConsecutiveEmptyPagesLimit(1)
                                .build()));
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        FinecoBankPaymentExecutor executor =
                new FinecoBankPaymentExecutor(apiClient, sessionStorage);
        return Optional.of(
                new FinecoBankPaymentController(
                        executor,
                        executor,
                        supplementalInformationHelper,
                        sessionStorage,
                        strongAuthenticationState));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return creditCardRefreshController.fetchCreditCardTransactions();
    }

    private CreditCardRefreshController getCreditCardRefreshController() {
        final FinecoBankCreditCardAccountFetcher accountFetcher =
                new FinecoBankCreditCardAccountFetcher(
                        apiClient, persistentStorage, request.isManual());

        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionMonthPaginationController<>(
                                accountFetcher, ZoneId.of("GMT"))));
    }
}
