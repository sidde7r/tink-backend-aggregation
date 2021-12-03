package se.tink.agent.sdk.fetching.capability;

import se.tink.agent.sdk.fetching.transactions.TransactionsFetcher;

/**
 * A feature for constructing a combined Fetcher that are retruning transactions for both CHECKING
 * and SAVINGS accounts.
 *
 * <p>Use this interface, as opposed to {@link FetchCheckingTransactions} and/or {@link
 * FetchSavingsTransactions}, when the API you integrate against return both of these types on the
 * same endpoint.
 */
public interface FetchCombinedCheckingSavingsTransactions {
    TransactionsFetcher combinedCheckingSavingsTransactionsFetcher();
}
