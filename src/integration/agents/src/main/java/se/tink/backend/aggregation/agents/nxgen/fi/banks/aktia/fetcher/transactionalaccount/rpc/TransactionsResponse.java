package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher.transactionalaccount.rpc;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher.transactionalaccount.entities.TransactionsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionsResponse implements TransactionKeyPaginatorResponse<String> {
    private AccountEntity account;
    // For reference.
    private List<Object> lockedEvents;
    private List<TransactionsEntity> transactions;
    private String continuationKey;

    @Override
    public String nextKey() {
        return continuationKey;
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactions.stream()
                .map(TransactionsEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(Objects.nonNull(continuationKey));
    }
}
