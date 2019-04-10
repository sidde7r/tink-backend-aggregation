package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.bec.fetcher.transactionalaccount.entities;

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

    public Collection<? extends Transaction> getTinkTransactions() {
        return Stream.concat(
                        Optional.of(booked).orElse(Collections.emptyList()).stream()
                                .map(BookedEntity::toTinkTransaction),
                        Optional.of(pending).orElse(Collections.emptyList()).stream()
                                .map(PendingEntity::toTinkTransaction))
                .collect(Collectors.toList());
    }
}
