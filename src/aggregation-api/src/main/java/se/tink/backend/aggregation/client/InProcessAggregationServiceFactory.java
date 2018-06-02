package se.tink.backend.aggregation.client;

import se.tink.backend.aggregation.api.AggregationService;
import se.tink.backend.aggregation.api.CreditSafeService;
import se.tink.backend.aggregation.rpc.User;

public class InProcessAggregationServiceFactory implements AggregationServiceFactory {
    private AggregationService aggregationService;
    private CreditSafeService creditSafeService;

    @Override
    public AggregationService getAggregationService() {
        return aggregationService;
    }

    public void setAggregationService(AggregationService aggregationService) {
        this.aggregationService = aggregationService;
    }

    public void setCreditSafeService(CreditSafeService creditSafeService) {
        this.creditSafeService = creditSafeService;
    }

    @Override
    public CreditSafeService getCreditSafeService() {
        return creditSafeService;
    }

    @Override
    public AggregationService getAggregationService(User routeObject) {
        // There is only one upstream object. Returning it. That is, no routing needed.
        return getAggregationService();
    }
}
