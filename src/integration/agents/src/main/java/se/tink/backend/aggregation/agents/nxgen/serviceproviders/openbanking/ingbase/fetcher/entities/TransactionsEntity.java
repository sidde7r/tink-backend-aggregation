package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionsEntity {

    private List<TransactionEntity> booked;
    private List<TransactionEntity> pending;

    @JsonProperty("_links")
    private LinksEntity links;

    public Stream<? extends Transaction> toTinkTransactions() {
        return Stream.concat(
                getNonNullStream(booked).map(TransactionEntity::toBookedTinkTransaction),
                getNonNullStream(pending).map(TransactionEntity::toPendingTinkTransaction));
    }

    private Stream<? extends TransactionEntity> getNonNullStream(
            List<TransactionEntity> transactions) {
        return Optional.ofNullable(transactions).orElse(Collections.emptyList()).stream();
    }

    public String getNextLink() {
        return Optional.ofNullable(links)
                .filter(LinksEntity::hasNext)
                .map(LinksEntity::getNext)
                .orElse(null);
    }
}
