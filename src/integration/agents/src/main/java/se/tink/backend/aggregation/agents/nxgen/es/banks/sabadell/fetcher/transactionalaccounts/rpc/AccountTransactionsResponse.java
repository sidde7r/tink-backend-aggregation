package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.transactionalaccounts.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.transactionalaccounts.entities.PeriodMovementModelListEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.transactionalaccounts.entities.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class AccountTransactionsResponse implements TransactionKeyPaginatorResponse<Boolean> {
    private boolean moreElements;
    private List<PeriodMovementModelListEntity> periodMovementModelList;

    public boolean hasMoreElements() {
        return moreElements;
    }

    public List<PeriodMovementModelListEntity> getPeriodMovementModelList() {
        return periodMovementModelList;
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return Optional.ofNullable(periodMovementModelList).orElseGet(Collections::emptyList)
                .stream()
                .flatMap(periodMovementModelListEntity ->
                        periodMovementModelListEntity.getGenericMovementWrapperList().getMovements().stream())
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(moreElements);
    }

    @Override
    public Boolean nextKey() {
        return moreElements;
    }
}
