package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.transactionalaccount.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.transactionalaccount.entities.TransactionsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.URL;

import java.util.Collection;
import java.util.Optional;

@JsonObject
public class GetTransactionsResponse implements TransactionKeyPaginatorResponse<String> {

    private TransactionsEntity transactions;

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactions.getTinkTransactions();
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(transactions.canFetchMore());
    }

    @Override
    public String nextKey() {
        return transactions.getLinks().getNext().getHref();
    }
}
