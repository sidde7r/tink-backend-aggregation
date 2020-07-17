package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne;

import com.google.inject.Inject;
import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.CaisseEpargneAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.executor.beneficiary.CaisseEpargneCreateBeneficiaryExecutor;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.fetcher.identitydata.IdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.fetcher.transactionalaccount.CaisseEpargneTransactionalAccountTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.fetcher.transactionalaccount.CaisseEpragneTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.fetcher.transferdestination.CaisseEpargneTransferDestinationsFetcher;
import se.tink.backend.aggregation.nxgen.agents.SubsequentProgressiveGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation.SupplementalInformationProviderImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.BankServiceInternalErrorFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.GatewayTimeoutFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TimeoutFilter;
import se.tink.backend.aggregation.nxgen.storage.Storage;

public class CaisseEpargneAgent extends SubsequentProgressiveGenerationAgent
        implements RefreshIdentityDataExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshTransferDestinationExecutor {
    private final CaisseEpargneApiClient apiClient;
    private final Storage instanceStorage;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final TransferDestinationRefreshController transferDestinationRefreshController;
    private final SupplementalInformationProviderImpl supplementalInformationProvider;

    @Inject
    protected CaisseEpargneAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        configureHttpClient(client);
        instanceStorage = new Storage();
        apiClient = new CaisseEpargneApiClient(client, sessionStorage, instanceStorage);
        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
        this.transferDestinationRefreshController = constructTransferDestinationRefreshController();
        this.supplementalInformationProvider =
                new SupplementalInformationProviderImpl(supplementalRequester, request);
    }

    private void configureHttpClient(TinkHttpClient client) {
        client.addFilter(new BankServiceInternalErrorFilter())
                .addFilter(new GatewayTimeoutFilter())
                .addFilter(new TimeoutFilter());
    }

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        CaisseEpragneTransactionalAccountFetcher accountFetcher =
                new CaisseEpragneTransactionalAccountFetcher(apiClient);

        CaisseEpargneTransactionalAccountTransactionFetcher transactionFetcher =
                new CaisseEpargneTransactionalAccountTransactionFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(transactionFetcher)));
    }

    private TransferDestinationRefreshController constructTransferDestinationRefreshController() {
        return new TransferDestinationRefreshController(
                metricRefreshController, new CaisseEpargneTransferDestinationsFetcher(apiClient));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return new FetchIdentityDataResponse(
                new IdentityDataFetcher(instanceStorage).fetchIdentityData());
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
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return transferDestinationRefreshController.fetchTransferDestinations(accounts);
    }

    @Override
    public Optional<CreateBeneficiaryController> constructCreateBeneficiaryController() {
        return Optional.of(
                new CreateBeneficiaryController(
                        new CaisseEpargneCreateBeneficiaryExecutor(
                                apiClient, supplementalInformationProvider, instanceStorage)));
    }

    @Override
    public StatelessProgressiveAuthenticator getAuthenticator() {
        return new CaisseEpargneAuthenticator(
                apiClient, instanceStorage, supplementalInformationProvider, persistentStorage);
    }
}
