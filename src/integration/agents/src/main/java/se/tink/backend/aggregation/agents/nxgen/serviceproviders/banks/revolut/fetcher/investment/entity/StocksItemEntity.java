package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class StocksItemEntity {

    @JsonProperty("ticker")
    private String ticker;

    @JsonProperty("quantityDecimalPlaces")
    private int quantityDecimalPlaces;

    @JsonProperty("icon")
    private String icon;

    @JsonProperty("name")
    private String name;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("maxOrderValue")
    private int maxOrderValue;

    @JsonProperty("shortName")
    private String shortName;

    @JsonProperty("isin")
    private String isin;

    @JsonProperty("minOrderValue")
    private int minOrderValue;

    public String getTicker() {
        return ticker;
    }

    public int getQuantityDecimalPlaces() {
        return quantityDecimalPlaces;
    }

    public String getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }

    public String getCurrency() {
        return currency;
    }

    public int getMaxOrderValue() {
        return maxOrderValue;
    }

    public String getShortName() {
        return shortName;
    }

    public String getIsin() {
        return isin;
    }

    public int getMinOrderValue() {
        return minOrderValue;
    }
}
