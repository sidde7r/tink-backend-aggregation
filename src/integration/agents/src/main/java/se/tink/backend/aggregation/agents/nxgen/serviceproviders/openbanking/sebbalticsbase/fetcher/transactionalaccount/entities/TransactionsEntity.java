package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.fetcher.transactionalaccount.entities;

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

    public List<Transaction> getTransactions(String providerMarket) {
        return Stream.concat(
                        Optional.ofNullable(booked)
                                .map(Collection::stream)
                                .orElse(Stream.empty())
                                .map(
                                        bookedEntity ->
                                                bookedEntity.toTinkTransaction(providerMarket)),
                        Optional.ofNullable(pending)
                                .map(Collection::stream)
                                .orElse(Stream.empty())
                                .filter(PendingEntity::isReserved)
                                .map(
                                        pendingEntity ->
                                                pendingEntity.toTinkTransaction(providerMarket)))
                .collect(Collectors.toList());
    }

    public List<Transaction> getPendingTransactions(String providerMarket) {
        return Optional.ofNullable(pending).orElse(Collections.emptyList()).stream()
                .filter(PendingEntity::isUpcoming)
                .map(pendingEntity -> pendingEntity.toTinkTransaction(providerMarket))
                .collect(Collectors.toList());
    }
}
