package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.entities.InsuranceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
public class PensionDetailResponse {

    @JsonProperty("insured")
    private InsuranceEntity insuranceEntity;

    @JsonProperty("accountType")
    private String accountType;
}
