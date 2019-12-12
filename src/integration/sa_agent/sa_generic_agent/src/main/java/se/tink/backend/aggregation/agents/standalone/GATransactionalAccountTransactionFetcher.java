package se.tink.backend.aggregation.agents.standalone;

import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class GATransactionalAccountTransactionFetcher
        implements TransactionKeyPaginator<TransactionalAccount, String> {
    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String key) {
        TransactionKeyPaginatorResponse resp = new TransactionKeyPaginatorResponseImpl(null, null);
        return resp;
    }
}
