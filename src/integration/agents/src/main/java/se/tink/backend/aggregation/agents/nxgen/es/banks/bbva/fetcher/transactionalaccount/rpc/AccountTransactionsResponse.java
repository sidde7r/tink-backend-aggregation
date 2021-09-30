package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vavr.CheckedPredicate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
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
    @JsonIgnore
    public Collection<Transaction> getTinkTransactions() {
        return accountTransactions.stream()
                .map(AccountTransactionEntity::toTransaction)
                .collect(Collectors.toList());
    }

    @Override
    @JsonIgnore
    public Optional<Boolean> canFetchMore() {
        if (!Objects.isNull(pagination)) {
            return Optional.of(getPagination().getPage() < getPagination().getNumPages());
        } else {
            return Optional.of(false);
        }
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
    @JsonIgnore
    public String nextKey() {
        if (pagination == null) {
            return null;
        }
        if (pagination.getNextPage() != null) {
            return pagination.getNextPage();
        }
        return String.valueOf(pagination.getPage() + 1);
    }

    void setPagination(PaginationEntity pagination) {
        this.pagination = pagination;
    }
}
