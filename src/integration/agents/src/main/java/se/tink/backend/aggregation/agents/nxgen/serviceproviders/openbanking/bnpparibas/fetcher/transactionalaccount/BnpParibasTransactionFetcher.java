package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount;

import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasApiBaseClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class BnpParibasTransactionFetcher
        implements TransactionDatePaginator<TransactionalAccount> {

    private final BnpParibasApiBaseClient apiClient;

    public BnpParibasTransactionFetcher(BnpParibasApiBaseClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        return apiClient.getTransactions(account.getAccountNumber(), fromDate, toDate);
    }
}
