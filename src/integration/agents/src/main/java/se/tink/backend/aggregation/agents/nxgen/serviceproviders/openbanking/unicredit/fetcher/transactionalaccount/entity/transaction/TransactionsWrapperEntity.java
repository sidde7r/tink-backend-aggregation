package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.entity.transaction;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsWrapperEntity {

    private List<TransactionEntity> booked;
    private List<TransactionEntity> pending;

    public List<TransactionEntity> getBooked() {
        return Optional.ofNullable(booked).orElse(Collections.emptyList());
    }

    public List<TransactionEntity> getPending() {
        return Optional.ofNullable(pending).orElse(Collections.emptyList());
    }
}
