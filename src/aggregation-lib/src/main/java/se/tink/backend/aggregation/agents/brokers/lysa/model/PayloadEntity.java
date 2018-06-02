package se.tink.backend.aggregation.agents.brokers.lysa.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PayloadEntity {
    private DetailsEntity details;

    public DetailsEntity getDetails() {
        return details;
    }

    public void setDetails(DetailsEntity details) {
        this.details = details;
    }
}
