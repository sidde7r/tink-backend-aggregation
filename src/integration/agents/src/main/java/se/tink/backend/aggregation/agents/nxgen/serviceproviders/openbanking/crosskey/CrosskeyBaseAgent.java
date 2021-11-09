package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey;

import java.util.Optional;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.authenticator.CrosskeyBaseAuthCodeAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.configuration.CrosskeyBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.configuration.CrosskeyMarketConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.executor.payment.CrossKeyPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.creditcardaccount.CrossKeyCreditCardAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.creditcardaccount.CrossKeyCreditCardTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.transactionalaccount.CrossKeyTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.transactionalaccount.CrossKeyTransactionalAccountTransactionFetcher;
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
import se.tink.backend.aggregation.nxgen.controllers.session.OAuth2TokenSessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TerminatedHandshakeRetryFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.RetryAfterRetryFilter;

public abstract class CrosskeyBaseAgent extends NextGenerationAgent
        implements RefreshCreditCardAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor {

    protected final CrosskeyBaseApiClient apiClient;
    private final CreditCardRefreshController creditCardRefreshController;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final AgentConfiguration<CrosskeyBaseConfiguration> agentConfiguration;

    public CrosskeyBaseAgent(
            AgentComponentProvider componentProvider,
            QsealcSigner qsealcSigner,
            CrosskeyMarketConfiguration marketConfiguration) {
        super(componentProvider);
        agentConfiguration =
                getAgentConfigurationController()
                        .getAgentConfiguration(CrosskeyBaseConfiguration.class);

        configureHttpClient();
        apiClient =
                createApiClient(
                        qsealcSigner,
                        marketConfiguration,
                        componentProvider.getCredentialsRequest().getProvider().getMarket(),
                        getUserIp());
        final LocalDateTimeSource localDateTimeSource = componentProvider.getLocalDateTimeSource();
        transactionalAccountRefreshController =
                getTransactionalAccountRefreshController(localDateTimeSource);
        creditCardRefreshController = getCreditCardRefreshController(localDateTimeSource);
    }

    private void configureHttpClient() {
        client.addFilter(
                new RetryAfterRetryFilter(
                        CrosskeyBaseConstants.HttpClient.MAX_RETRIES_FOR_429_RETRY_AFTER_RESPONSE));
        client.addFilter(new TerminatedHandshakeRetryFilter());
    }

    private String getUserIp() {
        return request.getUserAvailability().isUserPresent()
                ? request.getUserAvailability().getOriginatingUserIp()
                : null;
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        apiClient.setConfiguration(configuration);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final OAuth2AuthenticationController oAuth2AuthenticationController =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        CrosskeyBaseAuthCodeAuthenticator.getInstanceForAis(apiClient),
                        credentials,
                        strongAuthenticationState);

        return new AutoAuthenticationController(
                request,
                context,
                new ThirdPartyAppAuthenticationController<>(
                        oAuth2AuthenticationController, supplementalInformationHelper),
                oAuth2AuthenticationController);
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
    protected SessionHandler constructSessionHandler() {
        return new OAuth2TokenSessionHandler(persistentStorage);
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
    public Optional<PaymentController> constructPaymentController() {

        final OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        CrosskeyBaseAuthCodeAuthenticator.getInstanceForPis(apiClient),
                        credentials,
                        strongAuthenticationState);

        CrossKeyPaymentExecutor crossKeyPaymentExecutor =
                new CrossKeyPaymentExecutor(
                        apiClient,
                        new ThirdPartyAppAuthenticationController<>(
                                controller, supplementalInformationHelper),
                        credentials,
                        sessionStorage);

        return Optional.of(new PaymentController(crossKeyPaymentExecutor, crossKeyPaymentExecutor));
    }

    private CrosskeyBaseApiClient createApiClient(
            QsealcSigner qsealcSigner,
            CrosskeyMarketConfiguration marketConfiguration,
            String providerMarket,
            String userIp) {
        return new CrosskeyBaseApiClient(
                client,
                sessionStorage,
                marketConfiguration,
                agentConfiguration,
                qsealcSigner,
                providerMarket,
                userIp);
    }

    protected TransactionalAccountRefreshController getTransactionalAccountRefreshController(
            LocalDateTimeSource localDateTimeSource) {
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new CrossKeyTransactionalAccountFetcher(apiClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(
                                        new CrossKeyTransactionalAccountTransactionFetcher(
                                                apiClient))
                                .setLocalDateTimeSource(localDateTimeSource)
                                .build()));
    }

    protected CreditCardRefreshController getCreditCardRefreshController(
            LocalDateTimeSource localDateTimeSource) {
        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                new CrossKeyCreditCardAccountFetcher(apiClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(
                                        new CrossKeyCreditCardTransactionFetcher(apiClient))
                                .setLocalDateTimeSource(localDateTimeSource)
                                .build()));
    }
}
