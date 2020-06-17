package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UserCredentials implements RequestComponent {

    @JsonProperty("ApplicationName")
    private String applicationName = "RFO";

    @JsonProperty("UserId")
    private String userId;

    @JsonIgnore
    public UserCredentials(String username) {
        userId = username;
    }
}
