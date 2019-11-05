package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ValueEntity {

    @JsonProperty("amount")
    private long amount;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("type")
    private String type;
}
