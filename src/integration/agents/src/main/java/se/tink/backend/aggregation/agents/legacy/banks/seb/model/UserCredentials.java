package se.tink.backend.aggregation.agents.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserCredentials {

    @JsonProperty("ApplicationName")
    public String ApplicationName = "MASP";

    @JsonProperty("UserId")
    public String UserId;

    public UserCredentials(String username) {
        UserId = username;
    }
}
