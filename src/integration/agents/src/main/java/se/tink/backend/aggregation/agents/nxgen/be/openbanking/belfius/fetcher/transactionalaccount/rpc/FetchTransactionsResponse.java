package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount.entity.transaction.BookedTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount.entity.transaction.TransactionsListEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@Slf4j
@JsonObject
public class FetchTransactionsResponse implements TransactionKeyPaginatorResponse<String> {

    @JsonProperty("transactions")
    private TransactionsListEntity transactionsListEntity;

    @Override
    public String nextKey() {
        return transactionsListEntity.getNextPageKey();
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactionsListEntity.getTransactions().stream()
                .map(BookedTransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        log.info(
                "Transactions amount on page is : {}",
                transactionsListEntity.getTransactions().size());
        return Optional.of(
                transactionsListEntity.getNextPageKey() != null
                        && !transactionsListEntity.getTransactions().isEmpty());
    }
}
