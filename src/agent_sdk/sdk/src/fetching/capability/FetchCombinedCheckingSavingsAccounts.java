package se.tink.agent.sdk.fetching.capability;

import se.tink.agent.sdk.fetching.accounts.CombinedCheckingSavingsAccountsFetcher;

/**
 * A feature for constructing a combined Fetcher for returning both CHECKING and SAVINGS accounts.
 *
 * <p>Use this interface, as opposed to {@link FetchCheckingAccounts} and/or {@link
 * FetchSavingsAccounts}, when the API you integrate against return both of these types on the same
 * endpoint.
 */
public interface FetchCombinedCheckingSavingsAccounts {
    CombinedCheckingSavingsAccountsFetcher combinedCheckingSavingsAccountsFetcher();
}
