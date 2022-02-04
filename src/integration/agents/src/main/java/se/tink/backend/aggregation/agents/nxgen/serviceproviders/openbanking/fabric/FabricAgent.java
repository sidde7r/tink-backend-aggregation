package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric;

import com.google.inject.Inject;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Provider.AuthenticationFlow;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.authenticator.FabricAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.authenticator.FabricEmbeddedAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.authenticator.FabricRedirectAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.authenticator.FabricSupplementalInformationCollector;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.client.FabricAuthApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.client.FabricFetcherApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.client.FabricPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.client.FabricRequestBuilder;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment.FabricPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.fetcher.transactionalaccount.FabricAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.fetcher.transactionalaccount.FabricTransactionFetcher;
import se.tink.backend.aggregation.agents.utils.transfer.InferredTransferDestinations;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.payment.exception.PaymentControllerExceptionMapper;
import se.tink.backend.aggregation.nxgen.controllers.payment.validation.impl.SepaCapabilitiesInitializationValidator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.enums.MarketCode;

public abstract class FabricAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshTransferDestinationExecutor,
                UrlProvider {

    protected final LocalDateTimeSource localDateTimeSource;

    private final FabricRequestBuilder requestBuilder;

    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    @Inject
    public FabricAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);

        Objects.requireNonNull(getBaseUrl());
        localDateTimeSource = componentProvider.getLocalDateTimeSource();

        requestBuilder =
                new FabricRequestBuilder(
                        client,
                        componentProvider.getRandomValueGenerator(),
                        componentProvider.getUser().getIpAddress());

        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    private AgentConfiguration<FabricConfiguration> getAgentConfiguration() {
        return getAgentConfigurationController().getAgentConfiguration(FabricConfiguration.class);
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        client.setEidasProxy(configuration.getEidasProxy());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        FabricAuthApiClient authApiClient = new FabricAuthApiClient(requestBuilder, getBaseUrl());

        TypedAuthenticator fullAuthenticator =
                AuthenticationFlow.EMBEDDED.equals(provider.getAuthenticationFlow())
                        ? createEmbeddedAuthenticator(authApiClient)
                        : createRedirectAuthenticator(authApiClient);

        return new AutoAuthenticationController(
                request,
                context,
                fullAuthenticator,
                new FabricAutoAuthenticator(persistentStorage, authApiClient));
    }

    private TypedAuthenticator createRedirectAuthenticator(FabricAuthApiClient authApiClient) {
        return new ThirdPartyAppAuthenticationController<>(
                new FabricRedirectAuthenticator(
                        persistentStorage,
                        supplementalInformationHelper,
                        strongAuthenticationState,
                        authApiClient,
                        credentials,
                        localDateTimeSource,
                        getAgentConfiguration().getRedirectUrl()),
                supplementalInformationHelper);
    }

    private TypedAuthenticator createEmbeddedAuthenticator(FabricAuthApiClient authApiClient) {
        return new FabricEmbeddedAuthenticator(
                persistentStorage,
                authApiClient,
                new FabricSupplementalInformationCollector(
                        context.getCatalog(), supplementalInformationController),
                localDateTimeSource);
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

    protected TransactionalAccountRefreshController getTransactionalAccountRefreshController() {

        FabricFetcherApiClient fetcherApiClient =
                new FabricFetcherApiClient(requestBuilder, getBaseUrl());

        FabricAccountFetcher accountFetcher =
                new FabricAccountFetcher(persistentStorage, fetcherApiClient);
        FabricTransactionFetcher transactionFetcher =
                new FabricTransactionFetcher(persistentStorage, fetcherApiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(transactionFetcher)
                                .setConsecutiveEmptyPagesLimit(4)
                                .setAmountAndUnitToFetch(85, ChronoUnit.DAYS)
                                .setLocalDateTimeSource(localDateTimeSource)
                                .build()));
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
        FabricPaymentExecutor paymentExecutor =
                new FabricPaymentExecutor(
                        new FabricPaymentApiClient(
                                requestBuilder,
                                sessionStorage,
                                getAgentConfiguration().getRedirectUrl()),
                        supplementalInformationHelper,
                        sessionStorage,
                        strongAuthenticationState);

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
