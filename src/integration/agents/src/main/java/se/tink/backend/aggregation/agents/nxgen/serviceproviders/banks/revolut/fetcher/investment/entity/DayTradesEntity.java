package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DayTradesEntity {

    @JsonProperty("current")
    private int current;

    @JsonProperty("limit")
    private int limit;
}
