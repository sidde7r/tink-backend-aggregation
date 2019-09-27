package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.math.RoundingMode;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PositionsEntity {
    @JsonProperty private String category = "";
    @JsonProperty private BigDecimal changePercent;
    @JsonProperty private BigDecimal marketValue;
    @JsonProperty private String name = "";
    @JsonProperty private String securityID = "";

    @JsonIgnore
    public String getSecurityID() {
        return securityID;
    }

    @JsonIgnore
    public BigDecimal calculateOriginalValue() {
        return marketValue.divide(getIncrease(), 3, RoundingMode.HALF_UP);
    }

    @JsonIgnore
    private BigDecimal getIncrease() {
        return changePercent.movePointLeft(2).add(new BigDecimal(1));
    }
}
