package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditEntity {
    @JsonProperty private BigDecimal limit;
    @JsonProperty private BigDecimal available;
    @JsonProperty private BigDecimal spent;

    public BigDecimal getLimit() {
        return limit;
    }

    public BigDecimal getAvailable() {
        return available;
    }

    public BigDecimal getSpent() {
        return spent;
    }
}
