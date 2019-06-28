package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vavr.CheckedPredicate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.Fetchers;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities.AccountTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities.PaginationEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class AccountTransactionsResponse implements TransactionKeyPaginatorResponse<String> {
    private List<AccountTransactionEntity> accountTransactions;
    private PaginationEntity pagination;
    private Integer totalResults;
    private Boolean hostResult;

    @JsonIgnore
    public static CheckedPredicate<AccountTransactionsResponse> shouldRetryFetching(int attempt) {
        return response -> attempt <= Fetchers.MAX_TRY_ATTEMPTS;
    }

    @Override
    public Collection<Transaction> getTinkTransactions() {
        return accountTransactions.stream()
                .map(AccountTransactionEntity::toTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(getPagination().getPage() < getPagination().getNumPages());
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

    @Override
    public String nextKey() {
        if (pagination == null) {
            return null;
        }
        return pagination.getNextPage();
    }
}
