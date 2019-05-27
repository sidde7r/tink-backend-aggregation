package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.fetcher.cardaccounts.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;

@JsonObject
public class Transactions {

    private List<Booked> booked;

    private List<Pending> pending;

    public List<Booked> getBooked() {
        return booked;
    }

    public List<Pending> getPending() {
        return pending;
    }

    @JsonIgnore
    public List<CreditCardTransaction> toTinkTransactions(CreditCardAccount creditAccount) {
        List<CreditCardTransaction> bookedTransactions =
                collect(booked, (tr) -> tr.toTinkTransaction(creditAccount));
        List<CreditCardTransaction> pendingTransactions =
                collect(pending, (tr) -> tr.toTinkTransaction(creditAccount));

        List<CreditCardTransaction> transactions = new ArrayList<>(bookedTransactions);
        transactions.addAll(pendingTransactions);
        return transactions;
    }

    @JsonIgnore
    public <T> List<CreditCardTransaction> collect(
            List<T> transactions, Function<T, CreditCardTransaction> mapMethod) {

        return Optional.ofNullable(transactions)
                .map(t -> t.stream().map(tr -> mapMethod.apply(tr)).collect(Collectors.toList()))
                .orElseGet(Collections::emptyList);
    }
}
