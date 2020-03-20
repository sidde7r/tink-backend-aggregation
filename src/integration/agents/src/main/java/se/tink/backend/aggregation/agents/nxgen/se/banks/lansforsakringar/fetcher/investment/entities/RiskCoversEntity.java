package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RiskCoversEntity {
    private String riskCover;

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setCover(String riskCover) {
        this.riskCover = riskCover;
    }
}
