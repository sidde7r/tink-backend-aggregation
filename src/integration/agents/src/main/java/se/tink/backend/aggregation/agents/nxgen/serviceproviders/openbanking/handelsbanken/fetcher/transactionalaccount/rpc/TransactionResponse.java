package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity.TransactionsItemEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionResponse implements PaginatorResponse {

    private List<TransactionsItemEntity> transactions;

    public List<TransactionsItemEntity> getTransactions() {
        return transactions;
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        // Temporary hack to return empty list (incident IC-124)
        return Collections.emptyList();
        /*return transactions.stream()
        .map(TransactionsItemEntity::toTinkTransaction)
        .collect(Collectors.toList());*/
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.empty();
    }
}
