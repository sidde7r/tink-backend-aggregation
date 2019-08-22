package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionEntity {
    private List<BookedPendingTransactionEntity> booked = Collections.emptyList();
    private List<BookedPendingTransactionEntity> pending = Collections.emptyList();

    @JsonProperty("_links")
    private TransactionalLinksEntity links;

    public boolean hasMore() {

        /**
         * Bank API always sends null as "next" link. For this reason if we run the commented-out
         * statement (first statement), we will assume that there is no next page -which is a
         * mistake-. For this reason we return TRUE if and only if transactions are not empty here.
         * In this case the pagination will stop until it fetched certain amount of empty pages
         */
        // return
        // Optional.ofNullable(links).map(TransactionalLinksEntity::hasNextLink).orElse(false);
        // return (booked.size() + pending.size()) > 0;
        /**
         * We detected another bank side failure: when a user has a pending transaction, this
         * transaction is sent in response no matter what date range the agent uses in the request.
         * For such users, the return statement that we used above (second statement) would cause
         * infinite pagination. For this reason, temporarily we will use the return statement below.
         */
        return booked.size() > 0;
    }

    public Collection<Transaction> toTinkTransactions() {
        final Stream<Transaction> bookedTransactionsStream =
                booked.stream().map(entity -> entity.toTinkTransaction(false));

        /**
         * Since the bank erroneously sends pending transactions regardless of date range, we never
         * get an empty list which causes infinite loop in pagination. To avoid that we completely
         * ignore pending transactions until the bank fixes the bug.
         */
        /*final Stream<Transaction> pendingTransactionsStream =
                pending.stream().map(entity -> entity.toTinkTransaction(true));

        return Stream.concat(bookedTransactionsStream, pendingTransactionsStream)
                .collect(Collectors.toList());*/
        return bookedTransactionsStream.collect(Collectors.toList());
    }
}
