package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional;

import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.entities.BelfiusTransaction;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.entities.BelfiusUpcomingTransaction;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.rpc.FetchUpcomingTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.UpcomingTransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class BelfiusTransactionalAccountFetcher implements
        AccountFetcher<TransactionalAccount>,
        TransactionPaginator<TransactionalAccount>,
        UpcomingTransactionFetcher<TransactionalAccount> {

    private final BelfiusApiClient apiClient;
    private int numberOfTransactionPages;

    public BelfiusTransactionalAccountFetcher(BelfiusApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return this.apiClient.fetchProducts().stream()
                .filter(entry -> entry.getValue().isTransactionalAccount())
                .map(entry -> entry.getValue().toTransactionalAccount(entry.getKey()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public void resetState() {
        numberOfTransactionPages = 0;
    }

    @Override
    public PaginatorResponse fetchTransactionsFor(TransactionalAccount account) {
        String key = account.getBankIdentifier();
        FetchTransactionsResponse response;
        response = apiClient.fetchTransactions(key,
                BelfiusConstants.FIRST_TRANSACTION_PAGE == numberOfTransactionPages);

        List<Transaction> transactionsPage = response.stream()
                    .map(BelfiusTransaction::toTinkTransaction)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

        numberOfTransactionPages++;

        return PaginatorResponseImpl.create(transactionsPage,
                response.hasNext()
                        && numberOfTransactionPages < BelfiusConstants.MAX_NUMBER_OF_TRANSACTION_PAGES);
    }

    @Override
    public Collection<UpcomingTransaction> fetchUpcomingTransactionsFor(TransactionalAccount account) {
        List<UpcomingTransaction> transactionsAll = new ArrayList<>();
        String key = account.getBankIdentifier();
        boolean initialUpcomingTransactionRequest = true;
        FetchUpcomingTransactionsResponse response;

        do {
            response = apiClient.fetchUpcomingTransactions(key, initialUpcomingTransactionRequest);

            List<UpcomingTransaction> transactionsPage = response.stream()
                    .map(BelfiusUpcomingTransaction::toTinkUpcomingTransaction)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            transactionsAll.addAll(transactionsPage);
            initialUpcomingTransactionRequest = false;

        } while (response.hasNext());

        return transactionsAll;
    }
}
