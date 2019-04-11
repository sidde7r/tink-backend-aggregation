package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TradingHoursEntity {
    @JsonProperty("Affärsdagar vid köp")
    private String businessDaysWhenBuy;

    @JsonProperty("Affärsdagar vid sälj")
    private String businessDaysWhenSell;

    @JsonProperty("Bryttid")
    private String cutoffTime;

    @JsonProperty("Syns på fondsparandet")
    private String visibleInFundSavings;

    public String getBusinessDaysWhenBuy() {
        return businessDaysWhenBuy;
    }

    public String getBusinessDaysWhenSell() {
        return businessDaysWhenSell;
    }

    public String getCutoffTime() {
        return cutoffTime;
    }

    public String getVisibleInFundSavings() {
        return visibleInFundSavings;
    }
}
