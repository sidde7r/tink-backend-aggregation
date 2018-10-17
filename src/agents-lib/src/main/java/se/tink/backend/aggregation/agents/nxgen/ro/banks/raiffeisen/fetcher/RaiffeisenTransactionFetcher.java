package se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.fetcher;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.RaiffeisenApiClient;
import se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.RaiffeisenConstants;
import se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.fetcher.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionMonthPaginator;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class RaiffeisenTransactionFetcher implements TransactionMonthPaginator<TransactionalAccount> {

    private final RaiffeisenApiClient client;

    public RaiffeisenTransactionFetcher(RaiffeisenApiClient client) {
        this.client = client;
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, Year year, Month month) {
        LocalDate fromDate = LocalDate.of(year.getValue(), month, 1);
        LocalDate toDate = fromDate.with(TemporalAdjusters.lastDayOfMonth());

        if(toDate.isAfter(LocalDate.now())){
            toDate = LocalDate.now();
        }

        int page = 0;
        Collection<Transaction> transactions = new ArrayList<>();

        try {
            TransactionsResponse response = client.fetchTransctions(account.getFromTemporaryStorage(RaiffeisenConstants.STORAGE.ACCOUNT_ID), fromDate , toDate, page);
            transactions.addAll(response.getTinkTransactions());

            page++;
            int totalPages = response.getTotalPages();

            while(page < totalPages) {
                response = client.fetchTransctions(account.getFromTemporaryStorage(RaiffeisenConstants.STORAGE.ACCOUNT_ID), fromDate , toDate, page);
                transactions.addAll(response.getTinkTransactions());
                page++;
                totalPages = response.getTotalPages();
            }

        } catch (Exception e) {
            return PaginatorResponseImpl.createEmpty(false);
        }
        return PaginatorResponseImpl.create(transactions);
    }
}
