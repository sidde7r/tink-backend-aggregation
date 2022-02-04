package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.SAVINGS_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.TRANSFERS;

import com.google.inject.Inject;
import java.time.ZoneId;
import java.util.Optional;
import se.tink.agent.sdk.operation.User;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentPisCapability;
import se.tink.backend.aggregation.agents.agentcapabilities.PisCapability;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.FinecoBankAuthenticationHelper;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.FinecoBankAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.client.FinecoBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.client.FinecoHeaderValues;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.client.FinecoUrlProvider;
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
import se.tink.backend.aggregation.nxgen.controllers.payment.exception.PaymentControllerExceptionMapper;
import se.tink.backend.aggregation.nxgen.controllers.payment.validation.impl.SepaCapabilitiesInitializationValidator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionMonthPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.enums.MarketCode;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS, TRANSFERS})
@AgentPisCapability(
        capabilities = {
            PisCapability.SEPA_CREDIT_TRANSFER,
            PisCapability.SEPA_INSTANT_CREDIT_TRANSFER,
            PisCapability.PIS_SEPA_RECURRING_PAYMENTS
        })
public final class FinecoBankAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshCreditCardAccountsExecutor {

    private final LocalDateTimeSource localDateTimeSource;
    private final FinecoStorage finecoStorage;

    private final FinecoBankApiClient apiClient;
    private final TransactionalAccountRefreshController accountRefreshController;
    private final CreditCardRefreshController cardRefreshController;

    @Inject
    public FinecoBankAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        this.localDateTimeSource = componentProvider.getLocalDateTimeSource();
        this.finecoStorage = new FinecoStorage(persistentStorage, sessionStorage);

        this.apiClient = constructApiClient(componentProvider);
        this.accountRefreshController = constructAccountRefreshController();
        this.cardRefreshController = constructCardRefreshController(componentProvider.getUser());
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
                        new FinecoBankAuthenticationHelper(
                                apiClient, finecoStorage, credentials, localDateTimeSource),
                        supplementalInformationController,
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
                        componentProvider.getUser().getIpAddress());

        return new FinecoBankApiClient(
                new FinecoUrlProvider(),
                client,
                headerValues,
                componentProvider.getRandomValueGenerator());
    }

    private TransactionalAccountRefreshController constructAccountRefreshController() {
        final FinecoBankTransactionalAccountFetcher accountFetcher =
                new FinecoBankTransactionalAccountFetcher(apiClient, finecoStorage);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(accountFetcher)
                                .setConsecutiveEmptyPagesLimit(1)
                                .setLocalDateTimeSource(localDateTimeSource)
                                .build()));
    }

    private CreditCardRefreshController constructCardRefreshController(User user) {
        final FinecoBankCreditCardAccountFetcher accountFetcher =
                new FinecoBankCreditCardAccountFetcher(apiClient, finecoStorage, user.isPresent());

        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionMonthPaginationController<>(
                                accountFetcher, ZoneId.of("GMT"), localDateTimeSource)));
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
                        finecoStorage,
                        strongAuthenticationState,
                        supplementalInformationController);

        FinecoPaymentFetcher paymentFetcher = new FinecoPaymentFetcher(apiClient);

        return Optional.of(
                PaymentController.builder()
                        .paymentExecutor(paymentExecutor)
                        .fetchablePaymentExecutor(paymentFetcher)
                        .exceptionHandler(new PaymentControllerExceptionMapper())
                        .validator(
                                new SepaCapabilitiesInitializationValidator(
                                        this.getClass(), MarketCode.valueOf(provider.getMarket())))
                        .build());
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
