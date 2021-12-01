package se.tink.backend.aggregation.agents;

import se.tink.backend.aggregation.agents.capability.CapabilityExecutor;

/**
 * Interface for executing refreshes of loan accounts. Agent which supports Loan Accounts shall
 * implement this.
 */
public interface RefreshLoanAccountsExecutor extends CapabilityExecutor {

    /**
     * Fetch the loan accounts.
     *
     * @return fetch accounts response
     */
    FetchLoanAccountsResponse fetchLoanAccounts();

    /**
     * Fetch loan transaction.
     *
     * @return fetch transaction response
     */
    FetchTransactionsResponse fetchLoanTransactions();
}
