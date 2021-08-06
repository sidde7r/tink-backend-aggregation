package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics;

import com.google.inject.Inject;
import java.time.LocalDate;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.SebBalticsConstants.BicCode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.authenticator.SebBalticsDecoupledAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.configuration.SebBalticsConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.fetcher.transactionalaccount.SebBalticsTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.fetcher.transactionalaccount.SebBalticsTransactionalAccountFetcher;
import se.tink.backend.aggregation.client.provider_configuration.rpc.Capability;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.SubsequentProgressiveGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.OAuth2TokenSessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.AccessExceededFilter;

@AgentCapabilities({Capability.CHECKING_ACCOUNTS})
public class SebBalticsAgent extends SubsequentProgressiveGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    protected SebBalticsApiClient apiClient;
    protected AgentConfiguration<SebBalticsConfiguration> agentConfiguration;
    protected SebBalticsConfiguration sebConfiguration;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final LocalDate localDate;
    private final String providerMarket;
    private final String bankBic;

    @Inject
    protected SebBalticsAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        configureHttpClient(client);
        this.localDate = componentProvider.getLocalDateTimeSource().now().toLocalDate();
        this.providerMarket = componentProvider.getCredentialsRequest().getProvider().getMarket();
        this.apiClient = getApiClient();
        this.bankBic = getBankBicCode();
        this.transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    protected SebBalticsApiClient getApiClient() {
        return new SebBalticsApiClient(client, persistentStorage, request, providerMarket);
    }

    private void configureHttpClient(TinkHttpClient client) {
        client.addFilter(new AccessExceededFilter());
    }

    @Override
    public void setConfiguration(final AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        agentConfiguration =
                getAgentConfigurationController()
                        .getAgentConfiguration(SebBalticsConfiguration.class);
        sebConfiguration = agentConfiguration.getProviderSpecificConfiguration();
        apiClient.setConfiguration(sebConfiguration);
        client.setEidasProxy(configuration.getEidasProxy());
    }

    @Override
    public StatelessProgressiveAuthenticator getAuthenticator() {
        return new SebBalticsDecoupledAuthenticator(
                apiClient,
                agentConfiguration,
                sessionStorage,
                persistentStorage,
                credentials,
                request,
                bankBic,
                localDate,
                supplementalInformationController,
                catalog);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new OAuth2TokenSessionHandler(persistentStorage);
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
        SebBalticsTransactionalAccountFetcher accountFetcher =
                new SebBalticsTransactionalAccountFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        this.transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(
                                new SebBalticsTransactionFetcher(
                                        apiClient,
                                        providerMarket,
                                        transactionPaginationHelper,
                                        localDate))));
    }

    private String getBankBicCode() {
        return BicCode.marketToBicMapping.get(providerMarket);
    }
}
