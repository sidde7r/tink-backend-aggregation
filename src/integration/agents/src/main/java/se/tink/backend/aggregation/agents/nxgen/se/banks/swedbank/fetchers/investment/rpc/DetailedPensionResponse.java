package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DetailedPensionResponse {

    @JsonProperty("serverTime")
    private String serverTime;

    @JsonProperty("detailedPension")
    private DetailedPensionEntity detailedPension;

    public String getServerTime() {
        return serverTime;
    }

    public DetailedPensionEntity getDetailedPension() {
        return detailedPension;
    }
}
