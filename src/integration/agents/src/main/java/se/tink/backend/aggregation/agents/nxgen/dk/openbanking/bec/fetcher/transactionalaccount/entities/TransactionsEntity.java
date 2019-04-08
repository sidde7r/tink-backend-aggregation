package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.bec.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@JsonObject
public class TransactionsEntity {

    private List<BookedEntity> booked;
    private List<PendingEntity> pending;

    public Collection<? extends Transaction> getTinkTransactions() {
        return Stream.concat(
                        booked != null
                                ? booked.stream().map(BookedEntity::toTinkTransaction)
                                : Stream.empty(),
                        pending != null
                                ? pending.stream().map(PendingEntity::toTinkTransaction)
                                : Stream.empty())
                .collect(Collectors.toList());
    }
}
