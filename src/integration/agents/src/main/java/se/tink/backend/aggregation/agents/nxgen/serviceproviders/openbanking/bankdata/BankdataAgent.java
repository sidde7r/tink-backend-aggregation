package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata;

import java.time.ZoneId;
import java.util.Optional;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.authenticator.BankdataAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.configuration.BankdataConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.BankdataPaymentController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.BankdataPaymentExecutorSelector;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.fetcher.transactionalaccount.BankdataTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.filters.BankdataCustomServerErrorFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.filters.BankdataCustomServerErrorRetryFilter;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
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
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.http.filter.filters.ServerErrorFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TimeoutFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.ConnectionTimeoutRetryFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.ServerErrorRetryFilter;
import se.tink.libraries.credentials.service.UserAvailability;

public abstract class BankdataAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor {

    private final BankdataApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public BankdataAgent(
            AgentComponentProvider componentProvider, String baseUrl, String baseAuthUrl) {
        super(componentProvider);

        BankdataApiConfiguration apiConfiguration = getApiConfiguration(baseUrl, baseAuthUrl);
        configureHttpClient();
        apiClient =
                new BankdataApiClient(
                        client,
                        sessionStorage,
                        persistentStorage,
                        apiConfiguration,
                        context.getLogMasker());
        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    private BankdataApiConfiguration getApiConfiguration(String baseUrl, String baseAuthUrl) {
        UserAvailability userAvailability = request.getUserAvailability();
        return BankdataApiConfiguration.builder()
                .baseUrl(baseUrl)
                .baseAuthUrl(baseAuthUrl)
                .userIp(userAvailability.getOriginatingUserIp())
                .isUserPresent(userAvailability.isUserPresent())
                .build();
    }

    private void configureHttpClient() {
        client.addFilter(new ServerErrorFilter());
        client.addFilter(new BankdataCustomServerErrorFilter());
        client.addFilter(new TimeoutFilter());

        client.addFilter(
                new ServerErrorRetryFilter(
                        BankdataConstants.HttpClient.MAX_RETRIES,
                        BankdataConstants.HttpClient.RETRY_SLEEP_MILLISECONDS));
        client.addFilter(
                new BankdataCustomServerErrorRetryFilter(
                        BankdataConstants.HttpClient.MAX_RETRIES,
                        BankdataConstants.HttpClient.RETRY_SLEEP_MILLISECONDS));
        client.addFilter(
                new ConnectionTimeoutRetryFilter(
                        BankdataConstants.HttpClient.MAX_RETRIES,
                        BankdataConstants.HttpClient.RETRY_SLEEP_MILLISECONDS));
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        apiClient.setConfiguration(getAgentConfiguration(), configuration.getEidasProxy());
    }

    private AgentConfiguration<BankdataConfiguration> getAgentConfiguration() {
        return getAgentConfigurationController().getAgentConfiguration(BankdataConfiguration.class);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new BankdataAuthenticator(apiClient),
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
    public Optional<PaymentController> constructPaymentController() {
        BankdataPaymentExecutorSelector bankdataPaymentExecutorSelector =
                new BankdataPaymentExecutorSelector(apiClient, sessionStorage);

        return Optional.of(
                new BankdataPaymentController(
                        bankdataPaymentExecutorSelector,
                        bankdataPaymentExecutorSelector,
                        supplementalInformationHelper,
                        sessionStorage,
                        strongAuthenticationState));
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return transactionalAccountRefreshController.fetchCheckingAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return transactionalAccountRefreshController.fetchCheckingTransactions();
    }

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        final BankdataTransactionalAccountFetcher accountFetcher =
                new BankdataTransactionalAccountFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(accountFetcher)
                                .setZoneId(ZoneId.of("UTC"))
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
}
