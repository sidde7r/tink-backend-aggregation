package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction.entities;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
@Getter
public class TransactionEntity implements PaginatorResponse {
    private int pageCount;
    private List<PfmTransactionsEntity> pfmTransactions;

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        if (pfmTransactions == null) {
            return Collections.emptyList();
        }
        return pfmTransactions.stream()
                .map(PfmTransactionsEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(getPageCount() > 0);
    }

    public void addAll(Collection<PfmTransactionsEntity> pfmTransactionsEntities) {
        if (pfmTransactionsEntities != null && !pfmTransactionsEntities.isEmpty()) {
            this.pfmTransactions.addAll(pfmTransactionsEntities);
        }
    }

    public boolean canFetchNextPage(int currentPage) {
        return currentPage <= pageCount;
    }
}
