package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas;

import java.time.Clock;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.authenticator.BnpParibasAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.configuration.BnpParibasBankConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.configuration.BnpParibasConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.card.BnpParibasCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.BnpParibasIdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.BnpParibasTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.BnpParibasTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transfer.BnpTransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.payment.BnpParibasPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.utils.BnpParibasSignatureHeaderProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.FrOpenBankingPaymentExecutor;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public abstract class BnpParibasBaseAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshIdentityDataExecutor,
                RefreshTransferDestinationExecutor,
                RefreshCreditCardAccountsExecutor {

    private BnpParibasApiBaseClient apiClient;
    private AgentConfiguration<BnpParibasConfiguration> agentConfiguration;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private AutoAuthenticationController authenticator;
    private BnpParibasIdentityDataFetcher bnpParibasIdentityDataFetcher;
    private final TransferDestinationRefreshController transferDestinationRefreshController;
    private final BnpParibasPaymentApiClient paymentApiClient;

    public BnpParibasBaseAgent(
            AgentComponentProvider componentProvider,
            QsealcSigner qsealcSigner,
            BnpParibasBankConfig bankConfig) {
        super(componentProvider);

        agentConfiguration =
                getAgentConfigurationController()
                        .getAgentConfiguration(BnpParibasConfiguration.class);
        BnpParibasSignatureHeaderProvider bnpParibasSignatureHeaderProvider =
                new BnpParibasSignatureHeaderProvider(qsealcSigner);

        this.apiClient =
                new BnpParibasApiBaseClient(
                        client,
                        sessionStorage,
                        agentConfiguration,
                        bnpParibasSignatureHeaderProvider,
                        bankConfig);

        this.paymentApiClient =
                new BnpParibasPaymentApiClient(
                        client,
                        sessionStorage,
                        agentConfiguration,
                        bnpParibasSignatureHeaderProvider,
                        bankConfig);

        this.transactionalAccountRefreshController = getTransactionalAccountRefreshController();

        this.creditCardRefreshController =
                constructCardRefreshController(componentProvider.getLocalDateTimeSource());

        this.bnpParibasIdentityDataFetcher = new BnpParibasIdentityDataFetcher(this.apiClient);

        this.transferDestinationRefreshController =
                new TransferDestinationRefreshController(
                        metricRefreshController, new BnpTransferDestinationFetcher());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        if (authenticator == null) {
            authenticator = doConstructAuthenticator();
        }
        return authenticator;
    }

    private AutoAuthenticationController doConstructAuthenticator() {
        final OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new BnpParibasAuthenticator(apiClient, sessionStorage, agentConfiguration),
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
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        client.setEidasProxy(configuration.getEidasProxy());
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
        BnpParibasTransactionalAccountFetcher accountFetcher =
                new BnpParibasTransactionalAccountFetcher(apiClient);

        BnpParibasTransactionFetcher transactionFetcher =
                new BnpParibasTransactionFetcher(apiClient, Clock.system(ZoneId.of("CET")));

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(transactionFetcher)
                                .build()));
    }

    private CreditCardRefreshController constructCardRefreshController(
            LocalDateTimeSource localDateTimeSource) {
        BnpParibasCreditCardFetcher bnpParibasCreditCardFetcher =
                new BnpParibasCreditCardFetcher(apiClient, localDateTimeSource);
        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                bnpParibasCreditCardFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(
                                        bnpParibasCreditCardFetcher)
                                .build()));
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return bnpParibasIdentityDataFetcher.response();
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        FrOpenBankingPaymentExecutor paymentExecutor =
                new FrOpenBankingPaymentExecutor(
                        paymentApiClient,
                        agentConfiguration.getRedirectUrl(),
                        sessionStorage,
                        strongAuthenticationState,
                        supplementalInformationHelper);
        return Optional.of(new PaymentController(paymentExecutor, paymentExecutor));
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return transferDestinationRefreshController.fetchTransferDestinations(accounts);
    }
}
