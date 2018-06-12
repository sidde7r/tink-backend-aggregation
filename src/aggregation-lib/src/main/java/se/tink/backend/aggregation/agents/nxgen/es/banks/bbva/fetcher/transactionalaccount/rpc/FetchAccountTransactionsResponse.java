package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.rpc;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities.AccountTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities.PaginationEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class FetchAccountTransactionsResponse implements TransactionPagePaginatorResponse {

    private List<AccountTransactionEntity> accountTransactions;
    private PaginationEntity pagination;
    private Integer totalResults;
    private Boolean hostResult;

    @Override
    public Collection<Transaction> getTinkTransactions() {
        return accountTransactions.stream()
                .map(AccountTransactionEntity::toTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public boolean canFetchMore() {
        return getPagination().getPage() < getPagination().getNumPages();
    }

    public PaginationEntity getPagination() {
        return pagination;
    }

    public List<AccountTransactionEntity> getAccountTransactions() {
        return accountTransactions;
    }

    public Integer getTotalResults() {
        return totalResults;
    }

    public Boolean getHostResult() {
        return hostResult;
    }
}
