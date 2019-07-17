package se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionsEntity {

    private List<TransactionEntity> booked;

    @JsonProperty("_links")
    private LinkEntity links;

    private List<TransactionEntity> pending;

    public List<TransactionEntity> getBooked() {
        return booked;
    }

    public LinkEntity getLinks() {
        return links;
    }

    public List<TransactionEntity> getPending() {
        return pending;
    }

    @JsonIgnore
    public Collection<? extends Transaction> toTinkTransactions() {
        return Stream.concat(
                        Optional.ofNullable(booked).orElse(Collections.emptyList()).stream()
                                .map(TransactionEntity::toBookedTinkTransaction),
                        Optional.ofNullable(pending).orElse(Collections.emptyList()).stream()
                                .map(TransactionEntity::toPendingTinkTransaction))
                .collect(Collectors.toList());
    }
}
