package se.tink.agent.sdk.fetching.capability;

import se.tink.agent.sdk.fetching.transactions.TransactionsFetcher;

/**
 * A feature for constructing a Fetcher for returning transactions for CHECKING accounts.
 *
 * <p>Use this interface, as opposed to {@link FetchCombinedCheckingSavingsTransactions} if the API
 * you integrate against has separate endpoints for transactions for CHECKING and SAVINGS accounts.
 */
public interface FetchSavingsTransactions {
    TransactionsFetcher savingsTransactionsFetcher();
}
