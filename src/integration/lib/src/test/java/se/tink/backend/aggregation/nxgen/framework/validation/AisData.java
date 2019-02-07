package se.tink.backend.aggregation.nxgen.framework.validation;

import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.models.Transaction;

import java.util.Collection;

/** A collection of aggregated data, i.e. accounts and transactions. */
public final class AisData {
    private final Collection<Account> accounts;
    private final Collection<Transaction> transactions;

    public AisData(final Collection<Account> accounts, final Collection<Transaction> transactions) {
        this.accounts = accounts;
        this.transactions = transactions;
    }

    public Collection<Account> getAccounts() {
        return accounts;
    }

    public Collection<Transaction> getTransactions() {
        return transactions;
    }

    @Override
    public String toString() {
        return String.format("AisData(%s, %s)", accounts, transactions);
    }
}
