package se.tink.backend.aggregation.agents.brokers.avanza.v2.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StockEntity {
    private String name;
    private long totalNumberOfShares;

    public String getName() {
        return name;
    }

    public long getTotalNumberOfShares() {
        return totalNumberOfShares;
    }
}
