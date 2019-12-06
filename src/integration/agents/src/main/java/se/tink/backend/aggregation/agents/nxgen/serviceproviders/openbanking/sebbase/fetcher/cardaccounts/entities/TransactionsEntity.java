package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;

@JsonObject
public class TransactionsEntity {

    private List<BookedEntity> booked;
    private List<PendingEntity> pending;

    public List<BookedEntity> getBooked() {
        return booked;
    }

    public List<PendingEntity> getPending() {
        return pending;
    }

    @JsonIgnore
    public List<CreditCardTransaction> toTinkTransactions(String accountNumber) {
        List<CreditCardTransaction> bookedTransactions =
                collect(booked, BookedEntity::toTinkTransaction, accountNumber);
        List<CreditCardTransaction> pendingTransactions =
                collect(pending, PendingEntity::toTinkTransaction, accountNumber);
        List<CreditCardTransaction> transactions = new ArrayList<>(bookedTransactions);
        transactions.addAll(pendingTransactions);
        return transactions;
    }

    @JsonIgnore
    public <T> List<CreditCardTransaction> collect(
            List<T> transactions,
            Function<T, CreditCardTransaction> mapMethod,
            String accountNumber) {
        return Optional.ofNullable(transactions)
                .map(
                        transactionList ->
                                transactionList.stream()
                                        .map(mapMethod)
                                        .filter(
                                                transaction ->
                                                        isTransactionForCurrentAccount(
                                                                transaction, accountNumber))
                                        .collect(Collectors.toList()))
                .orElseGet(Collections::emptyList);
    }

    @JsonIgnore
    private boolean isTransactionForCurrentAccount(
            CreditCardTransaction creditCardTransaction, String accountNumber) {
        return accountNumber.equalsIgnoreCase(
                creditCardTransaction.getCreditCard().get().getCardNumber());
    }
}
