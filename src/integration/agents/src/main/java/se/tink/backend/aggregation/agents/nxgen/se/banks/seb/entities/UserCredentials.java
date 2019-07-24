package se.tink.backend.aggregation.agents.nxgen.se.banks.seb.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserCredentials implements RequestComponent {

    @JsonProperty("ApplicationName")
    private String applicationName = "MASP";

    @JsonProperty("UserId")
    private String userId;

    public UserCredentials(String username) {
        userId = username;
    }
}
