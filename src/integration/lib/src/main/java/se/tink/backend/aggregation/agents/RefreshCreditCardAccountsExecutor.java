package se.tink.backend.aggregation.agents;

import se.tink.backend.aggregation.agents.capability.CapabilityExecutor;

/**
 * Interface for executing refreshes of credit card accounts. Agent which supports Credit Card
 * Accounts shall implement this.
 */
public interface RefreshCreditCardAccountsExecutor extends CapabilityExecutor {

    /**
     * Fetch the credit card accounts.
     *
     * @return fetch accounts response
     */
    FetchAccountsResponse fetchCreditCardAccounts();

    /**
     * Fetch the transactions with existing credit card accounts.
     *
     * @return fetch transactions response
     */
    FetchTransactionsResponse fetchCreditCardTransactions();
}
