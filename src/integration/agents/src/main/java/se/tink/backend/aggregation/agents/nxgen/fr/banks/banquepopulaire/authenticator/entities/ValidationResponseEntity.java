package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ValidationResponseEntity {
    private String status;
    @JsonProperty("saml2_post")
    private Saml2PostEntity saml2Post;

    public Saml2PostEntity getSaml2Post() {
        return saml2Post;
    }

    public String getStatus() {
        return status;
    }
}

