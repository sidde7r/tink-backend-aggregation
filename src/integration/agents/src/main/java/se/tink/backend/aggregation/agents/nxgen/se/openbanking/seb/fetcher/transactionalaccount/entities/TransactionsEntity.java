package se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.fetcher.transactionalaccount.entities;

import java.util.Collection;
import java.util.List;
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
                        booked != null
                                ? booked.stream().map(BookedEntity::toTinkTransaction)
                                : Stream.empty(),
                        pending != null
                                ? pending.stream().map(PendingEntity::toTinkTransaction)
                                : Stream.empty())
                .collect(Collectors.toList());
    }
}
