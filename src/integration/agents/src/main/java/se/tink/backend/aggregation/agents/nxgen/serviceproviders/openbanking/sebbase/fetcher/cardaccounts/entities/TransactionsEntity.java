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
    public List<CreditCardTransaction> toTinkTransactions() {
        List<CreditCardTransaction> bookedTransactions =
                collect(booked, BookedEntity::toTinkTransaction);
        List<CreditCardTransaction> pendingTransactions =
                collect(pending, PendingEntity::toTinkTransaction);

        List<CreditCardTransaction> transactions = new ArrayList<>(bookedTransactions);
        transactions.addAll(pendingTransactions);
        return transactions;
    }

    @JsonIgnore
    public <T> List<CreditCardTransaction> collect(
            List<T> transactions, Function<T, CreditCardTransaction> mapMethod) {
        return Optional.ofNullable(transactions)
                .map(t -> t.stream().map(mapMethod).collect(Collectors.toList()))
                .orElseGet(Collections::emptyList);
    }
}
