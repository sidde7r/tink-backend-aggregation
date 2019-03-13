package se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.fetcher.entities;

import java.util.List;
import java.util.stream.Stream;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionsEntity {

    private List<TransactionEntity> booked;
    private List<TransactionEntity> pending;

    public Stream<? extends Transaction> toTinkTransactions() {
        return Stream
            .concat(getNonNullStream(booked).map(TransactionEntity::toBookedTinkTransaction),
                getNonNullStream(pending).map(TransactionEntity::toPendingTinkTransaction));
    }

    private Stream<? extends TransactionEntity> getNonNullStream(
        List<TransactionEntity> transactions) {
        return transactions != null ? transactions.stream() : Stream.empty();
    }
}