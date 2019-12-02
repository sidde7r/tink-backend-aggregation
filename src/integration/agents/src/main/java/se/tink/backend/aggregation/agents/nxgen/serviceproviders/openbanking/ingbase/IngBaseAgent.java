package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase;

import java.time.LocalDate;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants.Transaction;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.authenticator.IngBaseAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.configuration.IngBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.IngBaseAccountsFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.IngBaseTransactionsFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.filters.IngBaseGatewayTimeoutFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.session.IngSessionHandler;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.eidassigner.EidasIdentity;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.BankServiceInternalErrorFilter;
import se.tink.backend.aggregation.nxgen.http.filter.ServiceUnavailableBankServiceErrorFilter;
import se.tink.backend.aggregation.nxgen.http.filter.TimeoutFilter;
import se.tink.backend.aggregation.nxgen.http.filter.TimeoutRetryFilter;
import se.tink.libraries.credentials.service.CredentialsRequest;

public abstract class IngBaseAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final String clientName;
    protected final IngBaseApiClient apiClient;

    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private AutoAuthenticationController authenticator;

    public IngBaseAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, true);
        configureHttpClient(client);
        /*
            ING in their documentation use country code in lowercase, however their API treat
            lowercase as wrong country code and returns error that it's malformed
        */
        final String marketInUppercase = request.getProvider().getMarket().toUpperCase();
        apiClient =
                new IngBaseApiClient(
                        client, persistentStorage, marketInUppercase, request.isManual());
        clientName = request.getProvider().getPayload();
        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
    }

    private void configureHttpClient(TinkHttpClient client) {
        client.addFilter(new BankServiceInternalErrorFilter());
        client.addFilter(new IngBaseGatewayTimeoutFilter());
        client.addFilter(new ServiceUnavailableBankServiceErrorFilter());
        client.addFilter(
                new TimeoutRetryFilter(
                        IngBaseConstants.HttpClient.MAX_ATTEMPTS,
                        IngBaseConstants.HttpClient.RETRY_SLEEP_MILLISECONDS));
        client.addFilter(new TimeoutFilter());
    }

    @Override
    public void setConfiguration(final AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        final IngBaseConfiguration ingBaseConfiguration = getClientConfiguration();

        EidasIdentity eidasIdentity =
                new EidasIdentity(context.getClusterId(), context.getAppId(), this.getAgentClass());

        apiClient.setConfiguration(
                ingBaseConfiguration, configuration.getEidasProxy(), eidasIdentity);
        client.setEidasProxy(configuration.getEidasProxy());
    }

    protected IngBaseConfiguration getClientConfiguration() {
        return getAgentConfigurationController().getAgentConfiguration(IngBaseConfiguration.class);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final IngBaseAuthenticator ingBaseAuthenticator =
                new IngBaseAuthenticator(apiClient, persistentStorage);
        final OAuth2AuthenticationController oAuth2AuthenticationController =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        ingBaseAuthenticator,
                        credentials,
                        strongAuthenticationState);
        authenticator =
                new AutoAuthenticationController(
                        request,
                        context,
                        new ThirdPartyAppAuthenticationController<>(
                                oAuth2AuthenticationController, supplementalInformationHelper),
                        oAuth2AuthenticationController);
        return authenticator;
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
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new IngBaseAccountsFetcher(
                        apiClient,
                        request.getProvider().getCurrency().toUpperCase(),
                        shouldReturnLowercaseAccountId()),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(
                                new IngBaseTransactionsFetcher(
                                        apiClient, this::getTransactionsFromDate))));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new IngSessionHandler();
    }

    /**
     * Use a lowercase IBAN as account ID. Defaults to false (uppercase). Can be overridden per
     * market to match RE agent if needed.
     */
    protected boolean shouldReturnLowercaseAccountId() {
        return false;
    }

    private LocalDate getTransactionsFromDate() {
        final Long authenticationTime =
                persistentStorage.get(StorageKeys.AUTHENTICATION_TIME, Long.TYPE).orElse(0L);
        final long authenticationAge = System.currentTimeMillis() - authenticationTime;
        if (authenticationAge < Transaction.FULL_HISTORY_MAX_AGE) {
            return earliestTransactionHistoryDate();
        }
        return LocalDate.now().minusDays(Transaction.DEFAULT_HISTORY_DAYS);
    }

    /*
     * Available transaction history per market
     * see https://developer.ing.com/api-marketplace/marketplace/b6d5093d-626e-41e9-b9e8-ff287bbe2c07/versions/b063703e-1437-4995-90e2-06dac67fef92/documentation#country-specific-information
     */
    protected abstract LocalDate earliestTransactionHistoryDate();
}
