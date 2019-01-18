package se.tink.backend.aggregation.agents;

/**
 * Interface for executing refreshes of savings accounts.
 * Agent which supports Savings Accounts shall implement this.
 */
public interface RefreshSavingsAccountsExecutor {

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
