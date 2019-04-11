package se.tink.backend.aggregation.nxgen.framework.validation;

import java.util.Collection;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.libraries.identitydata.IdentityData;

/** A collection of aggregated data, i.e. accounts and transactions. */
public final class AisData {
    private final Collection<Account> accounts;
    private final Collection<Transaction> transactions;
    private final IdentityData customerInfo;

    public AisData(
            final Collection<Account> accounts,
            final Collection<Transaction> transactions,
            IdentityData customerInfo) {
        this.accounts = accounts;
        this.transactions = transactions;
        this.customerInfo = customerInfo;
    }

    public Collection<Account> getAccounts() {
        return accounts;
    }

    public Collection<Transaction> getTransactions() {
        return transactions;
    }

    public IdentityData getIdentityData() {
        return customerInfo;
    }

    @Override
    public String toString() {
        return String.format("AisData(%s, %s, %s)", accounts, transactions, customerInfo);
    }
}
