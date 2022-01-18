package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe;

import java.util.List;
import java.util.Optional;
import se.tink.agent.sdk.operation.Provider;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.HttpClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.HttpClientParams;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiGlobeAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiGlobeAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.client.CbiGlobeAuthApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.client.CbiGlobeFetcherApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.client.CbiGlobePaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.client.CbiGlobeRequestBuilder;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.CbiGlobeConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.CbiGlobeProviderConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.CbiGlobePaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.CbiGlobeTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.filter.CbiGlobeRetryFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.filter.CbiGlobeTokenFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.filter.CbiGlobeUnknownResourceRetryFilter;
import se.tink.backend.aggregation.agents.utils.transfer.InferredTransferDestinations;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.payment.exception.PaymentControllerExceptionMapper;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.filter.filters.AccessExceededFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.BadGatewayFilter;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.payloadparser.PayloadParser;

public abstract class CbiGlobeAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshTransferDestinationExecutor {

    protected final RandomValueGenerator randomValueGenerator;
    protected final LocalDateTimeSource localDateTimeSource;

    protected final String psuIpAddress;
    protected final Provider provider;
    protected final CbiGlobeProviderConfiguration providerConfiguration;

    protected final CbiStorage storage;
    protected final CbiUrlProvider urlProvider;

    protected final CbiGlobeRequestBuilder cbiRequestBuilder;
    protected final CbiGlobeAuthApiClient authApiClient;
    protected final CbiGlobeFetcherApiClient fetcherApiClient;

    protected TransactionalAccountRefreshController transactionalAccountRefreshController;

    public CbiGlobeAgent(AgentComponentProvider agentComponentProvider) {
        super(agentComponentProvider);
        randomValueGenerator = agentComponentProvider.getRandomValueGenerator();
        localDateTimeSource = agentComponentProvider.getLocalDateTimeSource();

        psuIpAddress = agentComponentProvider.getUser().getIpAddress();
        provider = agentComponentProvider.getProvider();
        providerConfiguration =
                PayloadParser.parse(provider.getPayload(), CbiGlobeProviderConfiguration.class);

        storage = new CbiStorage(persistentStorage, sessionStorage, new TemporaryStorage());
        urlProvider = new CbiUrlProvider(getBaseUrl());

        cbiRequestBuilder = buildRequestBuilder();
        authApiClient = buildAuthApiClient();
        fetcherApiClient = buildFetcherApiClient();

        transactionalAccountRefreshController = getTransactionalAccountRefreshController();

        configureTinkHttpClient();
    }

    private void configureTinkHttpClient() {
        client.setTimeout(HttpClientParams.CLIENT_TIMEOUT);
        client.addFilter(
                new CbiGlobeUnknownResourceRetryFilter(
                        HttpClient.MAX_RETRIES, HttpClient.RETRY_SLEEP_MILLISECONDS));
        client.addFilter(new AccessExceededFilter());
        client.addFilter(new BadGatewayFilter());
        client.addFilter(
                new CbiGlobeRetryFilter(
                        HttpClient.MAX_RETRIES, HttpClient.RETRY_SLEEP_MILLISECONDS));
        client.addFilter(
                new CbiGlobeTokenFilter(
                        client,
                        storage,
                        getAgentConfiguration().getProviderSpecificConfiguration(),
                        urlProvider));
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        client.setEidasProxy(configuration.getEidasProxy());
    }

    protected AgentConfiguration<CbiGlobeConfiguration> getAgentConfiguration() {
        return getAgentConfigurationController().getAgentConfiguration(CbiGlobeConfiguration.class);
    }

    /**
     * Historically, all CBI banks were hosted on the same address. Now some of the banks have their
     * own hosts.
     *
     * @return base url (protocol + host) where bank's API can be accessed.
     */
    protected String getBaseUrl() {
        return Urls.BASE_URL;
    }

    protected CbiGlobeRequestBuilder buildRequestBuilder() {
        return new CbiGlobeRequestBuilder(
                client,
                randomValueGenerator,
                localDateTimeSource,
                providerConfiguration,
                strongAuthenticationState,
                getAgentConfiguration().getRedirectUrl(),
                psuIpAddress);
    }

    protected CbiGlobeAuthApiClient buildAuthApiClient() {
        return new CbiGlobeAuthApiClient(cbiRequestBuilder, providerConfiguration, urlProvider);
    }

    protected CbiGlobeFetcherApiClient buildFetcherApiClient() {
        return new CbiGlobeFetcherApiClient(cbiRequestBuilder, urlProvider, storage);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new CbiGlobeAuthenticator(
                        authApiClient,
                        fetcherApiClient,
                        storage,
                        localDateTimeSource,
                        supplementalInformationController,
                        credentials),
                new CbiGlobeAutoAuthenticator(authApiClient, storage));
    }

    protected TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        CbiGlobeTransactionalAccountFetcher accountFetcher =
                CbiGlobeTransactionalAccountFetcher.createFromBoth(
                        new CbiGlobeFetcherApiClient(cbiRequestBuilder, urlProvider, storage),
                        storage);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionPagePaginationController<>(accountFetcher, 1)));
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
        return SessionHandler.alwaysFail();
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        CbiGlobePaymentExecutor paymentExecutor =
                new CbiGlobePaymentExecutor(
                        authApiClient,
                        new CbiGlobePaymentApiClient(
                                cbiRequestBuilder, urlProvider, providerConfiguration),
                        supplementalInformationHelper,
                        sessionStorage,
                        strongAuthenticationState,
                        provider);

        return Optional.of(
                new PaymentController(
                        paymentExecutor, paymentExecutor, new PaymentControllerExceptionMapper()));
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return InferredTransferDestinations.forPaymentAccounts(
                accounts, AccountIdentifierType.IBAN);
    }
}
