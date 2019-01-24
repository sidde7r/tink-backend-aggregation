package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher;

import java.util.Collection;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class CrossKeyTransactionFetcher implements TransactionDatePaginator<TransactionalAccount> {
    private final CrossKeyApiClient client;
    private final CrossKeyConfiguration agentConfiguration;

    public CrossKeyTransactionFetcher(CrossKeyApiClient client, CrossKeyConfiguration agentConfiguration) {
        this.client = client;
        this.agentConfiguration = agentConfiguration;
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, Date fromDate, Date toDate) {
        Collection<? extends Transaction> transactions = client.fetchTransactions(account, fromDate, toDate)
                .getTinkAcccounts(agentConfiguration);

        return PaginatorResponseImpl.create(transactions);
    }
}
