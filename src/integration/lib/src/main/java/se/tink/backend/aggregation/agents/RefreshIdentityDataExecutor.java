package se.tink.backend.aggregation.agents;

/**
 * Interface for executing refreshes of customer identity data. An agent which supports fetching
 * customer identity data shall implement this.
 */
public interface RefreshIdentityDataExecutor {

    /**
     * Fetch customer identity data.
     *
     * @return fetch customer identity data response
     */
    FetchCustomerInfoResponse fetchCustomerInfo();
}
