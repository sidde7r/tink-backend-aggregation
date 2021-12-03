package se.tink.agent.sdk.fetching.capability;

import se.tink.agent.sdk.fetching.accounts.SavingsAccountsFetcher;

/**
 * A feature for constructing a Fetcher for returning SAVINGS accounts.
 *
 * <p>Use this interface, as opposed to {@link FetchCombinedCheckingSavingsAccounts} if the API you
 * integrate against has separate endpoints for CHECKING and SAVINGS accounts.
 */
public interface FetchSavingsAccounts {
    SavingsAccountsFetcher savingsAccountsFetcher();
}
