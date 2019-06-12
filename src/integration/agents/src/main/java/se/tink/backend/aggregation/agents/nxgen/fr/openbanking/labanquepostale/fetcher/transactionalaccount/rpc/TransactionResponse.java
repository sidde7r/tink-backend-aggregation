package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.entities.TransactionalLinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionResponse implements TransactionKeyPaginatorResponse<String> {
    private List<TransactionEntity> transactions;

    @JsonProperty("_links")
    private TransactionalLinksEntity links;

    @Override
    public String nextKey() {
        return links.getNextLink();
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        Stream<Transaction> bookedTransactionsStream =
                transactions.stream()
                        .filter(TransactionEntity::isBooked)
                        .map(TransactionEntity::toTinkTransaction);
        Stream<Transaction> pendingTransactionStream =
                transactions.stream()
                        .filter(transaction -> !transaction.isBooked())
                        .map(TransactionEntity::toTinkTransaction);
        return Stream.concat(bookedTransactionsStream, pendingTransactionStream)
                .collect(Collectors.toList());
    }

    public List<TransactionEntity> getTransactions() {
        return transactions;
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(links.hasNextLink());
    }
}
