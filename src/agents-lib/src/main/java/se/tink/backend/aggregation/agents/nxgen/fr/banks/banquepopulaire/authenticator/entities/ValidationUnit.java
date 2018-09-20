package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ValidationUnit {
    private String type;
    private String id;
    private int minSize;

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public int getMinSize() {
        return minSize;
    }
}
