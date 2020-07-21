package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.entities;

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

    @JsonProperty("_links")
    private TransactionsLinksEntity links;

    private List<TransactionEntity> booked;
    private List<TransactionEntity> pending;

    public TransactionsLinksEntity getLinks() {
        return links;
    }

    public Collection<Transaction> toTinkTransactions() {
        final Stream<Transaction> bookedStream =
                Optional.ofNullable(booked).orElse(Collections.emptyList()).stream()
                        .map(TransactionEntity::toBookedTinkTransaction);

        final Stream<Transaction> pendingStream =
                Optional.ofNullable(pending).orElse(Collections.emptyList()).stream()
                        .map(TransactionEntity::toPendinginkTransaction);

        return Stream.concat(bookedStream, pendingStream).collect(Collectors.toList());
    }

    public boolean canFetchMore() {
        return links.hasNext();
    }
}
