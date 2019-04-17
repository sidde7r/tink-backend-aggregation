package se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.fetcher.transactionalaccount.entities;

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
    private List<TransactionsLinksEntity> links;

    private List<BookedEntity> booked;
    private List<PendingEntity> pending;

    public Collection<? extends Transaction> getTinkTransactions() {
        return Stream.concat(
                        Optional.ofNullable(booked).orElse(Collections.emptyList()).stream()
                                .map(BookedEntity::toTinkTransaction),
                        Optional.ofNullable(pending).orElse(Collections.emptyList()).stream()
                                .map(PendingEntity::toTinkTransaction))
                .collect(Collectors.toList());
    }

    public Boolean canFetchMore() {
        return links != null && !links.isEmpty()
                ? links.get(links.size() - 1).canFetchMore()
                : false;
    }

    public List<BookedEntity> getBooked() {
        return booked;
    }

    public List<PendingEntity> getPending() {
        return pending;
    }
}
