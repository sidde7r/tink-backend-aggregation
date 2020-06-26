package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.SebApiClient;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;

@JsonObject
public class TransactionsEntity {
    private List<BookedEntity> booked;
    private List<PendingEntity> pending;

    @JsonIgnore
    public List<Transaction> getTransactions(SebApiClient apiClient) {
        return Stream.concat(
                        Optional.ofNullable(booked)
                                .map(Collection::stream)
                                .orElse(Stream.empty())
                                .map(bookedEntity -> bookedEntity.toTinkTransaction(apiClient)),
                        Optional.ofNullable(pending)
                                .map(Collection::stream)
                                .orElse(Stream.empty())
                                .filter(PendingEntity::isPendingTransaction)
                                .map(PendingEntity::toTinkTransaction))
                .collect(Collectors.toList());
    }

    public List<UpcomingTransaction> getUpcomingTransactions() {
        return pending.stream()
                .filter(PendingEntity::isUpcomingTransaction)
                .map(PendingEntity::toTinkUpcomingTransaction)
                .collect(Collectors.toList());
    }
}
