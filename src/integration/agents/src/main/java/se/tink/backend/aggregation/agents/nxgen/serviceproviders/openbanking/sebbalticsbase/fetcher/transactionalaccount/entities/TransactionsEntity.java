package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.fetcher.transactionalaccount.entities;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class TransactionsEntity {

    private List<BookedEntity> booked;
    private List<PendingEntity> pending;

    public List<Transaction> getTransactions() {
        return Stream.concat(
                        Optional.ofNullable(booked)
                                .map(Collection::stream)
                                .orElse(Stream.empty())
                                .map(BookedEntity::toTinkTransaction),
                        Optional.ofNullable(pending)
                                .map(Collection::stream)
                                .orElse(Stream.empty())
                                .filter(PendingEntity::isReserved)
                                .map(PendingEntity::toTinkTransaction))
                .collect(Collectors.toList());
    }

    public List<Transaction> getPendingTransactions() {
        return pending == null
                ? Collections.emptyList()
                : pending.stream()
                        .filter(PendingEntity::isUpcoming)
                        .map(PendingEntity::toTinkTransaction)
                        .collect(Collectors.toList());
    }
}
