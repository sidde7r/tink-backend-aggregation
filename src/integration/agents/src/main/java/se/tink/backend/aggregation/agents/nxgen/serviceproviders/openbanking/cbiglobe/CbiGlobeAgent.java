package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe;

import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.HttpClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.HttpClientParams;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiGlobeAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiUserState;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.CbiGlobeConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.CbiGlobeProviderConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.InstrumentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.CbiGlobePaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.CbiGlobeTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.filter.CbiGlobeBperRetryFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.filter.CbiGlobeRetryFilter;
import se.tink.backend.aggregation.agents.utils.transfer.InferredTransferDestinations;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.SubsequentProgressiveGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.AccessExceededFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.BadGatewayFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.ForbiddenRetryFilter;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.payloadparser.PayloadParser;

public abstract class CbiGlobeAgent extends SubsequentProgressiveGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshTransferDestinationExecutor {

    protected CbiGlobeApiClient apiClient;
    protected TransactionalAccountRefreshController transactionalAccountRefreshController;
    protected TemporaryStorage temporaryStorage;
    protected StatelessProgressiveAuthenticator authenticator;
    protected CbiUserState userState;
    private CbiGlobeProviderConfiguration providerConfiguration;
    protected final String psuIpAddress;

    public CbiGlobeAgent(AgentComponentProvider agentComponentProvider) {
        super(agentComponentProvider);
        psuIpAddress = userIp;
        providerConfiguration =
                PayloadParser.parse(
                        request.getProvider().getPayload(), CbiGlobeProviderConfiguration.class);
        temporaryStorage = new TemporaryStorage();
        apiClient = getApiClient(request.isManual());
        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
        userState = new CbiUserState(persistentStorage, credentials);
        authenticator = getAuthenticator();
        client.setTimeout(HttpClientParams.CLIENT_TIMEOUT);
        applyFilters(client);
    }

    protected CbiGlobeProviderConfiguration getProviderConfiguration() {
        return providerConfiguration;
    }

    private void applyFilters(TinkHttpClient client) {
        client.addFilter(
                new ForbiddenRetryFilter(
                        HttpClient.MAX_RETRIES, HttpClient.RETRY_SLEEP_MILLISECONDS));
        client.addFilter(new AccessExceededFilter());
        client.addFilter(new BadGatewayFilter());
        client.addFilter(
                new CbiGlobeRetryFilter(
                        HttpClient.MAX_RETRIES, HttpClient.RETRY_SLEEP_MILLISECONDS));
        client.addFilter(
                new CbiGlobeBperRetryFilter(
                        HttpClient.MAX_RETRIES,
                        HttpClient.RETRY_SLEEP_MILLISECONDS_SLOW_AUTHENTICATION));
    }

    protected CbiGlobeApiClient getApiClient(boolean requestManual) {
        return new CbiGlobeApiClient(
                client,
                persistentStorage,
                sessionStorage,
                temporaryStorage,
                InstrumentType.ACCOUNTS,
                getProviderConfiguration(),
                requestManual ? psuIpAddress : null);
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        final AgentConfiguration<CbiGlobeConfiguration> agentConfiguration =
                getAgentConfiguration();
        apiClient.setConfiguration(agentConfiguration);
        this.client.setDebugOutput(true);
        this.client.setEidasProxy(configuration.getEidasProxy());
    }

    protected AgentConfiguration<CbiGlobeConfiguration> getAgentConfiguration() {
        return getAgentConfigurationController().getAgentConfiguration(CbiGlobeConfiguration.class);
    }

    @Override
    public StatelessProgressiveAuthenticator getAuthenticator() {
        if (authenticator == null) {
            authenticator =
                    new CbiGlobeAuthenticator(
                            apiClient,
                            strongAuthenticationState,
                            userState,
                            getAgentConfiguration().getProviderSpecificConfiguration());
        }

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

    protected TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        final CbiGlobeTransactionalAccountFetcher accountFetcher =
                CbiGlobeTransactionalAccountFetcher.createFromBoth(apiClient, persistentStorage);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionPagePaginationController<>(accountFetcher, 1)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        CbiGlobePaymentExecutor paymentExecutor =
                new CbiGlobePaymentExecutor(
                        apiClient,
                        supplementalInformationHelper,
                        sessionStorage,
                        strongAuthenticationState,
                        request.getProvider());
        return Optional.of(new PaymentController(paymentExecutor, paymentExecutor));
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return InferredTransferDestinations.forPaymentAccounts(
                accounts, AccountIdentifierType.IBAN);
    }
}
