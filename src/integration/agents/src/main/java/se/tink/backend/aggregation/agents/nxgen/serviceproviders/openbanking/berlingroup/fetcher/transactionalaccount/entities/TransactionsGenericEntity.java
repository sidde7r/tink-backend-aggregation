package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionsGenericEntity {

    protected List<BookedTransactionBaseEntity> booked = Collections.emptyList();
    protected List<PendingTransactionBaseEntity> pending = Collections.emptyList();

    public List<BookedTransactionBaseEntity> getBooked() {
        return booked;
    }

    public List<PendingTransactionBaseEntity> getPending() {
        return pending;
    }

    public void setBooked(List<BookedTransactionBaseEntity> booked) {
        this.booked = booked;
    }

    public void setPending(List<PendingTransactionBaseEntity> pending) {
        this.pending = pending;
    }

    public Collection<Transaction> toTinkTransactions() {
        final Stream<Transaction> bookedTransactionsStream =
                booked.stream().map(BookedTransactionBaseEntity::toTinkTransaction);
        final Stream<Transaction> pendingTransactionsStream =
                pending.stream().map(PendingTransactionBaseEntity::toTinkTransaction);

        return Stream.concat(bookedTransactionsStream, pendingTransactionsStream)
                .collect(Collectors.toList());
    }
}
