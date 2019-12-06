package se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.fetcher.transactionalaccount.entity.transaction;

import java.util.List;
import org.apache.commons.collections4.ListUtils;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsEntity {

    private List<TransactionEntity> booked;
    private List<TransactionEntity> pending;

    public List<TransactionEntity> getBooked() {
        return ListUtils.emptyIfNull(booked);
    }

    public List<TransactionEntity> getPending() {
        return ListUtils.emptyIfNull(pending);
    }
}
