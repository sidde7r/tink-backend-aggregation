package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankDefaultApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.EngagementOverviewResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.EngagementTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.SavingAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.TransactionDisposalAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.TransactionalAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.UpcomingTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.UpcomingTransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;

public class SwedbankDefaultTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount>,
        TransactionKeyPaginator<TransactionalAccount, LinkEntity>, UpcomingTransactionFetcher<TransactionalAccount> {

    private final SwedbankDefaultApiClient apiClient;
    private final String defaultCurrency;

    private UpcomingTransactionsResponse upcomingTransactionsResponse;

    public SwedbankDefaultTransactionalAccountFetcher(SwedbankDefaultApiClient apiClient, String defaultCurrency) {
        this.apiClient = apiClient;
        this.defaultCurrency = defaultCurrency;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        EngagementOverviewResponse engagementOverviewResponse = apiClient.engagementOverview();
        ArrayList<TransactionalAccount> accounts = new ArrayList<>();

        accounts.addAll(engagementOverviewResponse.getTransactionAccounts().stream()
                .map(TransactionalAccountEntity::toTransactionalAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList()));
        accounts.addAll(engagementOverviewResponse.getTransactionDisposalAccounts().stream()
                .map(TransactionDisposalAccountEntity::toTransactionalAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList()));
        accounts.addAll(engagementOverviewResponse.getSavingAccounts().stream()
                .map(SavingAccountEntity::toTransactionalAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList()));

        return accounts;
    }

    @Override
    public Collection<UpcomingTransaction> fetchUpcomingTransactionsFor(TransactionalAccount account) {
        if (upcomingTransactionsResponse == null) {
            upcomingTransactionsResponse = apiClient.upcomingTransactions();
        }

        return upcomingTransactionsResponse.toTinkUpcomingTransactions(account.getAccountNumber(), defaultCurrency);
    }

    @Override
    public TransactionKeyPaginatorResponse<LinkEntity> getTransactionsFor(
            TransactionalAccount account, LinkEntity key) {
        if (key != null) {
            return apiClient.engagementTransactions(key);
        }

        LinkEntity nextLink = account.getTemporaryStorage(SwedbankBaseConstants.StorageKey.NEXT_LINK, LinkEntity.class);

        TransactionKeyPaginatorResponseImpl<LinkEntity> transactionKeyPaginatorResponse =
                new TransactionKeyPaginatorResponseImpl<>();
        if (nextLink == null) {
            // Return empty response
            return transactionKeyPaginatorResponse;
        }

        // Every time we fetch the transactions for an account we get all reserved transactions.
        // This is a hack to only get the reserved transactions from the first response.
        EngagementTransactionsResponse engagementTransactionsResponse = apiClient.engagementTransactions(nextLink);

        List<Transaction> transactions = new ArrayList<>();
        transactions.addAll(engagementTransactionsResponse.toTransactions());
        transactions.addAll(engagementTransactionsResponse.reservedTransactionsToTransactions());

        transactionKeyPaginatorResponse.setNext(engagementTransactionsResponse.nextKey());
        transactionKeyPaginatorResponse.setTransactions(transactions);

        return transactionKeyPaginatorResponse;
    }
}
