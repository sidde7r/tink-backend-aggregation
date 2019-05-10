package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.authenticator.entities;

import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccessEntity {

    private List<Object> accounts;
    private List<Object> balances;
    private List<Object> transactions;

    public AccessEntity() {
        this.accounts = new ArrayList<>();
        this.balances = new ArrayList<>();
        this.transactions = new ArrayList<>();
    }

    public List<Object> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Object> accounts) {
        this.accounts = accounts;
    }

    public List<Object> getBalances() {
        return balances;
    }

    public void setBalances(List<Object> balances) {
        this.balances = balances;
    }

    public List<Object> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Object> transactions) {
        this.transactions = transactions;
    }
}
