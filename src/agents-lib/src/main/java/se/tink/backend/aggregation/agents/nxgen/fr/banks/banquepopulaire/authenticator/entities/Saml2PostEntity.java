package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Saml2PostEntity {
    private String samlResponse;
    private String action;
    private String method;

    public String getSamlResponse() {
        return samlResponse;
    }

    public String getAction() {
        return action;
    }

    public String getMethod() {
        return method;
    }
}
