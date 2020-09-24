package se.tink.backend.aggregation.agents;

/**
 * Interface for executing refreshes of investment accounts. Agent which supports Investment
 * Accounts shall implement this.
 */
public interface RefreshInvestmentAccountsExecutor extends CapabilityExecutor {

    /**
     * Fetch the investment accounts.
     *
     * @return fetch accounts response
     */
    FetchInvestmentAccountsResponse fetchInvestmentAccounts();

    /**
     * Fetch investment transactions
     *
     * @return fetch transactions response
     */
    FetchTransactionsResponse fetchInvestmentTransactions();
}
