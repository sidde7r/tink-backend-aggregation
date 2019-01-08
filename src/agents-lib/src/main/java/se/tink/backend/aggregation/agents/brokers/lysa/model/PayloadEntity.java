package se.tink.backend.aggregation.agents.brokers.lysa.model;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PayloadEntity {
    private DetailsEntity details;

    public DetailsEntity getDetails() {
        return details;
    }

    public void setDetails(DetailsEntity details) {
        this.details = details;
    }
}
