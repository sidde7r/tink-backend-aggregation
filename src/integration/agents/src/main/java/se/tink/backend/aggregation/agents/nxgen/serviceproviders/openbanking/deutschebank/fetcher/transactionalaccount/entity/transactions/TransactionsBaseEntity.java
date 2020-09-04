package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.fetcher.transactionalaccount.entity.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionsBaseEntity {

    protected List<BookedTransactionEntity> booked = Collections.emptyList();
    protected List<PendingTransactionEntity> pending = Collections.emptyList();

    @JsonProperty("_links")
    private TransactionLinksEntity links;

    public boolean hasMore() {
        return Optional.ofNullable(links).map(TransactionLinksEntity::hasNextLink).orElse(false);
    }

    public String getNextLink() {
        return links.getNextLink().getHref();
    }

    public Collection<Transaction> toTinkTransactions() {
        return booked.stream()
                .map(BookedTransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }
}
