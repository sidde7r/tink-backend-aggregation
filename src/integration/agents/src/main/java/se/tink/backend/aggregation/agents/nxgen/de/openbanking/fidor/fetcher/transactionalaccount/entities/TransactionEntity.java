package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.fetcher.transactionalaccount.entities;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionEntity {
    private List<BookedEntity> booked;
    private List<PendingEntity> pending;

    public Collection<? extends Transaction> getTransactions() {
        final Stream<Transaction> bookedTransactionStream =
                Optional.of(booked).orElse(Collections.emptyList()).stream()
                        .map(BookedEntity::toTinkTransaction);
        final Stream<Transaction> pendingTransactionStream =
                Optional.of(pending).orElse(Collections.emptyList()).stream()
                        .map(PendingEntity::toTinkTransaction);

        return Stream.concat(bookedTransactionStream, pendingTransactionStream)
                .collect(Collectors.toList());
    }
}
