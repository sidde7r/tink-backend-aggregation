package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup;

import java.util.List;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.BpceApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.fetcher.transferdestination.BpceTransferDestinationsFetcher;
import se.tink.backend.aggregation.nxgen.agents.SubsequentProgressiveGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.BankServiceInternalErrorFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.GatewayTimeoutFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TimeoutFilter;

public abstract class BpceGroupBaseAgent extends SubsequentProgressiveGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshIdentityDataExecutor,
                RefreshTransferDestinationExecutor {

    protected BpceGroupBaseAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        configureHttpClient(this.client);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return getTransactionalAccountRefreshController().fetchCheckingAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return getTransactionalAccountRefreshController().fetchCheckingTransactions();
    }

    @Override
    public FetchAccountsResponse fetchSavingsAccounts() {
        return getTransactionalAccountRefreshController().fetchSavingsAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchSavingsTransactions() {
        return getTransactionalAccountRefreshController().fetchSavingsTransactions();
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return getTransferDestinationRefreshController().fetchTransferDestinations(accounts);
    }

    protected abstract BpceApiClient createApiClient(RandomValueGenerator randomValueGenerator);

    protected abstract BpceApiClient getApiClient();

    protected abstract TransactionalAccountRefreshController
            constructTransactionalAccountRefreshController();

    protected abstract TransactionalAccountRefreshController
            getTransactionalAccountRefreshController();

    protected TransferDestinationRefreshController constructTransferDestinationRefreshController(
            BpceApiClient bpceApiClient) {
        return new TransferDestinationRefreshController(
                this.metricRefreshController, new BpceTransferDestinationsFetcher(bpceApiClient));
    }

    protected abstract TransferDestinationRefreshController
            getTransferDestinationRefreshController();

    private static void configureHttpClient(TinkHttpClient client) {
        client.setFollowRedirects(false);
        client.disableAggregatorHeader();
        client.addFilter(new BankServiceInternalErrorFilter())
                .addFilter(new GatewayTimeoutFilter())
                .addFilter(new TimeoutFilter());
    }
}
