package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.BbvaMxApiClient;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.BbvaMxConstants;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.UpcomingTransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class BbvaMxTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionDatePaginator<TransactionalAccount>,
                UpcomingTransactionFetcher<TransactionalAccount> {

    private final BbvaMxApiClient client;
    private final PersistentStorage storage;

    public BbvaMxTransactionalAccountFetcher(BbvaMxApiClient client, PersistentStorage storage) {
        this.client = client;
        this.storage = storage;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return client.fetchAccounts()
                .toTransactionalAccounts(storage.get(BbvaMxConstants.STORAGE.HOLDERNAME));
    }

    @Override
    public Collection<UpcomingTransaction> fetchUpcomingTransactionsFor(
            TransactionalAccount account) {
        return Collections.emptyList();
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        String accountId = account.getFromTemporaryStorage(BbvaMxConstants.STORAGE.ACCOUNT_ID);
        try {
            return client.fetchTransactions(accountId, fromDate, toDate);
        } catch (HttpResponseException e) {
            return PaginatorResponseImpl.createEmpty(false);
        }
    }
}
