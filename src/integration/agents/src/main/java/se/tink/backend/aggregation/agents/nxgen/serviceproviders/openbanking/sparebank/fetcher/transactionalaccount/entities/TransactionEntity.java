package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.entities;

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
public class TransactionEntity {
    private List<BookedPendingTransactionEntity> booked = Collections.emptyList();
    private List<BookedPendingTransactionEntity> pending = Collections.emptyList();

    @JsonProperty("_links")
    private LinksEntity links;

    public Collection<Transaction> toTinkTransactions(boolean includePending) {

        final Stream<Transaction> bookedTransactionsStream =
                booked.stream().map(entity -> entity.toTinkTransaction(false));

        final Stream<Transaction> pendingTransactionsStream =
                pending.stream().map(entity -> entity.toTinkTransaction(true));

        if (includePending) {
            return Stream.concat(bookedTransactionsStream, pendingTransactionsStream)
                    .collect(Collectors.toList());
        } else {
            return bookedTransactionsStream.collect(Collectors.toList());
        }
    }

    public Optional<String> getNext() {
        return Optional.ofNullable(links).map(LinksEntity::getNext).map(LinkEntity::getHref);
    }
}
