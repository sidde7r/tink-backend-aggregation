package se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    private BalancesItem balances;
    private TransactionsItem transactions;
    private AccountItem account;

    public BalancesItem getBalances() {
        return balances;
    }

    public TransactionsItem getTransactionsItem() {
        return transactions;
    }

    public AccountItem getAccount() {
        return account;
    }
}
