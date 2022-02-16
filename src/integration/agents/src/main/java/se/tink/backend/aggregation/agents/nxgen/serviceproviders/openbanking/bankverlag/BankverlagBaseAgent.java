package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag;

import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagConstants.HttpClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.authenticator.BankverlagAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.authenticator.BankverlagPaymentAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.fetcher.BankverlagAccountsFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.fetcher.BankverlagTransactionsFetcher;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.BasePaymentExecutor;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.BasePaymentMapper;
import se.tink.backend.aggregation.agents.utils.transfer.InferredTransferDestinations;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.payment.exception.PaymentControllerExceptionMapper;
import se.tink.backend.aggregation.nxgen.controllers.payment.validation.impl.SepaCapabilitiesInitializationValidator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.http.filter.filters.AccessExceededFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.BankServiceDownExceptionFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TimeoutFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.ConnectionTimeoutRetryFilter;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.enums.MarketCode;

public abstract class BankverlagBaseAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshTransferDestinationExecutor {

    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    protected final BankverlagApiClient apiClient;
    protected final BankverlagStorage bankverlagStorage;
    protected RandomValueGenerator randomValueGenerator;
    protected LocalDateTimeSource localDateTimeSource;
    private String aspspId;
    protected BankverlagRequestBuilder requestBuilder;

    protected BankverlagBaseAgent(AgentComponentProvider componentProvider, String aspspId) {
        super(componentProvider);
        this.aspspId = aspspId;
        randomValueGenerator = componentProvider.getRandomValueGenerator();
        localDateTimeSource = componentProvider.getLocalDateTimeSource();
        bankverlagStorage = new BankverlagStorage(persistentStorage, sessionStorage);
        requestBuilder =
                new BankverlagRequestBuilder(
                        client, randomValueGenerator, userIp, constructHeaderValues());
        apiClient = constructApiClient();
        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        client.setEidasProxy(configuration.getEidasProxy());
        client.addFilter(new BankServiceDownExceptionFilter());
        client.addFilter(new AccessExceededFilter());
        client.addFilter(new TimeoutFilter());
        client.addFilter(
                new ConnectionTimeoutRetryFilter(
                        HttpClient.MAX_RETRIES, HttpClient.RETRY_SLEEP_MILLISECONDS));
    }

    protected BankverlagHeaderValues constructHeaderValues() {
        return new BankverlagHeaderValues(
                aspspId, request.getUserAvailability().getOriginatingUserIpOrDefault());
    }

    protected abstract BankverlagApiClient constructApiClient();

    @Override
    protected Authenticator constructAuthenticator() {
        BankverlagAuthenticator bankverlagAuthenticator =
                new BankverlagAuthenticator(
                        apiClient,
                        supplementalInformationController,
                        bankverlagStorage,
                        credentials,
                        catalog,
                        aspspId,
                        provider.getDisplayName());

        return new AutoAuthenticationController(
                request, context, bankverlagAuthenticator, bankverlagAuthenticator);
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
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new BankverlagAccountsFetcher(apiClient, bankverlagStorage),
                new BankverlagTransactionsFetcher(apiClient, bankverlagStorage, aspspId));
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

        BankverlagPaymentAuthenticator bankverlagPaymentAuthenticator =
                new BankverlagPaymentAuthenticator(
                        apiClient,
                        supplementalInformationController,
                        bankverlagStorage,
                        credentials,
                        catalog,
                        aspspId,
                        provider.getDisplayName());

        BankverlagPaymentApiClient bankverlagPaymentApiClient =
                new BankverlagPaymentApiClient(new BasePaymentMapper(), requestBuilder);

        BasePaymentExecutor paymentExecutor =
                new BasePaymentExecutor(
                        bankverlagPaymentApiClient, bankverlagPaymentAuthenticator, sessionStorage);

        return Optional.of(
                PaymentController.builder()
                        .paymentExecutor(paymentExecutor)
                        .fetchablePaymentExecutor(paymentExecutor)
                        .exceptionHandler(new PaymentControllerExceptionMapper())
                        .validator(
                                new SepaCapabilitiesInitializationValidator(
                                        this.getClass(), MarketCode.valueOf(provider.getMarket())))
                        .build());
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return InferredTransferDestinations.forPaymentAccounts(
                accounts, AccountIdentifierType.IBAN);
    }
}
