package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class BelfiusTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionDatePaginator<TransactionalAccount> {

    private final BelfiusApiClient apiClient;

    public BelfiusTransactionalAccountFetcher(BelfiusApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        // TODO Belfius has no way of fetching all accounts, currently we have to hardcode the IDs,
        // and currently only ID=1 works
        List<String> accounts = Collections.singletonList("1");

        return accounts.stream()
                .map(
                        logicalId -> {
                            FetchAccountResponse response = apiClient.fetchAccountById();
                            return response.toTinkAccount(logicalId);
                        })
                .collect(Collectors.toList());
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        try {
            return PaginatorResponseImpl.create(
                    apiClient.fetchTransactionsForAccount(fromDate, toDate).toTinkTransactions());
        } catch (Exception e) {
            return PaginatorResponseImpl.createEmpty(false);
        }
    }
}
