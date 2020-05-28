package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.fetcher.transactionalaccount;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.BoursoramaApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.BoursoramaConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class BoursoramaTransactionalAccountTransactionFetcher
        implements TransactionPaginator<TransactionalAccount> {
    private final BoursoramaApiClient apiClient;
    private String continuationToken;

    public BoursoramaTransactionalAccountTransactionFetcher(BoursoramaApiClient apiClient) {
        this.apiClient = apiClient;
        // The first request will not have a continuation token, so therefore it's null.
        this.continuationToken = null;
    }

    @Override
    public void resetState() {
        continuationToken = null;
    }

    @Override
    public PaginatorResponse fetchTransactionsFor(TransactionalAccount account) {
        String accountKey =
                account.getFromTemporaryStorage(BoursoramaConstants.Storage.ACCOUNT_KEY);
        TransactionsResponse transactionsResponse =
                apiClient.getTransactions(accountKey, continuationToken);

        Collection<Transaction> transactions = transactionsResponse.getTransactions();
        continuationToken = transactionsResponse.getContinuationToken();
        boolean canFetchMore = transactionsResponse.canFetchMore();

        return PaginatorResponseImpl.create(transactions, canFetchMore);
    }
}
