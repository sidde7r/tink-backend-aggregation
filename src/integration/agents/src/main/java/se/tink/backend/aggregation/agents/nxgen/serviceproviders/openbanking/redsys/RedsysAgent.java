package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.consent.ConsentGenerator;
import se.tink.backend.aggregation.agents.consent.generators.serviceproviders.redsys.rpc.ConsentRequestBody;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.authenticator.RedsysAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.authenticator.RedsysAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.configuration.AspspConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.configuration.RedsysConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.ConsentController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.RedsysConsentController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.RedsysConsentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.RedsysTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.RedsysUpcomingTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.rpc.BaseTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.payments.RedsysPaymentExecutor;
import se.tink.backend.aggregation.agents.utils.transfer.InferredTransferDestinations;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.enums.AccountIdentifierType;

public abstract class RedsysAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshTransferDestinationExecutor,
                AspspConfiguration {

    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final ConsentController consentController;
    protected final RedsysApiClient apiClient;
    protected final RedsysConsentStorage consentStorage;
    protected final AgentComponentProvider componentProvider;
    protected final ConsentGenerator<ConsentRequestBody> consentGenerator;
    private final RedsysSignedRequestFactory signedRequestFactory;

    public RedsysAgent(
            AgentComponentProvider componentProvider,
            ConsentGenerator<ConsentRequestBody> consentGenerator) {
        super(componentProvider);
        this.consentGenerator = consentGenerator;
        this.componentProvider = componentProvider;
        this.signedRequestFactory =
                new RedsysSignedRequestFactory(
                        client, sessionStorage, this.getEidasIdentity(), componentProvider);
        this.apiClient =
                new RedsysApiClient(
                        sessionStorage,
                        persistentStorage,
                        this,
                        componentProvider,
                        signedRequestFactory);
        this.consentStorage = new RedsysConsentStorage(persistentStorage);
        this.consentController = getConsentController();
        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
        client.setResponseStatusHandler(new RedsysHttpResponseStatusHandler());
    }

    protected ConsentController getConsentController() {
        return new RedsysConsentController(
                apiClient,
                consentStorage,
                supplementalInformationHelper,
                strongAuthenticationState,
                consentGenerator);
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return InferredTransferDestinations.forPaymentAccounts(
                accounts, AccountIdentifierType.IBAN);
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        final AgentConfiguration<RedsysConfiguration> agentConfiguration =
                getAgentConfigurationController().getAgentConfiguration(RedsysConfiguration.class);
        signedRequestFactory.setConfiguration(agentConfiguration, configuration.getEidasProxy());
        apiClient.setConfiguration(agentConfiguration, configuration.getEidasProxy());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final RedsysAuthenticationController redsysAuthenticationController =
                new RedsysAuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new RedsysAuthenticator(apiClient, sessionStorage),
                        consentController,
                        credentials,
                        strongAuthenticationState);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        redsysAuthenticationController, supplementalInformationHelper),
                redsysAuthenticationController);
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
        final RedsysTransactionalAccountFetcher accountFetcher =
                new RedsysTransactionalAccountFetcher(apiClient, consentController, this);

        final TransactionPaginator<TransactionalAccount> paginator =
                new TransactionKeyPaginationController<>(accountFetcher);

        final TransactionFetcherController<TransactionalAccount> controller;
        if (supportsPendingTransactions()) {
            final RedsysUpcomingTransactionFetcher upcomingTransactionFetcher =
                    new RedsysUpcomingTransactionFetcher(apiClient, consentController);
            controller =
                    new TransactionFetcherController<>(
                            transactionPaginationHelper, paginator, upcomingTransactionFetcher);
        } else {
            controller = new TransactionFetcherController<>(transactionPaginationHelper, paginator);
        }

        return new TransactionalAccountRefreshController(
                metricRefreshController, updateController, accountFetcher, controller);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public LocalDate oldestTransactionDate() {
        return LocalDate.now().minusYears(2);
    }

    @Override
    public Class<? extends BaseTransactionsResponse> getTransactionsResponseClass() {
        return TransactionsResponse.class;
    }

    @Override
    public boolean shouldReturnLowercaseAccountId() {
        return false;
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        RedsysPaymentExecutor paymentExecutor =
                new RedsysPaymentExecutor(
                        apiClient,
                        consentController,
                        sessionStorage,
                        supplementalInformationHelper,
                        strongAuthenticationState);
        return Optional.of(new PaymentController(paymentExecutor, paymentExecutor));
    }
}
