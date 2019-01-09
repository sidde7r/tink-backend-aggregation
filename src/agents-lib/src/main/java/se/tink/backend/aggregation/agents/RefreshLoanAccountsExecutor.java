package se.tink.backend.aggregation.agents;

/**
 * Interface for executing refreshes of loan accounts.
 * Agent which supports Loan Accounts shall implement this.
 */
public interface RefreshLoanAccountsExecutor {

    /**
     * Fetch the loan accounts.
     *
     * @return fetch accounts response
     */
    FetchLoanAccountsResponse fetchLoanAccounts();
}
