package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class WebAPI2Entity {
    private String authBusinessContextRoot;
    private String authAccessTokenURL;

    public String getAuthBusinessContextRoot() {
        return authBusinessContextRoot;
    }

    public String getAuthAccessTokenURL() {
        return authAccessTokenURL;
    }
}
