package se.tink.backend.aggregation.agents.brokers.nordnet.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TradableEntity {
    @JsonProperty("market_id")
    private String marketId;
    @JsonProperty("tick_size_id")
    private String tickSizeId;
    @JsonProperty("display_order")
    private int displayOrder;
    @JsonProperty("lot_size")
    private double lotSize;
    private String identifier;

    public String getMarketId() {
        return marketId;
    }

    public void setMarketId(String marketId) {
        this.marketId = marketId;
    }

    public String getTickSizeId() {
        return tickSizeId;
    }

    public void setTickSizeId(String tickSizeId) {
        this.tickSizeId = tickSizeId;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public double getLotSize() {
        return lotSize;
    }

    public void setLotSize(double lotSize) {
        this.lotSize = lotSize;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}
