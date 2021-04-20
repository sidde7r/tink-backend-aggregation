package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.account.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vavr.CheckedPredicate;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.CajamarConstants.Fetchers;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.entities.PaginationEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.account.entities.AccountTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.account.entities.AccountTransactionsPaginationEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class CajamarAccountTransactionsResponse implements TransactionKeyPaginatorResponse<String> {

    private AccountTransactionsPaginationEntity accountTransactionsPagination;

    @JsonIgnore
    public static CheckedPredicate<CajamarAccountTransactionsResponse> shouldRetryFetching(
            int attempt) {
        return response -> attempt <= Fetchers.MAX_TRY_ATTEMPTS;
    }

    @JsonIgnore
    @Override
    public String nextKey() {
        if (isPaginationEqualNullOrEmpty()) {
            return null;
        }
        return String.valueOf(accountTransactionsPagination.getPagination().getPageNumber() + 1);
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return accountTransactionsPagination
                .getTransactions()
                .map(AccountTransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        if (!isPaginationEqualNullOrEmpty()) {
            PaginationEntity pagination = accountTransactionsPagination.getPagination();
            return Optional.of(pagination.getPageNumber() < pagination.getNumPages());
        }
        return Optional.of(false);
    }

    @JsonIgnore
    private boolean isPaginationEqualNullOrEmpty() {
        return accountTransactionsPagination == null
                || accountTransactionsPagination.getPagination() == null;
    }
}
