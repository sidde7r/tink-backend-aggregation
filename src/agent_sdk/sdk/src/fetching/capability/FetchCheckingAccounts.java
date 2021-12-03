package se.tink.agent.sdk.fetching.capability;

import se.tink.agent.sdk.fetching.accounts.CheckingAccountsFetcher;

/**
 * A feature for constructing a Fetcher for returning CHECKING accounts.
 *
 * <p>Use this interface, as opposed to {@link FetchCombinedCheckingSavingsAccounts} if the API you
 * integrate against has separate endpoints for CHECKING and SAVINGS accounts.
 */
public interface FetchCheckingAccounts {
    CheckingAccountsFetcher checkingAccountsFetcher();
}
