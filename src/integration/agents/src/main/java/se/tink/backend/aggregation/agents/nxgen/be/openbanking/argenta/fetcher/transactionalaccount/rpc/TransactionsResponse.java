package se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.fetcher.transactionalaccount.rpc;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.fetcher.transactionalaccount.entity.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.fetcher.transactionalaccount.entity.TransactionsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionsResponse implements TransactionKeyPaginatorResponse<String> {

    private TransactionsEntity transactions;

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return Stream.concat(
                        transactions.getBooked().stream()
                                .map(TransactionEntity::toTinkBookedTransaction),
                        transactions.getPending().stream()
                                .map(TransactionEntity::toTinkPendingTransaction))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(
                transactions.getLinks() != null && transactions.getLinks().getNext() != null);
    }

    @Override
    public String nextKey() {
        return transactions.getLinks() != null && transactions.getLinks().getNext() != null
                ? transactions.getLinks().getNext().getHref()
                : null;
    }
}
