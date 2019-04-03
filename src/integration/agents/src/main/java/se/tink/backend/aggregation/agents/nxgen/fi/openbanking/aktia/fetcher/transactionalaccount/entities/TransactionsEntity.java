package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.fetcher.transactionalaccount.entities;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionsEntity {

    @JsonProperty("_links")
    private TransactionsLinksEntity links;

    private List<BookedEntity> booked;
    private List<PendingEntity> pending;

    public Collection<? extends Transaction> toTinkTransactions() {
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
