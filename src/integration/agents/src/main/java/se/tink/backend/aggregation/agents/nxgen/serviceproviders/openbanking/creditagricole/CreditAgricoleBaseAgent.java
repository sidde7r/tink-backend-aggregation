package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole;

import java.time.Clock;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshBeneficiariesExecutor;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.authenticator.CreditAgricoleBaseAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.configuration.CreditAgricoleBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.configuration.CreditAgricoleBranchConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.payment.CreditAgricolePaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.CreditAgricoleBaseIdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.CreditAgricoleBaseTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.apiclient.CreditAgricoleBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.apiclient.CreditAgricoleStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.transfer.CreditAgricoleTransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.FrOpenBankingPaymentExecutor;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class CreditAgricoleBaseAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshIdentityDataExecutor,
                RefreshBeneficiariesExecutor {

    private final CreditAgricoleBaseApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final AgentConfiguration<CreditAgricoleBaseConfiguration> agentConfiguration;
    private final CreditAgricoleBaseConfiguration creditAgricoleConfiguration;
    private final TransferDestinationRefreshController transferDestinationRefreshController;
    private final CreditAgricoleBranchConfiguration branchConfiguration;

    public CreditAgricoleBaseAgent(
            AgentComponentProvider componentProvider, QsealcSigner qsealcSigner) {
        super(componentProvider);
        final String[] payload =
                request.getProvider().getPayload().split("\\s+"); // one or more whitespace
        this.branchConfiguration = new CreditAgricoleBranchConfiguration(payload[0], payload[1]);
        final CreditAgricoleStorage creditAgricoleStorage =
                new CreditAgricoleStorage(this.persistentStorage);

        this.agentConfiguration = getAgentConfiguration();
        this.creditAgricoleConfiguration = agentConfiguration.getProviderSpecificConfiguration();

        this.apiClient =
                new CreditAgricoleBaseApiClient(
                        this.client,
                        creditAgricoleStorage,
                        this.agentConfiguration,
                        branchConfiguration);

        final CreditAgricoleBaseMessageSignInterceptor creditAgricoleBaseMessageSignInterceptor =
                new CreditAgricoleBaseMessageSignInterceptor(this.agentConfiguration, qsealcSigner);
        this.client.setMessageSignInterceptor(creditAgricoleBaseMessageSignInterceptor);

        this.transactionalAccountRefreshController = getTransactionalAccountRefreshController();
        this.transferDestinationRefreshController = constructTransferDestinationRefreshController();
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        this.client.setEidasProxy(configuration.getEidasProxy());
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
    public FetchIdentityDataResponse fetchIdentityData() {
        final CreditAgricoleBaseIdentityDataFetcher creditAgricoleBaseIdentityDataFetcher =
                new CreditAgricoleBaseIdentityDataFetcher(apiClient);
        return creditAgricoleBaseIdentityDataFetcher.response();
    }

    @Override
    public FetchTransferDestinationsResponse fetchBeneficiaries(List<Account> accounts) {
        return transferDestinationRefreshController.fetchTransferDestinations(accounts);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new CreditAgricoleBaseAuthenticator(
                                apiClient,
                                persistentStorage,
                                agentConfiguration,
                                branchConfiguration),
                        credentials,
                        strongAuthenticationState);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        controller, supplementalInformationHelper),
                controller);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    private AgentConfiguration<CreditAgricoleBaseConfiguration> getAgentConfiguration() {
        return getAgentConfigurationController()
                .getAgentConfiguration(CreditAgricoleBaseConfiguration.class);
    }

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        final CreditAgricoleBaseTransactionalAccountFetcher accountFetcher =
                new CreditAgricoleBaseTransactionalAccountFetcher(
                        apiClient, persistentStorage, Clock.system(ZoneId.of("CET")));

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(accountFetcher).build()));
    }

    private TransferDestinationRefreshController constructTransferDestinationRefreshController() {
        return new TransferDestinationRefreshController(
                metricRefreshController, new CreditAgricoleTransferDestinationFetcher(apiClient));
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {

        FrOpenBankingPaymentExecutor paymentExecutor =
                new FrOpenBankingPaymentExecutor(
                        new CreditAgricolePaymentApiClient(
                                client,
                                sessionStorage,
                                creditAgricoleConfiguration,
                                branchConfiguration),
                        agentConfiguration.getRedirectUrl(),
                        sessionStorage,
                        strongAuthenticationState,
                        supplementalInformationHelper);

        return Optional.of(new PaymentController(paymentExecutor, paymentExecutor));
    }
}
