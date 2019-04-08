package se.tink.backend.aggregation.agents;

import se.tink.backend.aggregation.nxgen.http.filter.ClientFilterFactory;

public interface HttpLoggableExecutor {
    /**
     * All agents that implement TransferExecutor are responsible of attaching a ClientFilter to any
     * HttpClient being used for doing transfers towards the bank's API endpoints when this method
     * is called.
     *
     * <p>Most common implementation of this method is to simply do:
     * filterFactory.addClientFilter(httpClient)
     *
     * @param filterFactory Creates filters that logs all requests and responses (attached before
     *     doing execute in TransferAgentWorkerCommand)
     */
    void attachHttpFilters(ClientFilterFactory filterFactory);
}
