package se.tink.backend.aggregation.agents.brokers.avanza.v2.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AmountEntity {
    private double percent;
    private double price;
    private long volume;

    public double getPercent() {
        return percent;
    }

    public double getPrice() {
        return price;
    }

    public long getVolume() {
        return volume;
    }
}
