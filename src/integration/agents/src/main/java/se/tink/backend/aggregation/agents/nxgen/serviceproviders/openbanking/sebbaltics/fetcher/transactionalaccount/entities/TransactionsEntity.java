package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    private List<BookedEntity> booked;
    private List<PendingEntity> pending;

    @JsonIgnore
    public List<Transaction> getTransactions(String providerMarket) {
        return Stream.of(
                        getBookedTransactions(providerMarket),
                        getPendingReservedTransaction(providerMarket))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public List<Transaction> getPendingTransactions(String providerMarket) {
        return Optional.ofNullable(pending).orElse(Collections.emptyList()).stream()
                .filter(PendingEntity::isUpcoming)
                .map(pendingEntity -> pendingEntity.toTinkTransaction(providerMarket))
                .collect(Collectors.toList());
    }

    private List<Transaction> getBookedTransactions(String providerMarket) {
        return Optional.ofNullable(booked)
                .map(Collection::stream)
                .orElse(Stream.empty())
                .map(bookedEntity -> bookedEntity.toTinkTransaction(providerMarket))
                .collect(Collectors.toList());
    }

    private List<Transaction> getPendingReservedTransaction(String providerMarket) {
        return Optional.ofNullable(pending)
                .map(Collection::stream)
                .orElse(Stream.empty())
                .filter(PendingEntity::isReserved)
                .map(pendingEntity -> pendingEntity.toTinkTransaction(providerMarket))
                .collect(Collectors.toList());
    }
}
