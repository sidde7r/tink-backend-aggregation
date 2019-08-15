package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccessItem {

    private List<BalancesItem> balances;
    private List<TransactionsItem> transactions;
    private List<AccountsEntity> accounts;

    public List<BalancesItem> getBalances() {
        return balances;
    }

    public List<TransactionsItem> getTransactions() {
        return transactions;
    }

    public List<AccountsEntity> getAccounts() {
        return accounts;
    }
}
