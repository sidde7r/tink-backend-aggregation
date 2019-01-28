package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FundTradingHoursEntity {
    @JsonProperty("TradingHours")
    private TradingHoursEntity tradingHours;
    @JsonProperty("GeneralInfo")
    private List<String> generalInfo;

    public TradingHoursEntity getTradingHours() {
        return tradingHours;
    }

    public List<String> getGeneralInfo() {
        return generalInfo;
    }
}
