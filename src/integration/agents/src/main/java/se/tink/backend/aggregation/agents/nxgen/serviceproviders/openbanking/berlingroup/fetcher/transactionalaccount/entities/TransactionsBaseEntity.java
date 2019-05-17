package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities;

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
public class TransactionsBaseEntity {

    protected List<BookedTransactionBaseEntity> booked = Collections.emptyList();
    protected List<PendingTransactionBaseEntity> pending = Collections.emptyList();

    @JsonProperty("_links")
    private TransactionLinksEntity links;

    public boolean hasMore() {
        return Optional.ofNullable(links).map(TransactionLinksEntity::hasNextLink).orElse(false);
    }

    public String getNextLink() {
        return links.getNextLink();
    }

    public Collection<Transaction> toTinkTransactions() {
        Stream<Transaction> bookedTransactionsStream =
                booked.stream().map(BookedTransactionBaseEntity::toTinkTransaction);
        Stream<Transaction> pendingTransactionsStream =
                pending.stream().map(PendingTransactionBaseEntity::toTinkTransaction);

        return Stream.concat(bookedTransactionsStream, pendingTransactionsStream)
                .collect(Collectors.toList());
    }
}
