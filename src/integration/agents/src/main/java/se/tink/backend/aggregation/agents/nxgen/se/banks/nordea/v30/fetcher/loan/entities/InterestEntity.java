package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InterestEntity {
    @JsonProperty private BigDecimal rate;

    @JsonProperty("reference_rate_type")
    private String referenceRateType;

    @JsonProperty("period_start_date")
    private String periodStartDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("discounted_rate_end_date")
    private Date discountedRateEndDate;

    @JsonProperty("base_rate")
    private BigDecimal baseRate;

    public BigDecimal getRate() {
        return rate;
    }

    public String getReferenceRateType() {
        return referenceRateType;
    }

    public String getPeriodStartDate() {
        return periodStartDate;
    }

    public BigDecimal getBaseRate() {
        return baseRate;
    }

    public Date getDiscountedRateEndDate() {
        return discountedRateEndDate;
    }
}
