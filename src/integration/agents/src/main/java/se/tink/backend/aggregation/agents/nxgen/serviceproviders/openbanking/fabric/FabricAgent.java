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
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.libraries.account.enums.AccountIdentifierType;

public abstract class FabricAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshTransferDestinationExecutor,
                UrlProvider {

    protected final LocalDateTimeSource localDateTimeSource;
    protected final FabricApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    @Inject
    public FabricAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);

        Objects.requireNonNull(getBaseUrl());
        localDateTimeSource = componentProvider.getLocalDateTimeSource();
        apiClient =
                new FabricApiClient(
                        client,
                        persistentStorage,
                        componentProvider.getRandomValueGenerator(),
                        sessionStorage,
                        request.getUserAvailability().getOriginatingUserIpOrDefault(),
                        getBaseUrl(),
                        AuthenticationFlow.EMBEDDED.equals(provider.getAuthenticationFlow())
                                ? null
                                : getAgentConfiguration().getRedirectUrl());

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
        FabricAutoAuthenticator autoAuthenticator =
                new FabricAutoAuthenticator(persistentStorage, apiClient);

        return AuthenticationFlow.EMBEDDED.equals(provider.getAuthenticationFlow())
                ? createEmbeddedAuthenticator(autoAuthenticator)
                : createRedirectAuthenticator(autoAuthenticator);
    }

    private AutoAuthenticationController createRedirectAuthenticator(
            FabricAutoAuthenticator autoAuthenticator) {
        FabricRedirectAuthenticator redirectAuthenticator =
                new FabricRedirectAuthenticator(
                        persistentStorage,
                        supplementalInformationHelper,
                        strongAuthenticationState,
                        apiClient,
                        credentials,
                        localDateTimeSource);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        redirectAuthenticator, supplementalInformationHelper),
                autoAuthenticator);
    }

    private AutoAuthenticationController createEmbeddedAuthenticator(
            FabricAutoAuthenticator autoAuthenticator) {
        FabricEmbeddedAuthenticator embeddedAuthenticator =
                new FabricEmbeddedAuthenticator(
                        persistentStorage,
                        apiClient,
                        new FabricSupplementalInformationCollector(
                                context.getCatalog(), supplementalInformationController),
                        localDateTimeSource);

        return new AutoAuthenticationController(
                request, context, embeddedAuthenticator, autoAuthenticator);
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
        FabricAccountFetcher accountFetcher = new FabricAccountFetcher(apiClient);
        FabricTransactionFetcher transactionFetcher = new FabricTransactionFetcher(apiClient);

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
                        apiClient,
                        supplementalInformationHelper,
                        sessionStorage,
                        strongAuthenticationState);
        return Optional.of(new PaymentController(paymentExecutor, paymentExecutor));
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return InferredTransferDestinations.forPaymentAccounts(
                accounts, AccountIdentifierType.IBAN);
    }
}
