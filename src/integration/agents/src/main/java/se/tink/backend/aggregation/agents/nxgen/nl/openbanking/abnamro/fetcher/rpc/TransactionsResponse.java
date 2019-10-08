package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.fetcher.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.fetcher.entities.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionsResponse implements TransactionKeyPaginatorResponse<String> {

    private String accountNumber;
    private String nextPageKey;
    private List<TransactionEntity> transactions;

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getNextPageKey() {
        return nextPageKey;
    }

    public List<TransactionEntity> getTransactions() {
        return Optional.ofNullable(transactions).orElse(Collections.emptyList());
    }

    @Override
    public String nextKey() {
        return nextPageKey;
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return getTransactions().stream()
                .map(transactionEntity -> transactionEntity.toTinkTransaction())
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(Objects.nonNull(nextPageKey));
    }
}
