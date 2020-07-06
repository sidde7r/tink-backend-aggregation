package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Userinfo {

    @JsonProperty("cdetab")
    private String cdetab;

    @JsonProperty("authMethod")
    private String authMethod;

    @JsonProperty("authLevel")
    private String authLevel;
}
