package se.tink.backend.aggregation.agents.brokers.avanza.v2.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RelatedFundsEntity {
    private String name;
    private String id;
    private Double changeSinceOneYear;

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public Double getChangeSinceOneYear() {
        return changeSinceOneYear;
    }
}
