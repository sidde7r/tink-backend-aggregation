package se.tink.backend.aggregation.agents.nxgen.se.openbanking.business.seb;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.business.seb.fetcher.creditcards.SebCreditCardAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.business.seb.fetcher.creditcards.SebCreditCardTransactionsFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.business.seb.fetcher.transactionalaccount.SebTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.business.seb.fetcher.transactionalaccount.SebTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.business.seb.utils.SebStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionMonthPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS})
public final class SebCorporateAgent extends SebBaseAgent<SebCorporateApiClient>
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final SebStorage instanceStorage;

    @Inject
    public SebCorporateAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        this.apiClient = new SebCorporateApiClient(client, persistentStorage, request);

        this.instanceStorage = new SebStorage();
        this.transactionalAccountRefreshController = getTransactionalAccountRefreshController();
        creditCardRefreshController = getCreditCardRefreshController();
    }

    @Override
    protected SebCorporateApiClient getApiClient() {
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
                new SebTransactionalAccountFetcher(apiClient);

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
}
