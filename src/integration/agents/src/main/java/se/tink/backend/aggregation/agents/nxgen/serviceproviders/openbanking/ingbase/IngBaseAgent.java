package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase;

import com.google.common.collect.ImmutableSet;
import java.security.cert.CertificateException;
import java.time.LocalDate;
import java.util.Objects;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants.Transaction;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.authenticator.IngBaseAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.configuration.MarketConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.IngBaseAccountsFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.IngBaseTransactionsFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.rpc.BaseFetchTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.filters.IngBaseGatewayTimeoutFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.filters.IngRetryFilter;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.BankServiceInternalErrorFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.ServiceUnavailableBankServiceErrorFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TimeoutFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.TimeoutRetryFilter;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.CredentialsRequestType;

public abstract class IngBaseAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                MarketConfiguration {

    protected final IngBaseApiClient apiClient;

    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private AutoAuthenticationController authenticator;
    private final boolean isManualAuthentication;

    public IngBaseAgent(AgentComponentProvider agentComponentProvider) {
        super(agentComponentProvider);
        configureHttpClient(client);
        isManualAuthentication = shouldDoManualAuthentication(request);
        /*
            ING in their documentation use country code in lowercase, however their API treat
            lowercase as wrong country code and returns error that it's malformed
        */
        final String marketInUppercase = request.getProvider().getMarket().toUpperCase();
        apiClient =
                new IngBaseApiClient(
                        client,
                        persistentStorage,
                        marketInUppercase,
                        providerSessionCacheController,
                        isManualAuthentication,
                        this);
        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
    }

    private void configureHttpClient(TinkHttpClient client) {
        client.addFilter(
                new IngRetryFilter(
                        IngBaseConstants.HttpClient.MAX_ATTEMPTS,
                        IngBaseConstants.HttpClient.RETRY_SLEEP_MILLISECONDS));
        client.addFilter(
                new TimeoutRetryFilter(
                        IngBaseConstants.HttpClient.MAX_ATTEMPTS,
                        IngBaseConstants.HttpClient.RETRY_SLEEP_MILLISECONDS));
        client.addFilter(new TimeoutFilter());
        client.addFilter(new BankServiceInternalErrorFilter());
        client.addFilter(new IngBaseGatewayTimeoutFilter());
        client.addFilter(new ServiceUnavailableBankServiceErrorFilter());
    }

    @Override
    public void setConfiguration(final AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        final AgentConfiguration<IngBaseConfiguration> agentConfiguration =
                getAgentConfigurationController().getAgentConfiguration(IngBaseConfiguration.class);

        EidasIdentity eidasIdentity =
                new EidasIdentity(context.getClusterId(), context.getAppId(), this.getAgentClass());

        try {
            apiClient.setConfiguration(
                    agentConfiguration, configuration.getEidasProxy(), eidasIdentity);
        } catch (CertificateException e) {
            throw new IllegalStateException(
                    "Could not parse QSEALC properly while setting up ING agent", e);
        }

        client.setEidasProxy(configuration.getEidasProxy());

        String clientId = persistentStorage.get(StorageKeys.CLIENT_ID);
        if (clientId != null) {
            ImmutableSet<String> whitelistedValues = ImmutableSet.of(clientId);
            context.getLogMasker().addAgentWhitelistedValues(whitelistedValues);
        }
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
        return SessionHandler.alwaysFail();
    }

    /**
     * Use a lowercase IBAN as account ID. Defaults to false (uppercase). Can be overridden per
     * market to match RE agent if needed.
     */
    @Override
    public boolean shouldReturnLowercaseAccountId() {
        return false;
    }

    @Override
    public LocalDate earliestTransactionHistoryDate() {
        return null;
    }

    protected LocalDate getTransactionsFromDate() {
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

    private boolean shouldDoManualAuthentication(final CredentialsRequest request) {

        return !forceAutoAuthentication()
                        && (Objects.equals(
                                        CredentialsTypes.THIRD_PARTY_APP,
                                        request.getCredentials().getType())
                                || (request.isUpdate()
                                        && !Objects.equals(
                                                request.getType(),
                                                CredentialsRequestType.TRANSFER)))
                || request.isForceAuthenticate();
    }

    private boolean forceAutoAuthentication() {
        return Objects.equals(CredentialsTypes.THIRD_PARTY_APP, CredentialsTypes.PASSWORD)
                && !request.isUpdate()
                && !request.isCreate();
    }

    @Override
    public Class<? extends BaseFetchTransactionsResponse> getTransactionsResponseClass() {
        return BaseFetchTransactionsResponse.class;
    }
}
