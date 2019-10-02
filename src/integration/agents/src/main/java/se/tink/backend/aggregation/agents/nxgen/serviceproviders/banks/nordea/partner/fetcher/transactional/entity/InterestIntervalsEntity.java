package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.transactional.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InterestIntervalsEntity {
    @JsonProperty("from_amount")
    private double fromAmount;

    @JsonProperty("to_amount")
    private double toAmount;

    private double rate;
}
