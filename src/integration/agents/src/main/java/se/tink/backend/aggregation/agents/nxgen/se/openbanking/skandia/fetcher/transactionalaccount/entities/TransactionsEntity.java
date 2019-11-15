package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.fetcher.transactionalaccount.entities;

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
        return Stream.concat(
                        Optional.ofNullable(booked).orElse(Collections.emptyList()).stream()
                                .map(TransactionEntity::toBookingTransaction),
                        Optional.ofNullable(pending).orElse(Collections.emptyList()).stream()
                                .map(TransactionEntity::toPendingTransaction))
                .collect(Collectors.toList());
    }

    public void setBooked(List<TransactionEntity> booked) {
        this.booked = booked;
    }

    public List<TransactionEntity> getBooked() {
        return booked;
    }
}
