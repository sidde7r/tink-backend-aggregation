package se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.fetcher.transactionalaccount.entity.transaction;

import java.util.List;
import java.util.Optional;
import org.assertj.core.util.Lists;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsEntity {

    private List<TransactionEntity> booked;
    private List<TransactionEntity> pending;
    private Links links;

    public List<TransactionEntity> getBooked() {
        return Optional.ofNullable(booked).orElse(Lists.emptyList());
    }

    public List<TransactionEntity> getPending() {
        return Optional.ofNullable(pending).orElse(Lists.emptyList());
    }
}
