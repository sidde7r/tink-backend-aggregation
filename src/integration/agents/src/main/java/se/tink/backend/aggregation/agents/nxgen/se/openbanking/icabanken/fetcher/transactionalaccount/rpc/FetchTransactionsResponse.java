package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.fetcher.transactionalaccount.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.fetcher.transactionalaccount.entity.transaction.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.fetcher.transactionalaccount.entity.transaction.TransactionsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class FetchTransactionsResponse implements PaginatorResponse {

    private AccountEntity account;
    private TransactionsEntity transactions;

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return Stream.concat(getBookedTransactions().stream(), getPendingTransactions().stream())
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.empty();
    }

    private List<Transaction> getBookedTransactions() {
        return Optional.ofNullable(transactions.getBooked()).orElse(Collections.emptyList())
                .stream()
                .map(transactionEntity -> transactionEntity.toTinkTransaction(false))
                .collect(Collectors.toList());
    }

    private List<Transaction> getPendingTransactions() {
        return Optional.ofNullable(transactions.getPending()).orElse(Collections.emptyList())
                .stream()
                .map(transactionEntity -> transactionEntity.toTinkTransaction(true))
                .collect(Collectors.toList());
    }
}
