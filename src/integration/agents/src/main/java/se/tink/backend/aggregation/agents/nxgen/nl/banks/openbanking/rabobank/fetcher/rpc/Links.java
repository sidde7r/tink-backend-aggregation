package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Links {

    @JsonProperty("balances")
    private String balances;

    @JsonProperty("transactions")
    private String transactions;

    @JsonProperty("account")
    private String account;

    @JsonProperty("first")
    private String first;

    @JsonProperty("last")
    private String last;

    @JsonProperty("next")
    private String next;

    @JsonProperty("previous")
    private String previous;

    public void setBalances(final String balances) {
        this.balances = balances;
    }

    public String getBalances() {
        return balances;
    }

    public void setTransactions(final String transactions) {
        this.transactions = transactions;
    }

    public String getTransactions() {
        return transactions;
    }

    public void setAccount(final String account) {
        this.account = account;
    }

    public String getAccount() {
        return account;
    }

    public void setFirst(final String first) {
        this.first = first;
    }

    public String getFirst() {
        return first;
    }

    public void setLast(final String last) {
        this.last = last;
    }

    public String getLast() {
        return last;
    }

    public void setNext(final String next) {
        this.next = next;
    }

    public String getNext() {
        return next;
    }

    public String getPrevious() {
        return previous;
    }

    public void setPrevious(final String previous) {
        this.previous = previous;
    }
}
