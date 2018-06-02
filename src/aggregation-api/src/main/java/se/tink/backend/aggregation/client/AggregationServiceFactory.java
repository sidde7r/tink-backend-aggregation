package se.tink.backend.aggregation.client;

import se.tink.backend.aggregation.api.AggregationService;
import se.tink.backend.aggregation.api.CreditSafeService;
import se.tink.backend.aggregation.rpc.User;

public interface AggregationServiceFactory {
    String SERVICE_NAME = "aggregation";

    AggregationService getAggregationService();

    /**
     * Get an {@link AggregationService} using routing.
     * 
     * @param routeObject
     *            a {@link User} on which {@link User#getId()} will be used to determine which aggregation service will
     *            be routed to.
     * @return given the same upstream {@link AggregationService}s, returns the same {@link AggregationService} instance
     *         for <code>routeObject</code> with the same {@link User#getId()}.
     */
    public abstract AggregationService getAggregationService(User routeObject);

    public abstract CreditSafeService getCreditSafeService();
}
