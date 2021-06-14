package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.BICProduction;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.RequestValues;
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

public class SwedbankBalticsBaseAgent extends SubsequentProgressiveGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshTransferDestinationExecutor {

    private final SwedbankApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    //    private final TransferDestinationRefreshController transferDestinationRefreshController;
    private final SwedbankTransactionalAccountFetcher transactionalAccountFetcher;

    protected SwedbankBalticsBaseAgent(
            AgentComponentProvider componentProvider, QsealcSigner qsealcSigner) {
        super(componentProvider);
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
                        componentProvider.getCredentialsRequest(),
                        BICProduction.ESTONIA,
                        RequestValues.SMART_ID); // TODO: get authType from config

        transactionalAccountFetcher =
                new SwedbankTransactionalAccountFetcher(
                        apiClient, persistentStorage, sessionStorage, transactionPaginationHelper);
        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
        //        transferDestinationRefreshController = constructTransferDestinationController();
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public StatelessProgressiveAuthenticator getAuthenticator() {
        return new SwedbankBalticsAuthenticator(apiClient, persistentStorage, sessionStorage);
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
        // TODO: fixme
        return new FetchTransactionsResponse(new HashMap<>());
        //        return transactionalAccountRefreshController.fetchCheckingTransactions();
    }

    @Override
    public FetchAccountsResponse fetchSavingsAccounts() {
        // TODO: fixme
        return new FetchAccountsResponse(new LinkedList<>());
        //        return transactionalAccountRefreshController.fetchSavingsAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchSavingsTransactions() {
        // TODO: fixme
        return new FetchTransactionsResponse(new HashMap<>());
        //        return transactionalAccountRefreshController.fetchSavingsTransactions();
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        // TODO: fixme
        return new FetchTransferDestinationsResponse(new HashMap<>());
        //        return transferDestinationRefreshController.fetchTransferDestinations(accounts);
    }

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                transactionalAccountFetcher,
                new SwedbankTransactionFetcher(
                        apiClient, sessionStorage, request.getProvider().getMarket()));
    }

    // TODO: fixme
    //    private TransferDestinationRefreshController constructTransferDestinationController() {
    //        return new TransferDestinationRefreshController(
    //                metricRefreshController, new SwedbankTransferDestinationFetcher());
    //    }
}
