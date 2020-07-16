package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IdToken {

    @JsonProperty("last_login")
    private String lastLogin;

    @JsonProperty("auth_time")
    private AuthTime authTime;
}
