package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.fetcher.transactionalaccount.entities;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionsEntity {

    private List<TransactionEntity> booked;
    private List<TransactionEntity> pending;

    public Collection<Transaction> toTinkTransactions() {
        return Optional.ofNullable(booked).orElse(Collections.emptyList()).stream()
                .map(TransactionEntity::toBookedTinkTransaction)
                .collect(Collectors.toList());
    }

    public void setBooked(List<TransactionEntity> booked) {
        this.booked = booked;
    }

    public List<TransactionEntity> getBooked() {
        return booked;
    }
}
