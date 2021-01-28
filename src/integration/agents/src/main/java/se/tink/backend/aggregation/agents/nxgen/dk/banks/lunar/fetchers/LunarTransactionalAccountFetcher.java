package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers;

import java.util.Collection;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.LunarApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.UpcomingTransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;

@RequiredArgsConstructor
public class LunarTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionPagePaginator<TransactionalAccount>,
                UpcomingTransactionFetcher<TransactionalAccount> {

    private final LunarApiClient apiClient;

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        // For now just check if user is logged and can fetch accounts
        apiClient.fetchAccounts();
        return Collections.emptyList();
    }

    @Override
    public Collection<UpcomingTransaction> fetchUpcomingTransactionsFor(
            TransactionalAccount account) {
        return Collections.emptyList();
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, int page) {
        return PaginatorResponseImpl.createEmpty(false);
    }
}
