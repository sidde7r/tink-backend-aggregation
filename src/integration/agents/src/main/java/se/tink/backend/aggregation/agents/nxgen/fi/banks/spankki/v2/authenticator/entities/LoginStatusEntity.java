package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginStatusEntity {
    @JsonProperty private String passwordStatus = "";
    @JsonProperty private String passwordStatusMessage = "";
    @JsonProperty private String pinPosition = "";
    @JsonProperty private String pinStatus = "";
    @JsonProperty private String pinStatusMessage = "";

    @JsonIgnore
    public String getPinPosition() {
        return pinPosition;
    }
}
