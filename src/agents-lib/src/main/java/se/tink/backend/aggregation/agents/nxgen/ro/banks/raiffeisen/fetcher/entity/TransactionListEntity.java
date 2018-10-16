package se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.fetcher.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionListEntity {
    private List<BookedEntity> booked;
    private List<PendingEntity> pending;

    public List<Transaction> toTinkTransactions() {
        List<Transaction> result = new ArrayList<>();
        result.addAll(booked.stream().map(BookedEntity::toTinkTransaction)
                .collect(Collectors.toList()));
        result.addAll(pending.stream().map(PendingEntity::toTinkTransaction)
                .collect(Collectors.toList()));
        return result;
    }
}
