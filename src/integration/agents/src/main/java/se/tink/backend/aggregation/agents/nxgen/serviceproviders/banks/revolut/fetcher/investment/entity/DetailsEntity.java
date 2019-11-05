package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DetailsEntity {

    @JsonProperty("ticker")
    private String ticker;

    @JsonProperty("pocketId")
    private String pocketId;

    public String getTicker() {
        return ticker;
    }

    public String getPocketId() {
        return pocketId;
    }
}
