package se.tink.backend.aggregationcontroller.resources;

import se.tink.backend.aggregationcontroller.v1.api.AggregationControllerService;

public class AggregationControllerServiceResource implements AggregationControllerService {
    @Override
    public String ping() {
        return "pong";
    }
}
