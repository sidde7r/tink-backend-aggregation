package se.tink.backend.aggregation.agents.brokers.avanza.v2.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class KeyRatiosEntity {
    private double volatility;
    private double priceEarningsRatio;
    private double directYield;

    public double getVolatility() {
        return volatility;
    }

    public double getPriceEarningsRatio() {
        return priceEarningsRatio;
    }

    public double getDirectYield() {
        return directYield;
    }
}
