package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.fetcher.transactionalaccount.entities;

import java.util.Collection;
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

    public Collection<? extends Transaction> getTransactions() {

        return Stream.concat(
                        Optional.ofNullable(booked)
                                .map(Collection::stream)
                                .orElse(Stream.empty())
                                .map(BookedEntity::toTinkTransaction),
                        Optional.ofNullable(pending)
                                .map(Collection::stream)
                                .orElse(Stream.empty())
                                .map(PendingEntity::toTinkTransaction))
                .collect(Collectors.toList());
    }
}
