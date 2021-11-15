package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class DetailedPensionResponse {

    @JsonProperty("serverTime")
    private String serverTime;

    @JsonProperty("detailedPension")
    private DetailedPensionEntity detailedPension;
}
