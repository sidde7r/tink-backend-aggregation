package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InterestEntity {
    @JsonProperty
    private double rate;
    @JsonProperty("reference_rate_type")
    private String referenceRateType;
    @JsonProperty("period_start_date")
    private String periodStartDate;
    @JsonProperty("base_rate")
    private double baseRate;

    public double getRate() {
        return rate;
    }

    public String getReferenceRateType() {
        return referenceRateType;
    }

    public String getPeriodStartDate() {
        return periodStartDate;
    }

    public double getBaseRate() {
        return baseRate;
    }
}
