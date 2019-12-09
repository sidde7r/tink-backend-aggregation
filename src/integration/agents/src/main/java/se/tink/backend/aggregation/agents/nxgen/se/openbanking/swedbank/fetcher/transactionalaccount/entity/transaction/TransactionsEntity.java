package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.entity.transaction;

import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsEntity {
    private List<TransactionEntity> booked;
    private List<TransactionEntity> pending;
    private TransactionLinksEntity accountLinks;

    public Optional<List<TransactionEntity>> getBooked() {
        return Optional.ofNullable(booked);
    }

    public Optional<List<TransactionEntity>> getPending() {
        return Optional.ofNullable(pending);
    }
}
