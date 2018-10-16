package se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.fetcher;

import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.RaiffeisenApiClient;
import se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.RaiffeisenConstants;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

public class RaiffeisenTransactionFetcher implements TransactionDatePaginator<TransactionalAccount> {

    private final RaiffeisenApiClient client;

    public RaiffeisenTransactionFetcher(RaiffeisenApiClient client) {
        this.client = client;
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, Date fromDate, Date toDate) {
        try {
            return client.fetchTransctions(account.getFromTemporaryStorage(RaiffeisenConstants.STORAGE.ACCOUNT_ID),
                    fromDate, toDate);
        }
        catch (Exception e) {

        }
        return PaginatorResponseImpl.createEmpty();
    }
}
