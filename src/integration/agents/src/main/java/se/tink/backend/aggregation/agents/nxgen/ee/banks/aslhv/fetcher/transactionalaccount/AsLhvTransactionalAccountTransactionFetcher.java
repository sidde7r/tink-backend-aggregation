package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.fetcher.transactionalaccount;

import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.AsLhvApiClient;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.AsLhvSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.fetcher.AsLhvTransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class AsLhvTransactionalAccountTransactionFetcher
        implements TransactionDatePaginator<TransactionalAccount> {
    private final AsLhvTransactionFetcher transactionFetcher;

    public AsLhvTransactionalAccountTransactionFetcher(
            final AsLhvApiClient apiClient, final AsLhvSessionStorage sessionStorage) {
        transactionFetcher = new AsLhvTransactionFetcher(apiClient, sessionStorage);
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        return transactionFetcher.getTransactionsFor(account, fromDate, toDate);
    }
}
