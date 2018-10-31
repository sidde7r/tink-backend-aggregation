package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.AsLhvApiClient;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.fetcher.AsLhvTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.rpc.GetAccountTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class AsLhvTransactionalAccountTransactionFetcher implements TransactionDatePaginator<TransactionalAccount> {
    private final AsLhvTransactionFetcher transactionFetcher;

    public AsLhvTransactionalAccountTransactionFetcher(AsLhvApiClient apiClient) {
        transactionFetcher = new AsLhvTransactionFetcher(apiClient);
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, Date fromDate, Date toDate) {
        return transactionFetcher.getTransactionsFor(account, fromDate, toDate);
    }
}
