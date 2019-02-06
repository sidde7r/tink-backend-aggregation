package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.BBVAApiClient;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.BBVAConstants;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.UpcomingTransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class BBVATransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionDatePaginator<TransactionalAccount>,
                UpcomingTransactionFetcher<TransactionalAccount> {

    private final BBVAApiClient client;
    private final PersistentStorage storage;

    public BBVATransactionalAccountFetcher(BBVAApiClient client, PersistentStorage storage) {
        this.client = client;
        this.storage = storage;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return client.fetchAccounts()
                .toTransactionalAccounts(storage.get(BBVAConstants.STORAGE.HOLDERNAME));
    }

    @Override
    public Collection<UpcomingTransaction> fetchUpcomingTransactionsFor(
            TransactionalAccount account) {
        return Collections.emptyList();
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        String accountId = account.getFromTemporaryStorage(BBVAConstants.STORAGE.ACCOUNT_ID);
        try {
            return client.fetchTransactions(accountId, fromDate, toDate);
        } catch (HttpResponseException e) {
            return PaginatorResponseImpl.createEmpty(false);
        }
    }
}
