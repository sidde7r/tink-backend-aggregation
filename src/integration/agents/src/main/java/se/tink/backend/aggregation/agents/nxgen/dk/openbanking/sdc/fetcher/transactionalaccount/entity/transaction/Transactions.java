package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.sdc.fetcher.transactionalaccount.entity.transaction;

import java.util.List;
import org.apache.commons.collections4.ListUtils;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Transactions {

    private List<TransactionEntity> booked;
    private List<TransactionEntity> pending;
    private TransactionKeyLinksEntity links;

    public List<TransactionEntity> getBooked() {
        return ListUtils.emptyIfNull(booked);
    }

    public List<TransactionEntity> getPending() {
        return ListUtils.emptyIfNull(pending);
    }
}
