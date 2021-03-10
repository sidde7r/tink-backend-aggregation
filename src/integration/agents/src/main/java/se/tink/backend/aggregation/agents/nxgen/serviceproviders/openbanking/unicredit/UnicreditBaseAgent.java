package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit;

import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.UnicreditAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.UnicreditAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.configuration.UnicreditProviderConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.UnicreditPaymentController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.UnicreditPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.UnicreditTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.UnicreditTransactionalAccountTransactionFetcher;
import se.tink.backend.aggregation.agents.utils.transfer.InferredTransferDestinations;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.account.AccountIdentifier;

public abstract class UnicreditBaseAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshTransferDestinationExecutor {

    protected final UnicreditBaseApiClient apiClient;
    protected final UnicreditStorage unicreditStorage;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public UnicreditBaseAgent(
            AgentComponentProvider componentProvider,
            UnicreditProviderConfiguration providerConfiguration) {
        super(componentProvider);

        UnicreditBaseHeaderValues headerValues = setupHeaderValues(componentProvider);
        unicreditStorage = new UnicreditStorage(getPersistentStorage());
        apiClient = getApiClient(providerConfiguration, headerValues);
        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    private UnicreditBaseHeaderValues setupHeaderValues(AgentComponentProvider componentProvider) {
        String redirectUrl =
                getAgentConfigurationController()
                        .getAgentConfiguration(UnicreditBaseConfiguration.class)
                        .getRedirectUrl();

        return new UnicreditBaseHeaderValues(
                redirectUrl, componentProvider.getCredentialsRequest().isManual() ? userIp : null);
    }

    protected UnicreditBaseApiClient getApiClient(
            UnicreditProviderConfiguration providerConfiguration,
            UnicreditBaseHeaderValues headerValues) {
        return new UnicreditBaseApiClient(
                client, unicreditStorage, providerConfiguration, headerValues);
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        client.setEidasProxy(configuration.getEidasProxy());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final UnicreditAuthenticationController controller =
                new UnicreditAuthenticationController(
                        supplementalInformationHelper,
                        new UnicreditAuthenticator(unicreditStorage, apiClient, credentials),
                        strongAuthenticationState);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        controller, supplementalInformationHelper),
                controller);
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

        final UnicreditTransactionalAccountFetcher accountFetcher =
                new UnicreditTransactionalAccountFetcher(apiClient);
        final UnicreditTransactionalAccountTransactionFetcher transactionFetcher =
                new UnicreditTransactionalAccountTransactionFetcher(
                        apiClient, transactionPaginationHelper);

        return new TransactionalAccountRefreshController(
                metricRefreshController, updateController, accountFetcher, transactionFetcher);
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        return Optional.of(
                new UnicreditPaymentController(
                        new UnicreditPaymentExecutor(apiClient),
                        supplementalInformationHelper,
                        unicreditStorage,
                        strongAuthenticationState));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return InferredTransferDestinations.forPaymentAccounts(
                accounts, AccountIdentifier.Type.IBAN);
    }
}
