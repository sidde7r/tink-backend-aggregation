package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;

@JsonObject
@Getter
public class TransactionsEntity {
    private List<TransactionEntity> booked;
    private List<TransactionEntity> pending;

    @JsonIgnore
    public List<CreditCardTransaction> toTinkTransactions(String accountNumber) {
        return Stream.concat(
                        collect(booked, te -> te.toTinkTransaction(false), accountNumber),
                        collect(pending, te -> te.toTinkTransaction(true), accountNumber))
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public <T> Stream<CreditCardTransaction> collect(
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
                                                                transaction, accountNumber)))
                .orElse(Stream.empty());
    }

    @JsonIgnore
    private boolean isTransactionForCurrentAccount(
            CreditCardTransaction creditCardTransaction, String accountNumber) {
        return accountNumber.equalsIgnoreCase(
                creditCardTransaction.getCreditCard().get().getCardNumber());
    }
}
