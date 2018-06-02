package se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher;

import java.util.Collection;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.AlandsBankenApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class AlandsBankenTransactionFetcher implements TransactionDatePaginator<TransactionalAccount> {
    private final AlandsBankenApiClient client;

    public AlandsBankenTransactionFetcher(AlandsBankenApiClient client) {
        this.client = client;
    }

    @Override
    public Collection<Transaction> getTransactionsFor(TransactionalAccount account, Date fromDate, Date toDate) {
        return client.fetchTransactions(account, fromDate, toDate)
                .getTinkAcccounts();
    }
}
