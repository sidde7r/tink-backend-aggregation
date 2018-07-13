package se.tink.backend.aggregation.agents.brokers.avanza.v2.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RelatedStockEntity {
    private String name;
    private String id;
    private double lastPrice;
    private double priceOneYearAgo;
    private String flagCode;

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public double getLastPrice() {
        return lastPrice;
    }

    public double getPriceOneYearAgo() {
        return priceOneYearAgo;
    }

    public String getFlagCode() {
        return flagCode;
    }
}
