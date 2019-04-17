package se.tink.backend.aggregation.agents.nxgen.be.openbanking.crelan.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.crelan.CrelanApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.crelan.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class CrelanTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionDatePaginator<TransactionalAccount> {

    private final CrelanApiClient apiClient;

    public CrelanTransactionalAccountFetcher(CrelanApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        GetAccountsResponse getAccountsResponse = apiClient.getAccounts();
        getAccountsResponse
                .getAccountList()
                .forEach(acc -> acc.setBalance(apiClient.getBalance(acc).getBalances()));

        return getAccountsResponse.toTinkAcounts();
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        return PaginatorResponseImpl.create(
                apiClient.getTransactions(account, fromDate, toDate).toTinkTransactions());
    }
}
