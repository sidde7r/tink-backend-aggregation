package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics;

import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.configuration.SwedbankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.SwedbankTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.SwedbankTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.filter.SwedbankConsentLimitFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.filter.SwedbankMethodNotAllowedFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.SwedbankBalticsAuthenticator;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.nxgen.agents.SubsequentProgressiveGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.filter.filters.BankServiceInternalErrorFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.ServiceUnavailableBankServiceErrorFilter;

// TODO: Maybe to have conception SwedbankBalticsBaseAgent -> SwedbankStepAuthenticationBaseAgent
public class SwedbankBalticsBaseAgent extends SubsequentProgressiveGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final SwedbankApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final SwedbankTransactionalAccountFetcher transactionalAccountFetcher;
    private final AgentComponentProvider componentProvider;

    protected SwedbankBalticsBaseAgent(
            AgentComponentProvider componentProvider,
            QsealcSigner qsealcSigner,
            SwedbankBaseConfiguration swedbankConfiguration) {
        super(componentProvider);
        this.componentProvider = componentProvider;
        client.addFilter(new SwedbankConsentLimitFilter());
        client.addFilter(new SwedbankMethodNotAllowedFilter());
        client.addFilter(new BankServiceInternalErrorFilter());
        client.addFilter(new ServiceUnavailableBankServiceErrorFilter());
        apiClient =
                new SwedbankApiClient(
                        client,
                        persistentStorage,
                        getAgentConfiguration(),
                        qsealcSigner,
                        componentProvider,
                        swedbankConfiguration.getBIC(),
                        swedbankConfiguration.getAuthenticationMethodId(),
                        swedbankConfiguration.getBookingStatus());

        transactionalAccountFetcher =
                new SwedbankTransactionalAccountFetcher(
                        apiClient,
                        persistentStorage,
                        sessionStorage,
                        transactionPaginationHelper,
                        componentProvider);
        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public StatelessProgressiveAuthenticator getAuthenticator() {
        return new SwedbankBalticsAuthenticator(
                apiClient, persistentStorage, sessionStorage, request, provider);
    }

    @Override
    public void setConfiguration(final AgentsServiceConfiguration agentsServiceConfiguration) {
        super.setConfiguration(agentsServiceConfiguration);
        client.setEidasProxy(agentsServiceConfiguration.getEidasProxy());
    }

    private AgentConfiguration<SwedbankConfiguration> getAgentConfiguration() {
        return getAgentConfigurationController().getAgentConfiguration(SwedbankConfiguration.class);
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
                transactionalAccountFetcher,
                new SwedbankTransactionFetcher(
                        apiClient,
                        sessionStorage,
                        request.getProvider().getMarket(),
                        componentProvider));
    }
}
