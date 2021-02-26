package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import java.time.ZoneId;
import java.util.Optional;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.FinecoBankAuthenticationHelper;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.FinecoBankAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.configuration.FinecoBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.card.FinecoBankCreditCardAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.FinecoBankTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.FinecoBankPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.FinecoPaymentFetcher;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
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

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS})
public final class FinecoBankAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshCreditCardAccountsExecutor {

    private final FinecoBankApiClient apiClient;
    private final TransactionalAccountRefreshController accountRefreshController;
    private final CreditCardRefreshController cardRefreshController;

    private final LocalDateTimeSource localDateTimeSource;

    @Inject
    public FinecoBankAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        this.localDateTimeSource = componentProvider.getLocalDateTimeSource();

        this.apiClient = constructApiClient(componentProvider);
        this.accountRefreshController = constructAccountRefreshController();
        this.cardRefreshController = constructCardRefreshController();
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        client.setEidasProxy(configuration.getEidasProxy());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        FinecoBankAuthenticator finecoBankAuthenticator =
                new FinecoBankAuthenticator(
                        supplementalInformationHelper,
                        persistentStorage,
                        new FinecoBankAuthenticationHelper(
                                apiClient, persistentStorage, credentials, localDateTimeSource),
                        strongAuthenticationState);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        finecoBankAuthenticator, supplementalInformationHelper),
                finecoBankAuthenticator);
    }

    private FinecoBankApiClient constructApiClient(AgentComponentProvider componentProvider) {
        FinecoHeaderValues headerValues =
                new FinecoHeaderValues(
                        getAgentConfigurationController()
                                .getAgentConfiguration(FinecoBankConfiguration.class)
                                .getRedirectUrl(),
                        request.isManual() ? userIp : null);

        return new FinecoBankApiClient(
                client,
                persistentStorage,
                headerValues,
                componentProvider.getRandomValueGenerator());
    }

    private TransactionalAccountRefreshController constructAccountRefreshController() {
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

    private CreditCardRefreshController constructCardRefreshController() {
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

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return accountRefreshController.fetchCheckingAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return accountRefreshController.fetchCheckingTransactions();
    }

    @Override
    public FetchAccountsResponse fetchSavingsAccounts() {
        return accountRefreshController.fetchSavingsAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchSavingsTransactions() {
        return accountRefreshController.fetchSavingsTransactions();
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        FinecoBankPaymentExecutor paymentExecutor =
                new FinecoBankPaymentExecutor(
                        apiClient,
                        sessionStorage,
                        strongAuthenticationState,
                        supplementalInformationController);

        FinecoPaymentFetcher paymentFetcher = new FinecoPaymentFetcher(apiClient);

        return Optional.of(new PaymentController(paymentExecutor, paymentFetcher));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return cardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return cardRefreshController.fetchCreditCardTransactions();
    }
}
