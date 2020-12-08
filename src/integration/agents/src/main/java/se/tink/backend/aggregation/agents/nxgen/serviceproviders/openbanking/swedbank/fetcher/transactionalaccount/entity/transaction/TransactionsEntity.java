package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.entity.transaction;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsEntity {
    private List<TransactionEntity> booked;
    private List<TransactionEntity> pending;

    public List<TransactionEntity> getBooked() {
        return Optional.ofNullable(booked).orElseGet(Lists::newArrayList);
    }

    public List<TransactionEntity> getPending() {
        return Optional.ofNullable(pending).orElseGet(Lists::newArrayList);
    }
}
