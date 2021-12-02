package se.tink.backend.aggregation.agents;

import se.tink.backend.aggregation.agents.capability.CapabilityExecutor;

/**
 * Interface for executing refreshes of customer identity data. An agent which supports fetching
 * customer identity data shall implement this.
 */
public interface RefreshIdentityDataExecutor extends CapabilityExecutor {

    /**
     * Fetch customer identity data.
     *
     * @return fetch customer identity data response
     */
    FetchIdentityDataResponse fetchIdentityData();
}
