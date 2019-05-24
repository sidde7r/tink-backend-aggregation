
package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.fetcher.cardaccounts.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@JsonObject
public class Transactions {

    
    private List<Booked> booked;
    
    private List<Pending> pending;

    public List<Booked> getBooked() {
        return booked;
    }

    public void setBooked(List<Booked> booked) {
        this.booked = booked;
    }

    public List<Pending> getPending() {
        return pending;
    }

    public void setPending(List<Pending> pending) {
        this.pending = pending;
    }

    @JsonIgnore
    public List<CreditCardTransaction> toTinkTransactions(CreditCardAccount creditAccount) {
        List<CreditCardTransaction> bookedTransactions = collect(booked, (tr) -> tr.toTinkTransaction(creditAccount));
        List<CreditCardTransaction> pendingTransactions = collect(pending, (tr) -> tr.toTinkTransaction(creditAccount));

        List<CreditCardTransaction> transactions = new ArrayList<>(bookedTransactions);
        transactions.addAll(pendingTransactions);
        return transactions;
    }

    @JsonIgnore
    public <T> List<CreditCardTransaction> collect(List<T> transactions, Function<T, CreditCardTransaction> mapMethod) {
        List<CreditCardTransaction> result = null;
        if(transactions != null){
            result = transactions.stream().map( tr -> mapMethod.apply(tr)).collect(Collectors.toList());
        }else{
            result = Collections.EMPTY_LIST;
        }
        return result;
    }
}
