package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.KnabConstants.HttpClient;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.authenticator.KnabAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.configuration.KnabConfiguration;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.fetcher.KnabAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.fetcher.KnabTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.filter.KnabFailureFilter;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.filter.KnabRetryFilter;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.session.KnabSessionHandler;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.BankServiceInternalErrorFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.GatewayTimeoutFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TerminatedHandshakeRetryFilter;

@AgentCapabilities({CHECKING_ACCOUNTS})
public final class KnabAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final KnabApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    @Inject
    public KnabAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);

        configureHttpClient(client);
        this.apiClient =
                new KnabApiClient(client, persistentStorage, request.getCredentials(), userIp);
        this.transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    private void configureHttpClient(TinkHttpClient client) {
        client.addFilter(new BankServiceInternalErrorFilter());
        client.addFilter(
                new KnabRetryFilter(
                        KnabConstants.HttpClient.MAX_RETRIES,
                        KnabConstants.HttpClient.RETRY_SLEEP_MILLISECONDS));
        client.addFilter(new GatewayTimeoutFilter());
        client.addFilter(
                new TerminatedHandshakeRetryFilter(
                        HttpClient.MAX_RETRIES, HttpClient.RETRY_SLEEP_MILLISECONDS));
        client.addFilter(new KnabFailureFilter());
    }

    @Override
    public void setConfiguration(final AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        final AgentConfiguration<KnabConfiguration> agentConfiguration =
                getAgentConfigurationController().getAgentConfiguration(KnabConfiguration.class);

        this.apiClient.setConfiguration(agentConfiguration);
        this.client.setEidasProxy(configuration.getEidasProxy());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new KnabAuthenticator(
                                supplementalInformationHelper,
                                strongAuthenticationState,
                                apiClient,
                                persistentStorage),
                        credentials,
                        strongAuthenticationState);

        return new AutoAuthenticationController(
                request,
                context,
                new ThirdPartyAppAuthenticationController<>(
                        controller, supplementalInformationHelper),
                controller);
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
                new KnabAccountFetcher(apiClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(
                                        new KnabTransactionFetcher(apiClient))
                                .build()));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new KnabSessionHandler();
    }
}
