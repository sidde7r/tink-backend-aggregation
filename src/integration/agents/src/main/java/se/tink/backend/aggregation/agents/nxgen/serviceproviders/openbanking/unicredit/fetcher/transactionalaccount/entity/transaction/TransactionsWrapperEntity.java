package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.entity.transaction;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsWrapperEntity {

    private List<TransactionEntity> booked;
    private List<TransactionEntity> pending;

    @JsonProperty("_links")
    private TransactionsLinksEntity links;

    public List<TransactionEntity> getBooked() {
        return Optional.ofNullable(booked).orElse(Collections.emptyList());
    }

    public List<TransactionEntity> getPending() {
        return Optional.ofNullable(pending).orElse(Collections.emptyList());
    }

    public TransactionsLinksEntity getLinks() {
        return links;
    }
}
