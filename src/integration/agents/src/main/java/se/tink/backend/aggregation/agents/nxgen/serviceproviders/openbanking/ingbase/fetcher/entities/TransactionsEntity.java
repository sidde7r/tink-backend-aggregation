package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionsEntity<T extends TransactionEntity> {

    private List<T> booked;
    private List<T> pending;

    @JsonProperty("_links")
    private LinksEntity links;

    @JsonIgnore
    public List<Transaction> toTinkTransactions() {
        return Stream.concat(
                        getNonNullStream(booked).map(TransactionEntity::toBookedTinkTransaction),
                        getNonNullStream(pending).map(TransactionEntity::toPendingTinkTransaction))
                .collect(Collectors.toList());
    }

    @JsonIgnore
    private Stream<T> getNonNullStream(List<T> transactions) {
        return Optional.ofNullable(transactions).orElse(Collections.emptyList()).stream();
    }

    @JsonIgnore
    public String getNextLink() {
        return Optional.ofNullable(links)
                .filter(LinksEntity::hasNext)
                .map(LinksEntity::getNext)
                .orElse(null);
    }
}
