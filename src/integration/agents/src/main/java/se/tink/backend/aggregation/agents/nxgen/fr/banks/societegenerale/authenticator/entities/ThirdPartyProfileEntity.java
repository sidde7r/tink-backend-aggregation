package se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ThirdPartyProfileEntity {
    @JsonProperty("HI469")
    private String hi469;

    @JsonProperty("HI52")
    private String hi52;
}
