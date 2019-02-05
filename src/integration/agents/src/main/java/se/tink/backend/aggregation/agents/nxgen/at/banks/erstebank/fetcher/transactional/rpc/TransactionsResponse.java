package se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.fetcher.transactional.rpc;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.fetcher.transactional.entity.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionsResponse implements PaginatorResponse {
    private int totalElements;
    private int pageSize;
    private int page;
    private int totalPages;
    private List<TransactionEntity> transactions;

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactions.stream().filter(TransactionEntity::isValidTransaction)
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(totalElements > 0 && page < totalPages);
    }
}
