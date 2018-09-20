package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TypeEntity {
    private String code;
    private String label;
    private String type;

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public String getType() {
        return type;
    }
}
