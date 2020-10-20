package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class BusinessAccountEntity {
    @JsonProperty("NAMN")
    private String holderName;
}
