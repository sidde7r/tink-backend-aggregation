package se.tink.backend.aggregation.agents;

import se.tink.backend.aggregation.agents.capability.CapabilityExecutor;

/**
 * Interface for executing refreshes of savings accounts. Agent which supports Savings Accounts
 * shall implement this.
 */
public interface RefreshSavingsAccountsExecutor extends CapabilityExecutor {

    /**
     * Fetch the savings accounts.
     *
     * @return fetch accounts response
     */
    FetchAccountsResponse fetchSavingsAccounts();

    /**
     * Fetch the transactions with existing savings accounts.
     *
     * @return fetch transactions response
     */
    FetchTransactionsResponse fetchSavingsTransactions();
}
