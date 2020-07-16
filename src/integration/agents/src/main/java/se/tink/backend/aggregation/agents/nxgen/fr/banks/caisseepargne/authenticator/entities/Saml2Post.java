package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class Saml2Post {

    @JsonProperty("samlResponse")
    private String samlResponse;

    @JsonProperty("method")
    private String method;

    @JsonProperty("action")
    private String action;
}
