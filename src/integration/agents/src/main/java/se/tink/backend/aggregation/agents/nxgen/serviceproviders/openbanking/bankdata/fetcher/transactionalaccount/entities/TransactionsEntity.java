package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
@Getter
@Setter
public class TransactionsEntity {

    protected List<BookedTransactionEntity> booked = Collections.emptyList();
    protected List<PendingTransactionEntity> pending = Collections.emptyList();

    @JsonProperty("_links")
    private Map<String, LinkEntity> links;

    public Collection<Transaction> toTinkTransactions() {
        final Stream<Transaction> bookedTransactionsStream =
                booked.stream().map(BookedTransactionEntity::toTinkTransaction);
        final Stream<Transaction> pendingTransactionsStream =
                pending.stream().map(PendingTransactionEntity::toTinkTransaction);

        return Stream.concat(bookedTransactionsStream, pendingTransactionsStream)
                .collect(Collectors.toList());
    }
}
