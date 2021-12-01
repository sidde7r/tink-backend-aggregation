package se.tink.backend.aggregation.agents;

import se.tink.backend.aggregation.agents.capability.CapabilityExecutor;

/**
 * Interface for executing refreshes of checking accounts. Agent which supports Checking Accounts
 * shall implement this.
 */
public interface RefreshCheckingAccountsExecutor extends CapabilityExecutor {

    /**
     * Fetch the checking accounts.
     *
     * @return fetch accounts response
     */
    FetchAccountsResponse fetchCheckingAccounts();

    /**
     * Fetch the transactions with existing checking accounts.
     *
     * @return fetch transactions response
     */
    FetchTransactionsResponse fetchCheckingTransactions();
}
