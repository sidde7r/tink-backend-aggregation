package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.transactional.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InterestsEntity {
    @JsonProperty("rate_type")
    private String rateType;

    @JsonProperty("accumulated_amount")
    private double accumulatedAmount;

    @JsonProperty("accumulated_year_amount")
    private double accumulatedYearAmount;

    @JsonProperty("accumulated_last_year_amount")
    private double accumulatedLastYearAmount;

    @JsonProperty("interest_intervals")
    private List<InterestIntervalsEntity> interestIntervals;
}
