package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AssetInfoEntity {

    @JsonProperty("ticker")
    private String ticker;

    @JsonProperty("gap")
    private int gap;

    @JsonProperty("stampDutyRate")
    private int stampDutyRate;
}
