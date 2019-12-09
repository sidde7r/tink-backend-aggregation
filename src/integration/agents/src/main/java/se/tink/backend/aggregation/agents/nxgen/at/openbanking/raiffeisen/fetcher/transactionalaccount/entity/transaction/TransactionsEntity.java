package se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.fetcher.transactionalaccount.entity.transaction;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsEntity {

    private List<TransactionEntity> booked;
    private List<TransactionEntity> pending;
    private Links links;

    public List<TransactionEntity> getBooked() {
        return Optional.ofNullable(booked).orElse(Collections.emptyList());
    }

    public List<TransactionEntity> getPending() {
        return Optional.ofNullable(pending).orElse(Collections.emptyList());
    }

    public boolean hasMore() {
        return Optional.ofNullable(links).map(Links::hasNextLink).orElse(false);
    }
}
