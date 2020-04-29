package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SimpleInsuranceEntity {

    @JsonProperty("DETAIL_URL")
    private String detailHandle;

    public String getDetailHandle() {
        return detailHandle;
    }
}
