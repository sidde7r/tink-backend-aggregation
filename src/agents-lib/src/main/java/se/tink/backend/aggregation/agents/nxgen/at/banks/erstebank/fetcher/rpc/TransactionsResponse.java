package se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.fetcher.rpc;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.fetcher.entity.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionsResponse implements TransactionPagePaginatorResponse {
    private int totalElements;
    private int pageSize;
    private int page;
    private int totalPages;
    private List<TransactionEntity> transactions;

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactions.stream().filter(TransactionEntity::isTransaction)
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public boolean canFetchMore() {
        return page < totalPages;
    }
}
