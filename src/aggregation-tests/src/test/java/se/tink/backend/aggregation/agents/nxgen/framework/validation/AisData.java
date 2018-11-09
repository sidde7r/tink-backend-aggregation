package se.tink.backend.aggregation.agents.nxgen.framework.validation;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.rpc.FetchProductsResponse;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.system.rpc.Transaction;

/**
 * A collection of aggregated data, i.e. accounts and transactions.
 */
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
