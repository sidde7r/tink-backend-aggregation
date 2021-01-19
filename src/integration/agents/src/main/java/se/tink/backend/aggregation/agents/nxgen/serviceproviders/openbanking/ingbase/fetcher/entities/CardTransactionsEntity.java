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
public class CardTransactionsEntity {

    private List<CardTransactionEntity> booked;
    private List<CardTransactionEntity> pending;

    @JsonProperty("_links")
    private LinksEntity links;

    @JsonIgnore
    public List<Transaction> toTinkTransactions() {
        return Stream.concat(
                        getNonNullStream(booked)
                                .map(CardTransactionEntity::toBookedTinkTransaction),
                        getNonNullStream(pending)
                                .map(CardTransactionEntity::toPendingTinkTransaction))
                .collect(Collectors.toList());
    }

    @JsonIgnore
    private Stream<CardTransactionEntity> getNonNullStream(
            List<CardTransactionEntity> transactions) {
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
