package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.SamlinkApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class SamlinkTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionKeyPaginator<
                        TransactionalAccount, SamlinkTransactionalAccountFetcher.TransactionKey> {

    private final SamlinkApiClient apiClient;

    public SamlinkTransactionalAccountFetcher(SamlinkApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {

        return apiClient.getAccounts().stream()
                .map(AccountEntity::toTransactionalAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionKeyPaginatorResponse<TransactionKey> getTransactionsFor(
            TransactionalAccount account, TransactionKey key) {

        if (key == null) {
            return fetchInitialTransactions(account);
        }

        return fetchNextTransactions(key);
    }

    private TransactionKeyPaginatorResponseImpl<TransactionKey> fetchInitialTransactions(
            TransactionalAccount account) {
        return apiClient
                .getTransactions(account)
                .map(transactions -> createResponse(transactions, new TransactionKey(transactions)))
                .orElseGet(TransactionKeyPaginatorResponseImpl::new);
    }

    private TransactionKeyPaginatorResponseImpl<TransactionKey> fetchNextTransactions(
            TransactionKey key) {
        return apiClient
                .getTransactions(key.transactions, key.paginationOffSet)
                .map(
                        transactions ->
                                createResponse(transactions, new TransactionKey(transactions, key)))
                .orElseGet(TransactionKeyPaginatorResponseImpl::new);
    }

    private TransactionKeyPaginatorResponseImpl<TransactionKey> createResponse(
            TransactionsResponse transactions, TransactionKey key) {
        TransactionKeyPaginatorResponseImpl<TransactionKey> response =
                new TransactionKeyPaginatorResponseImpl<>();
        response.setTransactions(transactions.toTinkTransactions(apiClient::getTransactionDetails));
        response.setNext(key);
        return response;
    }

    static class TransactionKey {

        private final int paginationOffSet;
        private final TransactionsResponse transactions;

        TransactionKey(TransactionsResponse transactions) {
            this.transactions = transactions;
            this.paginationOffSet = transactions.size();
        }

        TransactionKey(TransactionsResponse transactions, TransactionKey previousKey) {
            this.transactions = transactions;
            this.paginationOffSet = previousKey.paginationOffSet + transactions.size();
        }
    }
}
