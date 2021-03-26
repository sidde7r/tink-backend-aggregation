package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class InsuranceEntity {

    @JsonProperty("insuranceCustomerId")
    private String insuranceCustomerId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("type")
    private String type;
}
