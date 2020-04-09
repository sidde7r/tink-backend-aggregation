package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.fetcher.transactionalaccount.entities;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionsEntity {
    private List<TransactionEntity> booked;
    private List<TransactionEntity> pending;

    public Collection<Transaction> toTinkTransactions() {
        final Stream<Transaction> bookedTransactions =
                Optional.ofNullable(booked).orElse(Collections.emptyList()).stream()
                        .map(entity -> entity.toTinkTransaction(false));
        final Stream<Transaction> pendingTransactions =
                Optional.ofNullable(pending).orElse(Collections.emptyList()).stream()
                        .map(entity -> entity.toTinkTransaction(true));
        return Stream.concat(bookedTransactions, pendingTransactions).collect(Collectors.toList());
    }
}
