package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LatestTradeEntity {
    private boolean cancelled;
    private double price;
    private long volume;
    private String dealTime;
    private boolean matchedOnMarket;
    private String seller;
    private String buyer;

    public boolean getCancelled() {
        return cancelled;
    }

    public double getPrice() {
        return price;
    }

    public long getVolume() {
        return volume;
    }

    public String getDealTime() {
        return dealTime;
    }

    public boolean getMatchedOnMarket() {
        return matchedOnMarket;
    }

    public String getSeller() {
        return seller;
    }

    public String getBuyer() {
        return buyer;
    }
}
