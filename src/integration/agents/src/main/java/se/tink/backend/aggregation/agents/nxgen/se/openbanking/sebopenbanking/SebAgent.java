package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.TRANSFERS;

import com.google.inject.Inject;
import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.SebPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.creditcards.SebCreditCardAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.creditcards.SebCreditCardTransactionsFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.transactionalaccount.SebTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.transactionalaccount.SebTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.transferdestinations.SebTransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.utils.SebStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionMonthPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.AccessExceededFilter;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, TRANSFERS})
public final class SebAgent extends SebBaseAgent<SebApiClient>
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshTransferDestinationExecutor {

    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final SebStorage instanceStorage;
    private final TransferDestinationRefreshController transferDestinationRefreshController;

    @Inject
    public SebAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        connfigureHttpClient(client);
        this.apiClient = new SebApiClient(client, persistentStorage, request.isManual());
        this.instanceStorage = new SebStorage();
        this.transactionalAccountRefreshController = getTransactionalAccountRefreshController();
        creditCardRefreshController = getCreditCardRefreshController();
        transferDestinationRefreshController = constructTransferDestinationController();
    }

    private TransferDestinationRefreshController constructTransferDestinationController() {
        return new TransferDestinationRefreshController(
                metricRefreshController, new SebTransferDestinationFetcher(instanceStorage));
    }

    private void connfigureHttpClient(TinkHttpClient client) {
        client.addFilter(
                new AccessExceededFilter(this.provider != null ? this.provider.getName() : null));
    }

    @Override
    protected SebApiClient getApiClient() {
        return this.apiClient;
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
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return creditCardRefreshController.fetchCreditCardTransactions();
    }

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        SebTransactionalAccountFetcher accountFetcher =
                new SebTransactionalAccountFetcher(apiClient, instanceStorage);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        this.transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(
                                new SebTransactionFetcher(apiClient))));
    }

    private CreditCardRefreshController getCreditCardRefreshController() {
        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                new SebCreditCardAccountFetcher<>(apiClient, instanceStorage),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionMonthPaginationController<>(
                                new SebCreditCardTransactionsFetcher(instanceStorage),
                                SebCommonConstants.ZONE_ID)));
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        SebPaymentExecutor sebPaymentExecutor =
                new SebPaymentExecutor(apiClient, supplementalRequester);

        return Optional.of(new PaymentController(sebPaymentExecutor, sebPaymentExecutor));
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return transferDestinationRefreshController.fetchTransferDestinations(accounts);
    }
}
