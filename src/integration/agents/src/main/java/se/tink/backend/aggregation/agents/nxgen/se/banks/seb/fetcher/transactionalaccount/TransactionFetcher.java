package se.tink.backend.aggregation.agents.nxgen.se.banks.seb.fetcher.transactionalaccount;

import java.util.Collections;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.SEBApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.fetcher.transactionalaccount.entities.TransactionPageKey;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class TransactionFetcher
        implements TransactionKeyPaginator<TransactionalAccount, TransactionPageKey> {
    private final SEBApiClient apiClient;

    public TransactionFetcher(SEBApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public TransactionKeyPaginatorResponse<TransactionPageKey> getTransactionsFor(
            TransactionalAccount account, TransactionPageKey key) {
        return new TransactionKeyPaginatorResponseImpl<>(Collections.emptyList(), null);
    }
}
