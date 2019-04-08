package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@JsonObject
public class TransactionsEntity {

    @JsonProperty("_links")
    private TransactionsLinksEntity links;

    private List<BookedEntity> booked;
    private List<PendingEntity> pending;

    public Collection<? extends Transaction> toTinkTransactions() {
        return Stream.concat(
                        Optional.ofNullable(booked).orElse(Collections.emptyList()).stream()
                                .map(BookedEntity::toTinkTransaction),
                        Optional.ofNullable(pending).orElse(Collections.emptyList()).stream()
                                .map(PendingEntity::toTinkTransaction))
                .collect(Collectors.toList());
    }
}
