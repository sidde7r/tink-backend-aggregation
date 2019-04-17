package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.fetcher.transactionalaccount.rpc;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.fetcher.transactionalaccount.entity.transaction.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.fetcher.transactionalaccount.entity.transaction.BookedEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.fetcher.transactionalaccount.entity.transaction.PendingEntity;
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
        return Optional.of(false);
    }

    private List<Transaction> getBookedTransactions() {
        return transactions.getBooked().stream()
                .map(this::toTinkTransaction)
                .collect(Collectors.toList());
    }

    private List<Transaction> getPendingTransactions() {
        return transactions.getPending().stream()
                .map(this::toTinkTransaction)
                .collect(Collectors.toList());
    }

    private Transaction toTinkTransaction(BookedEntity transaction) {
        return Transaction.builder()
                .setPending(false)
                .setDate(transaction.getBookingDate())
                .setAmount(transaction.getTransactionAmount().toAmount())
                .setDescription(transaction.getTransactionText())
                .build();
    }

    private Transaction toTinkTransaction(PendingEntity transaction) {
        return Transaction.builder()
                .setPending(true)
                .setDate(transaction.getBookingDate())
                .setAmount(transaction.getTransactionAmount().toAmount())
                .setDescription(transaction.getTransactionText())
                .build();
    }
}
