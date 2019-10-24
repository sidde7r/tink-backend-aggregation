package se.tink.backend.aggregation.agents.utils.authentication.encap3.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ActivatedMethodEntity {
    @JsonProperty private String authMethod;
    @JsonProperty private String authenticationKey;
    @JsonProperty private String challengeResponse;

    @JsonIgnore
    public ActivatedMethodEntity(
            String authMethod, String authenticationKey, String challengeResponse) {
        this.authMethod = authMethod;
        this.authenticationKey = authenticationKey;
        this.challengeResponse = challengeResponse;
    }
}
