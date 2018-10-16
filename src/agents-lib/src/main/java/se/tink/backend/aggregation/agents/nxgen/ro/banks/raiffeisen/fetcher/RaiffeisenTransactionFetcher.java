package se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.fetcher;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.temporal.TemporalAdjusters;
import se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.RaiffeisenApiClient;
import se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.RaiffeisenConstants;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionMonthPaginator;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

public class RaiffeisenTransactionFetcher implements TransactionMonthPaginator<TransactionalAccount> {

    private final RaiffeisenApiClient client;

    public RaiffeisenTransactionFetcher(RaiffeisenApiClient client) {
        this.client = client;
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, Year year, Month month) {
        // First day of the month
        LocalDate fromDate = LocalDate.of(year.getValue(), month, 1);
        // Last day of the month
        LocalDate toDate = fromDate.with(TemporalAdjusters.lastDayOfMonth());
        int page = 0; //TODO: Fix page pagination

        try {
            return client.fetchTransctions(account.getFromTemporaryStorage(RaiffeisenConstants.STORAGE.ACCOUNT_ID), fromDate , toDate, page);
        } catch (Exception e) {

        }
        return PaginatorResponseImpl.createEmpty();
    }
}
