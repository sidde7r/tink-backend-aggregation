package se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
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

    @JsonProperty("_links")
    @Getter
    private Links links;
}
