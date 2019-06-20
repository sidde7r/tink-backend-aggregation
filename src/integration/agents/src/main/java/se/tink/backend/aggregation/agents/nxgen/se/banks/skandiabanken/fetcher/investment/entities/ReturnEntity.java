package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public class ReturnEntity {
    @JsonProperty("FromDate")
    private String fromDate;

    @JsonProperty("Return")
    private BigDecimal returnAmount;

    public String getFromDate() {
        return fromDate;
    }

    public BigDecimal getReturnAmount() {
        return returnAmount;
    }
}
