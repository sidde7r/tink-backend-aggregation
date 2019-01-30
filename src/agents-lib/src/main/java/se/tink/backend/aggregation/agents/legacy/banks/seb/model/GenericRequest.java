package se.tink.backend.aggregation.agents.banks.seb.model;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GenericRequest {
    private RequestWrappingEntity request;

    public GenericRequest(RequestWrappingEntity request) {
        this.request = request;
    }
}
