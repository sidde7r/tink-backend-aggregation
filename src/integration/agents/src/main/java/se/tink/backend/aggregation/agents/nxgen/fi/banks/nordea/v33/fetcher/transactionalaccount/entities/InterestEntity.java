package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InterestEntity {
    @JsonProperty("rate_type")
    private String rateType;

    @JsonProperty("interest_intervals")
    private List<InterestIntervalEntity> interestIntervals;
}
