package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.Filters.NUMBER_OF_RETRIES;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.Filters.RATE_LIMIT_RETRY_MS_MAX;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.Filters.RATE_LIMIT_RETRY_MS_MIN;

import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.common.signature.QSealSignatureProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.SibsAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.configuration.SibsConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.SibsPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.sign.SignPaymentStrategy;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.sign.SignPaymentStrategyFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.fetcher.transactionalaccount.SibsTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.fetcher.transactionalaccount.SibsTransactionalAccountTransactionFetcher;
import se.tink.backend.aggregation.agents.progressive.ProgressiveAuthAgent;
import se.tink.backend.aggregation.agents.utils.transfer.InferredTransferDestinations;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.SubsequentProgressiveGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.http.filter.filters.executiontime.TimeMeasuredRequestExecutor;
import se.tink.libraries.account.enums.AccountIdentifierType;

public abstract class SibsProgressiveBaseAgent extends SubsequentProgressiveGenerationAgent
        implements RefreshTransferDestinationExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                ProgressiveAuthAgent {

    private static final int MAX_HTTP_NUM_RETRIES = 5;
    private static final int SERVICE_INVALID_MAX_HTTP_NUM_RETRIES = 3;
    private static final int RETRY_SLEEP_MILLISECONDS = 1000;
    protected final SibsBaseApiClient apiClient;
    protected final SibsUserState userState;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final StatelessProgressiveAuthenticator authenticator;
    protected final LocalDateTimeSource localDateTimeSource;

    protected SibsProgressiveBaseAgent(
            AgentComponentProvider agentComponentProvider,
            AgentsServiceConfiguration agentsServiceConfiguration,
            QSealSignatureProvider qSealSignatureProvider) {
        super(agentComponentProvider);
        userState = new SibsUserState(persistentStorage);
        setConfiguration(agentsServiceConfiguration);
        final AgentConfiguration<SibsConfiguration> agentConfiguration = getAgentConfiguration();
        apiClient =
                new SibsBaseApiClient(
                        client,
                        userState,
                        request.getProvider().getPayload(),
                        request.getUserAvailability().isUserPresent(),
                        userIp,
                        agentComponentProvider.getLocalDateTimeSource(),
                        agentConfiguration);

        localDateTimeSource = agentComponentProvider.getLocalDateTimeSource();

        client.addFilter(
                new SibsMessageSignInterceptor(
                        agentConfiguration,
                        qSealSignatureProvider,
                        agentComponentProvider.getRandomValueGenerator()));
        client.setRequestExecutionTimeLogger(
                httpRequest ->
                        TimeMeasuredRequestExecutor.withRequest(httpRequest).withThreshold(0));
        new SibsTinkApiClientConfigurator()
                .applyFilters(
                        client,
                        new SibsRetryFilterProperties(
                                MAX_HTTP_NUM_RETRIES,
                                RETRY_SLEEP_MILLISECONDS,
                                SERVICE_INVALID_MAX_HTTP_NUM_RETRIES),
                        new SibsRateLimitFilterProperties(
                                RATE_LIMIT_RETRY_MS_MIN,
                                RATE_LIMIT_RETRY_MS_MAX,
                                NUMBER_OF_RETRIES),
                        provider.getName());
        client.setEidasProxy(configuration.getEidasProxy());
        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
        authenticator =
                new SibsAuthenticator(
                        apiClient,
                        userState,
                        request,
                        strongAuthenticationState,
                        agentComponentProvider.getLocalDateTimeSource());
    }

    private AgentConfiguration<SibsConfiguration> getAgentConfiguration() {
        return getAgentConfigurationController().getAgentConfiguration(SibsConfiguration.class);
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
        final SibsTransactionalAccountFetcher accountFetcher =
                new SibsTransactionalAccountFetcher(apiClient);
        final SibsTransactionalAccountTransactionFetcher transactionFetcher =
                new SibsTransactionalAccountTransactionFetcher(
                        apiClient, request, userState, localDateTimeSource);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(transactionFetcher)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        SignPaymentStrategy signPaymentStrategy =
                SignPaymentStrategyFactory.buildSignPaymentRedirectStrategy(
                        apiClient, supplementalInformationHelper);
        SibsPaymentExecutor sibsPaymentExecutor =
                new SibsPaymentExecutor(apiClient, signPaymentStrategy, strongAuthenticationState);
        return Optional.of(new PaymentController(sibsPaymentExecutor, sibsPaymentExecutor));
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return InferredTransferDestinations.forPaymentAccounts(
                accounts, AccountIdentifierType.SEPA_EUR, AccountIdentifierType.IBAN);
    }

    @Override
    public StatelessProgressiveAuthenticator getAuthenticator() {
        return authenticator;
    }
}
